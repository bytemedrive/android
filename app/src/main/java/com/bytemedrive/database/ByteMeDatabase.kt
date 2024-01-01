package com.bytemedrive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bytemedrive.application.encryptedSharedPreferences
import com.bytemedrive.file.root.FileDownloadDao
import com.bytemedrive.file.root.FileDownloadEntity
import com.bytemedrive.file.root.FileUploadDao
import com.bytemedrive.file.root.FileUploadEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.nio.charset.StandardCharsets

@Database(version = 1, exportSchema = true, entities = [FileDownloadEntity::class, FileUploadEntity::class])
@TypeConverters(Converters::class)
abstract class ByteMeDatabase : RoomDatabase() {

    abstract fun fileDownloadDao(): FileDownloadDao

    abstract fun fileUploadDao(): FileUploadDao

    companion object {

        private const val DATABASE_NAME = "byteme"
        fun newInstance(context: Context) =
            Room.databaseBuilder(context, ByteMeDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(SupportOpenHelperFactory(encryptedSharedPreferences.getDbPassword()))
                .build()
    }
}