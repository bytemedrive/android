package com.bytemedrive.store

import com.bytemedrive.file.EventFileDeleted
import com.bytemedrive.file.EventFileUploaded
import com.bytemedrive.folder.EventFolderCreated
import com.bytemedrive.signup.EventCustomerSignedUp
import com.fasterxml.jackson.annotation.JsonValue

enum class EventType(@JsonValue val code: String, val clazz: Class<*>) {
    FILE_DELETED("file-deleted", EventFileDeleted::class.java),
    FILE_UPLOADED("file-uploaded", EventFileUploaded::class.java),
    FOLDER_CREATED("folder-created", EventFolderCreated::class.java),
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