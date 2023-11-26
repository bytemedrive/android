package com.bytemedrive.service

import android.content.Context
import android.content.Intent

class ServiceManager {

    fun startServices(context: Context) {
        context.startService(Intent(context, ServiceFileUpload::class.java))
        context.startService(Intent(context, ServiceFileDownload::class.java))
        context.startService(Intent(context, ServiceThumbnailCreate::class.java))
        context.startService(Intent(context, ServiceThumbnailDownload::class.java))
    }

    fun stopServices(context: Context) {
        context.stopService(Intent(context, ServiceFileUpload::class.java))
        context.stopService(Intent(context, ServiceFileDownload::class.java))
        context.stopService(Intent(context, ServiceThumbnailCreate::class.java))
        context.stopService(Intent(context, ServiceThumbnailDownload::class.java))
    }
}