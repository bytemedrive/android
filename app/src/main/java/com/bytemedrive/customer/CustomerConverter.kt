package com.bytemedrive.customer

import com.bytemedrive.event.Event
import com.bytemedrive.file.File
import com.bytemedrive.upload.EventFileUploaded

object CustomerConverter {

    fun convert(event: Event<*>) {
        when(event.data) {
            is EventFileUploaded -> applyEvent(event.data)
        }
    }

    private fun applyEvent(data: EventFileUploaded) {
        Customer.files.add(File(data.fileName, data.fileSizeBytes))
    }
}