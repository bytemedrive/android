package com.bytemedrive.store

import com.bytemedrive.file.root.EventThumbnailUploaded
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.EventFileMoved
import com.bytemedrive.file.root.EventFileStarAdded
import com.bytemedrive.file.root.EventFileStarRemoved
import com.bytemedrive.file.root.EventFileUploaded
import com.bytemedrive.folder.EventFolderCreated
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.EventFolderMoved
import com.bytemedrive.folder.EventFolderStarAdded
import com.bytemedrive.folder.EventFolderStarRemoved
import com.bytemedrive.signup.EventCustomerSignedUp
import com.bytemedrive.wallet.EventCouponRedeemed
import com.fasterxml.jackson.annotation.JsonValue

enum class EventType(@JsonValue val code: String, val clazz: Class<*>) {
    COUPON_REDEEMED("coupon-redeemed", EventCouponRedeemed::class.java),
    CUSTOMER_SIGNED_UP("customer-signed-up", EventCustomerSignedUp::class.java),
    FILE_DELETED("file-deleted", EventFileDeleted::class.java),
    FILE_MOVED("file-moved", EventFileMoved::class.java),
    FILE_STAR_ADDED("file-star-added", EventFileStarAdded::class.java),
    FILE_STAR_DELETED("file-star-deleted", EventFileStarRemoved::class.java),
    FILE_UPLOADED("file-uploaded", EventFileUploaded::class.java),
    FILE_THUMBNAIL_UPLOADED("file-thumbnail-uploaded", EventThumbnailUploaded::class.java),
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