package com.bytemedrive.store

import com.bytemedrive.signup.EventCustomerSignedUp
import com.bytemedrive.upload.EventFileUploaded
import com.fasterxml.jackson.annotation.JsonValue

enum class EventType(@JsonValue val code: String, val clazz: Class<*>) {
    FILE_UPLOADED("file-uploaded", EventFileUploaded::class.java),
    CUSTOMER_SIGNED_UP("customer-signed-up", EventCustomerSignedUp::class.java);

    companion object {

        fun of(code: String): EventType {
            for (value in EventType.values()) {
                if (value.code == code) {
                    return value
                }
            }

            throw IllegalArgumentException("There is no EventType with name: $code")
        }

        fun of(clazz: Class<*>): EventType {
            for (value in EventType.values()) {
                if (value.clazz.isAssignableFrom(clazz)) {
                    return value
                }
            }

            throw IllegalArgumentException("There is no EventType with class: $clazz")
        }


    }
}