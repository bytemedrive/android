package com.bytemedrive.event

import com.bytemedrive.upload.EventFileUploaded
import com.fasterxml.jackson.annotation.JsonValue

enum class EventType(@JsonValue val code: String, val clazz: Class<*>) {
    FILE_UPLOADED("file-uploaded", EventFileUploaded::class.java);

    companion object {
        fun of(code: String): EventType {
            for (value in EventType.values()) {
                if (value.code == code) {
                    return value
                }
            }

            throw IllegalArgumentException("There is no EventType with name: $code")
        }
    }



}