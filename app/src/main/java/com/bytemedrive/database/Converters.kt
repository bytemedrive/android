package com.bytemedrive.database

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun convertZonedDateTimeSerializerToString(zonedDateTime: ZonedDateTime?): String? = zonedDateTime?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

    @TypeConverter
    fun convertStringToZonedDateTimeSerializer(value: String?): ZonedDateTime? = value?.let {
        ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }
}