package com.bytemedrive.store

import com.bytemedrive.file.root.EventFileCopied
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.EventFileMoved
import com.bytemedrive.file.root.EventFileStarAdded
import com.bytemedrive.file.root.EventFileStarRemoved
import com.bytemedrive.file.root.EventFileUploadCompleted
import com.bytemedrive.file.root.EventFileUploadQueued
import com.bytemedrive.file.root.EventFileUploadStarted
import com.bytemedrive.file.root.EventThumbnailCompleted
import com.bytemedrive.file.root.EventThumbnailStarted
import com.bytemedrive.folder.EventFolderCopied
import com.bytemedrive.folder.EventFolderCreated
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderMoved
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.signup.EventCustomerSignedUp
import com.bytemedrive.wallet.payment.creditcode.EventCouponRedeemed
import com.fasterxml.jackson.annotation.JsonValue

enum class EventType(@JsonValue val code: String, val clazz: Class<*>) {
    COUPON_REDEEMED("coupon-redeemed", EventCouponRedeemed::class.java),
    CUSTOMER_SIGNED_UP("customer-signed-up", EventCustomerSignedUp::class.java),
    FILE_COPIED("file-copied", EventFileCopied::class.java),
    FILE_DELETED("file-deleted", EventFileDeleted::class.java),
    FILE_MOVED("file-moved", EventFileMoved::class.java),
    FILE_STAR_ADDED("file-star-added", EventFileStarAdded::class.java),
    FILE_STAR_DELETED("file-star-deleted", EventFileStarRemoved::class.java),
    FILE_UPLOADED_QUEUED("file-upload-queued", EventFileUploadQueued::class.java),
    FILE_UPLOADED_STARTED("file-upload-started", EventFileUploadStarted::class.java),
    FILE_UPLOADED_COMPLETED("file-upload-completed", EventFileUploadCompleted::class.java),
    FILE_THUMBNAIL_STARTED("file-thumbnail-started", EventThumbnailStarted::class.java),
    FILE_THUMBNAIL_COMPLETED("file-thumbnail-completed", EventThumbnailCompleted::class.java),
    FOLDER_COPIED("folder-copied", EventFolderCopied::class.java),
    FOLDER_CREATED("folder-created", EventFolderCreated::class.java),
    FOLDER_DELETED("folder-deleted", EventFolderDeleted::class.java),
    FOLDER_MOVED("folder-moved", EventFolderMoved::class.java),
    FOLDER_STAR_ADDED("folder-star-added", EventFolderStarAdded::class.java),
    FOLDER_STAR_DELETED("folder-star-deleted", EventFolderStarRemoved::class.java);

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