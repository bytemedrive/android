package com.bytemedrive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bytemedrive.application.encryptedSharedPreferences
import com.bytemedrive.file.root.FileUploadDao
import com.bytemedrive.file.root.FileUploadEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(version = 1, exportSchema = true, entities = [DataFileLinkEntity::class, FileUploadEntity::class])
abstract class ByteMeDatabase : RoomDatabase() {

    abstract fun dataFileLinkDao(): DataFileLinkDao

    abstract fun fileUploadDao(): FileUploadDao

    companion object {

        private const val DATABASE_NAME = "byteme"
        fun newInstance(context: Context) =
            Room.databaseBuilder(context, ByteMeDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(SupportOpenHelperFactory(encryptedSharedPreferences.getDbPassword()))
                .build()
    }
}