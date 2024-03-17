package com.bytemedrive.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bytemedrive.MainActivity
import com.bytemedrive.R
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.database.FileUpload
import com.bytemedrive.file.root.EventFileUploadFailed
import com.bytemedrive.file.root.QueueFileUploadRepository
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.time.ZonedDateTime

class ServiceFileUpload : Service() {

    private val TAG = ServiceFileUpload::class.qualifiedName

    private val queueFileUploadRepository: QueueFileUploadRepository by inject()
    private val fileManager: FileManager by inject()
    private val eventPublisher: EventPublisher by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private lateinit var handler: Handler
    private lateinit var fileUploader: Runnable
    
    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        createChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = notificationBuilder(pendingIntent)

        handler = Handler()
        fileUploader = Runnable {
            serviceScope.launch(Dispatchers.IO) {
                fileUpload(notification)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(fileUploader)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(fileUploader)
    }

    private suspend fun fileUpload(notification: NotificationCompat.Builder) {
        try {
            Log.d(TAG, "Checking whether there are any files to upload")

            val filesToUpload = queueFileUploadRepository.getFiles()

            if (filesToUpload.isNotEmpty()) {
                startForeground(NOTIFICATION_ID, notification.build())

                filesToUpload.forEachIndexed { index, fileUpload ->
                    uploadFile(fileUpload)
                    updateNotification(notification, "${index + 1} / ${filesToUpload.size} is being uploaded")
                }

                stopForeground(STOP_FOREGROUND_DETACH)
            }

            handler.postDelayed(fileUploader, 10_000)
        } catch (e: Exception) {
            GlobalExceptionHandler.throwable = e
        }
    }

    private suspend fun uploadFile(fileUpload: FileUpload) {
        Log.i(TAG, "Started uploading file id=${fileUpload.id}")

        val file = File(fileUpload.path)

        if (file.exists()) {
            try {
                fileManager.uploadFile(fileUpload, applicationContext.cacheDir, file)
            } catch (error: OutOfMemoryError) {
                Log.e(TAG, "File upload failed! File path=${file.path}.", error)
                queueFileUploadRepository.deleteFile(fileUpload.id)
                eventPublisher.publishEvent(EventFileUploadFailed(fileUpload.id, ZonedDateTime.now()))
            } catch (exception: Exception) {
                Log.e(TAG, "File upload failed! File path=${file.path}.", exception)
                queueFileUploadRepository.deleteFile(fileUpload.id)
                eventPublisher.publishEvent(EventFileUploadFailed(fileUpload.id, ZonedDateTime.now()))
                
                throw exception
            }
        } else {
            Log.w(TAG, "File upload canceled. File ${file.path} could not be found.")
        }

        Log.i(TAG, "Removing file id=${fileUpload.id} from uploading queue")
        queueFileUploadRepository.deleteFile(fileUpload.id)

        Log.i(TAG, "Finished uploading file id=${fileUpload.id}")
    }

    private fun createChannel() {
        notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun updateNotification(builder: NotificationCompat.Builder, updatedText: String) {
        val updatedNotification = builder.setContentText(updatedText).build()

        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    private fun notificationBuilder(pendingIntent: PendingIntent) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("File uploading progress")
        .setSmallIcon(R.drawable.baseline_file_upload_24)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setContentIntent(pendingIntent)
        .setSilent(true)

    companion object {

        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "bytemedrive.fileupload"
        const val CHANNEL_NAME = "File Upload"
    }
}