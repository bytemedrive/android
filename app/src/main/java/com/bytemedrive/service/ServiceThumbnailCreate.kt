package com.bytemedrive.service

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.media3.common.MimeTypes
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.root.UploadChunk
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.image.ImageManager
import com.bytemedrive.privacy.AesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class ServiceThumbnailCreate : Service() {

    private val TAG = ServiceThumbnailCreate::class.qualifiedName

    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val fileManager: FileManager by inject()

    private val dataFileRepository: DataFileRepository by inject()

    private val resolutions: List<Resolution> = listOf(Resolution.P360, Resolution.P1280)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            while (true) {
                try {
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "Checking whether there are any thumbnails to create")
                        dataFileRepository.getAllDataFiles().forEach { dataFile ->
                            val chunkViewIds = dataFile.chunks.map(UploadChunk::viewId)

                            resolutions.forEach { resolution ->
                                if (dataFile.contentType == MimeTypes.IMAGE_JPEG && dataFile.thumbnails.find { it.resolution == resolution } == null) {
                                    Log.i(TAG, "Missing thumbnail with resolution $resolution for file chunk view ids=$chunkViewIds.")
                                    val encryptedFile = fileManager.rebuildFile(chunkViewIds, "${dataFile.id}-encrypted", dataFile.contentType, applicationContext.cacheDir)

                                    val sizeOfChunks = dataFile.chunks.sumOf(UploadChunk::sizeBytes)

                                    if (sizeOfChunks != encryptedFile.length()) {
                                        Log.e(TAG, "Encrypted file size ${encryptedFile.length()} is not same as encrypted file chunks size $sizeOfChunks")
                                    } else {
                                        val fileDecrypted = Files.createTempFile("${dataFile.id}", ".decrypted")
                                        AesService.decryptFileWithKey(
                                            FileInputStream(encryptedFile),
                                            FileOutputStream(fileDecrypted.toFile()),
                                            AesService.secretKey(dataFile.secretKeyBase64!!),
                                            dataFile.sizeBytes
                                        )
                                        var thumbnail = fileManager.getThumbnail(BitmapFactory.decodeFile(fileDecrypted.toFile().absolutePath), resolution)

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