package com.bytemedrive.service

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MimeTypes
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.image.ImageManager
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ServiceThumbnailCreate : Service() {

    private val TAG = ServiceThumbnailCreate::class.qualifiedName

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val fileManager: FileManager by inject()

    private val resolutions: List<Resolution> = listOf(Resolution.P360, Resolution.P1280)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (true) {
                withContext(Dispatchers.IO) {
                    Log.i(TAG, "Checking whether there are missing thumbnails to be created.")
                    AppState.customer.value?.dataFiles?.forEach { dataFile ->
                        Log.i(TAG, "File chunk view ids=${dataFile.chunksViewIds} has ${dataFile.thumbnails.size} thumbnails.")

                        resolutions.forEach { resolution ->
                            if (dataFile.contentType == MimeTypes.IMAGE_JPEG && dataFile.thumbnails.find { it.resolution == resolution } == null) {
                                Log.i(TAG, "Missing thumbnail with resolution $resolution for file chunk view ids=${dataFile.chunksViewIds}.")
                                val encryptedFile = fileManager.rebuildFile(dataFile.chunksViewIds, "${dataFile.id}-encrypted", dataFile.contentType, applicationContext.cacheDir)

                                if (encryptedFile.readBytes().isNotEmpty()) {
                                    val decryptedBytes = AesService.decryptWithKey(encryptedFile.readBytes(), dataFile.secretKey)
                                    var thumbnail = fileManager.getThumbnail(BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size), resolution)

                                    dataFile.exifOrientation?.let { thumbnail = ImageManager.rotateBitmap(thumbnail, it) }

                                    val stream = ByteArrayOutputStream()
                                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                                    Log.i(TAG, "Uploading thumbnail with resolution $resolution for file id=${dataFile.id}.")
                                    fileManager.uploadThumbnail(stream.toByteArray(), applicationContext.cacheDir, dataFile.id, dataFile.contentType, resolution)
                                }
                            }
                        }
                    }

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
}