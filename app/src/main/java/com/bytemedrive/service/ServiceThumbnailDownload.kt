package com.bytemedrive.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MimeTypes
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class ServiceThumbnailDownload : Service() {

    private val TAG = ServiceThumbnailDownload::class.qualifiedName

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val fileManager: FileManager by inject()

    private val resolutions: List<Resolution> = listOf(Resolution.P360, Resolution.P1280)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    Log.i(TAG, "Checking whether there are missing thumbnails to be downloaded.")
                    val fileList = applicationContext.fileList();

                    AppState.customer.value?.dataFiles?.forEach { dataFile ->
                        resolutions.forEach { resolution ->
                            val thumbnailName = getThumbnailNameWithExtension(dataFile.name, resolution)

                            if (dataFile.contentType == MimeTypes.IMAGE_JPEG && !fileList.contains(thumbnailName)) {
                                Log.i(TAG, "Downloading thumbnail ${resolution.value} for ${dataFile.name}")

                                dataFile.thumbnails
                                    .find { it.resolution == resolution }
                                    ?.let { thumbnail ->
                                        val encryptedFile = fileManager.rebuildFile(
                                            thumbnail.chunksViewIds,
                                            getThumbnailName(dataFile.name, resolution),
                                            thumbnail.contentType,
                                            applicationContext.cacheDir
                                        )

                                        val fileDecrypted = AesService.decryptWithKey(encryptedFile.readBytes(), thumbnail.secretKey)

                                        applicationContext.openFileOutput(thumbnailName, Context.MODE_PRIVATE).use { it.write(fileDecrypted) }
                                    }
                            }
                        }
                    }

                    AppState.customer.update { it }

                    TimeUnit.SECONDS.sleep(10)
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }

    private fun getThumbnailNameWithExtension(fileName: String, resolution: Resolution): String {
        val split = fileName.split(".");

        return "${getThumbnailName(fileName, resolution)}.${split.last()}"
    }

    private fun getThumbnailName(fileName: String, resolution: Resolution): String {
        val split = fileName.split(".");

        return "${split.first()}_${resolution.value}"
    }
}