package com.bytemedrive.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
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
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.FileInputStream
import java.time.ZonedDateTime

class ServiceThumbnailDownload : Service() {

    private val TAG = ServiceThumbnailDownload::class.qualifiedName

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val fileManager: FileManager by inject()

    private val dataFileRepository: DataFileRepository by inject()
    private val eventPublisher: EventPublisher by inject()

    private val resolutions: List<Resolution> = listOf(Resolution.P360, Resolution.P1280)

    private lateinit var handler: Handler
    private lateinit var thumbnailDownloader: Runnable

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        handler = Handler()
        thumbnailDownloader = Runnable {
            serviceScope.launch(Dispatchers.IO) {
                downloadThumbnails()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(thumbnailDownloader)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(thumbnailDownloader)
    }

    private suspend fun downloadThumbnails() {
        try {
            Log.d(TAG, "Checking whether there are any thumbnails to download")
            val fileList = applicationContext.fileList()

            val dataFiles = dataFileRepository.getDataFilesByUploadStatus(UploadStatus.COMPLETED)
            for (dataFile in dataFiles) {
                for (resolution in resolutions) {
                    val thumbnailName = FileManager.getThumbnailName(dataFile.id, resolution)

                    if (dataFile.contentType == MimeTypes.IMAGE_JPEG && !fileList.contains(thumbnailName)) {
                        dataFile.thumbnails
                            .find { it.resolution == resolution }
                            ?.let { thumbnail ->
                                Log.i(TAG, "Downloading thumbnail ${resolution.value} for ${dataFile.name}")

                                val encryptedFile = fileManager.rebuildFile(
                                    thumbnail.chunks.map(UploadChunk::viewId),
                                    FileManager.getThumbnailName(dataFile.id, resolution),
                                    thumbnail.contentType,
                                    applicationContext.cacheDir
                                )

                                val sizeOfChunks = thumbnail.chunks.sumOf(UploadChunk::sizeBytes)

                                if (sizeOfChunks != encryptedFile.length()) {
                                    Log.e(
                                        TAG,
                                        "Removing thumbnail due to encrypted thumbnail size ${encryptedFile.length()} is not same as encrypted thumbnail chunks size $sizeOfChunks"
                                    )
                                    val thumbnailRemoved = dataFile.copy(thumbnails = dataFile.thumbnails.filterNot { it.resolution == thumbnail.resolution })
                                    dataFileRepository.updateDataFile(thumbnailRemoved)
                                } else {
                                    AesService.decryptFileWithKey(
                                        FileInputStream(encryptedFile),
                                        applicationContext.openFileOutput(thumbnailName, Context.MODE_PRIVATE),
                                        AesService.secretKey(thumbnail.secretKeyBase64),
                                        thumbnail.sizeBytes
                                    )

                                    eventPublisher.publishEvent(EventThumbnailCompleted(dataFile.id, resolution, ZonedDateTime.now()))
                                }
                            }
                    }
                }
            }

            handler.postDelayed(thumbnailDownloader, 10_000)
        } catch (e: Exception) {
            GlobalExceptionHandler.throwable = e
        }
    }
}