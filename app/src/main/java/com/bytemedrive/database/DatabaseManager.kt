package com.bytemedrive.database

import android.content.Context
import android.util.Log
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfigurationFactory
import com.couchbase.lite.newConfig

class DatabaseManager(val context: Context) {

    private val TAG = DatabaseManager::class.qualifiedName

    lateinit var database: Database

    init {
        CouchbaseLite.init(context)
        initializeDatabase(context)
        createCollections()
    }

    fun getCollectionFileDownload() = database.getCollection(COLLECTION_FILE_DOWNLOAD_QUEUE)

    fun getCollectionFileUpload() = database.getCollection(COLLECTION_FILE_UPLOAD_QUEUE)

    fun clearCollections() {
        Log.i(TAG, "Removing database collections and creating new ones")

        database.getCollection(COLLECTION_FILE_DOWNLOAD_QUEUE)?.let {
            database.deleteCollection(it.name, it.scope.name)
            database.createCollection(it.name, it.scope.name)
        }
        database.getCollection(COLLECTION_FILE_UPLOAD_QUEUE)?.let {
            database.deleteCollection(it.name, it.scope.name)
            database.createCollection(it.name, it.scope.name)
        }
    }

    private fun createCollections() {
        database.createCollection(COLLECTION_FILE_DOWNLOAD_QUEUE)
        database.createCollection(COLLECTION_FILE_UPLOAD_QUEUE)
    }

    private fun initializeDatabase(context: Context) {
        val config = DatabaseConfigurationFactory.newConfig(context.filesDir.toString())

        try {
            database = Database(DATABASE_NAME, config)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, e.stackTraceToString())
        }
    }

    companion object {

        const val DATABASE_NAME = "bytemedrive"
        const val COLLECTION_FILE_DOWNLOAD_QUEUE = "file_download_queue"
        const val COLLECTION_FILE_UPLOAD_QUEUE = "file_upload_queue"
    }
}