package com.bytemedrive.database

import androidx.room.TypeConverter
import com.bytemedrive.datafile.entity.Thumbnail
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.file.root.UploadChunk
import com.bytemedrive.network.JsonConfig.mapper
import com.bytemedrive.store.EventType
import com.fasterxml.jackson.core.type.TypeReference
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun convertZonedDateTime(zonedDateTime: ZonedDateTime?): String? = zonedDateTime?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

    @TypeConverter
    fun convertZonedDateTime(value: String?): ZonedDateTime? = value?.let {
        ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    @TypeConverter
    fun convertUploadStatus(value: String): UploadStatus = UploadStatus.valueOf(value)

    @TypeConverter
    fun convertUploadStatus(value: UploadStatus): String = value.name

    @TypeConverter
    fun convertEventType(value: String): EventType = EventType.valueOf(value)

    @TypeConverter
    fun convertEventType(value: EventType): String = value.name

    @TypeConverter
    fun convertUploadChunks(data: List<UploadChunk>): String = mapper.writeValueAsString(data)

    @TypeConverter
    fun convertUploadChunks(json: String) = mapper.readValue(json, object : TypeReference<List<UploadChunk>>() {})

    @TypeConverter
    fun convertThumbnails(data: List<Thumbnail>): String = mapper.writeValueAsString(data)

    @TypeConverter
    fun convertThumbnails(json: String) = mapper.readValue(json, object : TypeReference<List<Thumbnail>>() {})
}