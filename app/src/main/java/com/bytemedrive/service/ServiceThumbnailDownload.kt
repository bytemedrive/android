package com.bytemedrive.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MimeTypes
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.file.root.EventThumbnailCompleted
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.root.UploadChunk
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.lang.IllegalStateException
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class ServiceThumbnailDownload : Service() {

    private val TAG = ServiceThumbnailDownload::class.qualifiedName

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val fileManager: FileManager by inject()

    private val dataFileRepository: DataFileRepository by inject()
    private val eventPublisher: EventPublisher by inject()

    private val resolutions: List<Resolution> = listOf(Resolution.P360, Resolution.P1280)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (true) {
                try {
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "Checking whether there are any thumbnails to download")
                        val fileList = applicationContext.fileList()

                        dataFileRepository.getDataFilesByUploadStatus(UploadStatus.COMPLETED).forEach { dataFile ->
                            resolutions.forEach { resolution ->
                                val thumbnailName = FileManager.getThumbnailName(dataFile.id, resolution)

                                if (dataFile.contentType == MimeTypes.IMAGE_JPEG && !fileList.contains(thumbnailName)) {
                                    Log.i(TAG, "Downloading thumbnail ${resolution.value} for ${dataFile.name}")

                                    dataFile.thumbnails
                                        .find { it.resolution == resolution }
                                        ?.let { thumbnail ->
                                            val encryptedFile = fileManager.rebuildFile(
                                                thumbnail.chunks.map(UploadChunk::viewId),
                                                FileManager.getThumbnailName(dataFile.id, resolution),
                                                thumbnail.contentType,
                                                applicationContext.cacheDir
                                            )

                                            val sizeOfChunks = thumbnail.chunks.sumOf(UploadChunk::sizeBytes)

                                            if (sizeOfChunks != encryptedFile.length()) {
                                                Log.e(TAG, "Removing thumbnail due to encrypted thumbnail size ${encryptedFile.length()} is not same as encrypted thumbnail chunks size $sizeOfChunks")
                                                val thumbnailRemoved = dataFile.copy(thumbnails = dataFile.thumbnails.filterNot { it.resolution == thumbnail.resolution })
                                                dataFileRepository.updateDataFile(thumbnailRemoved)
                                            } else {
                                                val fileDecrypted = AesService.decryptWithKey(encryptedFile.readBytes(), AesService.secretKey(thumbnail.secretKeyBase64))

                                                applicationContext.openFileOutput(thumbnailName, Context.MODE_PRIVATE).use { it.write(fileDecrypted) }

                                                eventPublisher.publishEvent(EventThumbnailCompleted(dataFile.id, resolution, ZonedDateTime.now()))
                                            }
                                        }
                                }
                            }
                        }

                        TimeUnit.SECONDS.sleep(10)
                    }
                } catch (e: Exception) {
                    GlobalExceptionHandler.throwable = e
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