package com.bytemedrive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bytemedrive.application.encryptedSharedPreferences
import com.bytemedrive.customer.control.CustomerDao
import com.bytemedrive.customer.entity.CustomerEntity
import com.bytemedrive.datafile.control.DataFileDao
import com.bytemedrive.datafile.entity.DataFileEntity
import com.bytemedrive.datafile.entity.DataFileLinkEntity
import com.bytemedrive.file.root.FileDownloadDao
import com.bytemedrive.file.root.FileDownloadEntity
import com.bytemedrive.file.root.FileUploadDao
import com.bytemedrive.file.root.FileUploadEntity
import com.bytemedrive.folder.FolderDao
import com.bytemedrive.folder.FolderEntity
import com.bytemedrive.store.EventDao
import com.bytemedrive.store.EventEntity
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    version = 1,
    exportSchema = true,
    entities = [FileDownloadEntity::class, FileUploadEntity::class, EventEntity::class, CustomerEntity::class, DataFileEntity::class, DataFileLinkEntity::class, FolderEntity::class],
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class ByteMeDatabase : RoomDatabase() {

    abstract fun fileDownloadDao(): FileDownloadDao

    abstract fun fileUploadDao(): FileUploadDao

    abstract fun eventDao(): EventDao

    abstract fun customerDao(): CustomerDao

    abstract fun dataFileDao(): DataFileDao

    abstract fun folderDao(): FolderDao

    companion object {

        private const val DATABASE_NAME = "byteme"

        @Volatile
        private var instance: ByteMeDatabase? = null

        fun getInstance(context: Context): ByteMeDatabase = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context): ByteMeDatabase =
            Room.databaseBuilder(context, ByteMeDatabase::class.java, DATABASE_NAME)
//                .openHelperFactory(SupportOpenHelperFactory(encryptedSharedPreferences.getDbPassword()))
                .build()
    }
}