package com.bytemedrive.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bytemedrive.file.root.QueueFileUploadRepository

class ServiceManager {
    private val TAG = ServiceManager::class.qualifiedName

    fun startServices(context: Context) {
        Log.i(TAG, "Starting services")
        context.startService(Intent(context, ServiceFileUpload::class.java))
        context.startService(Intent(context, ServiceFileDownload::class.java))
        context.startService(Intent(context, ServiceThumbnailCreate::class.java))
        context.startService(Intent(context, ServiceThumbnailDownload::class.java))
    }

    fun stopServices(context: Context) {
        Log.i(TAG, "Stopping services")

        context.stopService(Intent(context, ServiceFileUpload::class.java))
        context.stopService(Intent(context, ServiceFileDownload::class.java))
        context.stopService(Intent(context, ServiceThumbnailCreate::class.java))
        context.stopService(Intent(context, ServiceThumbnailDownload::class.java))
    }
}