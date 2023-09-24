package com.bytemedrive.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bytemedrive.MainActivity
import com.bytemedrive.R
import com.bytemedrive.database.FileUpload
import com.bytemedrive.file.root.FileUploadQueueRepository
import com.bytemedrive.file.shared.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.util.concurrent.TimeUnit

class FileUploadService : Service() {

    private val TAG = FileUploadService::class.qualifiedName

    private val fileUploadQueueRepository: FileUploadQueueRepository by inject()
    private val fileManager: FileManager by inject()
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = notificationBuilder(pendingIntent)

        startForeground(NOTIFICATION_ID, notification.build())

        serviceScope.launch {
            // TODO: Temporary solution - should be improved
            while (true) {
                withContext(Dispatchers.IO) {
                    val filesToUpload = fileUploadQueueRepository.getFiles()

                    filesToUpload.forEachIndexed { index, fileUpload ->
                        uploadFile(fileUpload)
                        updateNotification(notification, "${index + 1} / ${filesToUpload.size} is being uploaded")
                    }

                    notificationManager.cancel(NOTIFICATION_ID)

                    TimeUnit.SECONDS.sleep(60)
                }
            }
        }

        return START_STICKY
    }

    private suspend fun uploadFile(fileUpload: FileUpload) {
        Log.i(TAG, "Started uploading ${fileUpload.id}")

        val file = File(fileUpload.path)

        if (file.exists()) {
            fileManager.uploadFile(fileUpload, applicationContext.cacheDir, file)
        } else {
            Log.w(TAG, "File upload canceled. File ${file.path} could not be found.")
        }

        fileUploadQueueRepository.deleteFile(fileUpload.id)

        Log.i(TAG, "Finished uploading ${fileUpload.id}")
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }

    companion object {

        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "bytemedrive.fileupload"
        const val CHANNEL_NAME = "File Upload"
    }
}