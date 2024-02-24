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
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.file.root.QueueFileDownloadRepository
import com.bytemedrive.file.shared.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.UUID
import java.util.concurrent.TimeUnit

class ServiceFileDownload : Service() {

    private val TAG = ServiceFileDownload::class.qualifiedName

    private val queueFileDownloadRepository: QueueFileDownloadRepository by inject()
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

        serviceScope.launch {
            while (true) {
                try {
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "Checking whether there are any files to download")
                        val filesToDownload = queueFileDownloadRepository.getFiles()

                        if (filesToDownload.isNotEmpty()) {
                            startForeground(NOTIFICATION_ID, notification.build())

                            filesToDownload.forEachIndexed { index, file ->
                                downloadFile(file)
                                updateNotification(notification, "${index + 1} / ${filesToDownload.size} is being downloaded")
                            }

                            stopForeground(STOP_FOREGROUND_DETACH)
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

    private suspend fun downloadFile(dataFileLinkId: UUID) {
        Log.i(TAG, "Started downloading $dataFileLinkId")

        fileManager.downloadFile(dataFileLinkId)
        queueFileDownloadRepository.deleteFile(dataFileLinkId)

        Log.i(TAG, "Finished downloading $dataFileLinkId")
    }

    private fun createChannel() {
        notificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun updateNotification(builder: NotificationCompat.Builder, updatedText: String) {
        val updatedNotification = builder.setContentText(updatedText).build()

        notificationManager.notify(NOTIFICATION_ID, updatedNotification)
    }

    private fun notificationBuilder(pendingIntent: PendingIntent) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("File downloading progress")
        .setSmallIcon(R.drawable.baseline_file_download_24)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setContentIntent(pendingIntent)
        .setSilent(true)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
    }

    companion object {

        const val NOTIFICATION_ID = 2
        const val CHANNEL_ID = "bytemedrive.filedownload"
        const val CHANNEL_NAME = "File Download"
    }
}