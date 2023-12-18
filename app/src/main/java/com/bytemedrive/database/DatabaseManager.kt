package com.bytemedrive.database

import android.content.Context
import android.util.Log
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfigurationFactory
import com.couchbase.lite.newConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseManager(private val context: Context) {

    private val TAG = DatabaseManager::class.qualifiedName

    private val coroutineScope: CoroutineScope = MainScope()

    private var database: Database? = null

    init {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                CouchbaseLite.init(context)
                initializeDatabase()
            }
        }
    }

    fun getCollectionFileDownload() = database?.getCollection(COLLECTION_FILE_DOWNLOAD_QUEUE)

    fun getCollectionFileUpload() = database?.getCollection(COLLECTION_FILE_UPLOAD_QUEUE)

    fun clearCollections() {
        Log.i(TAG, "Removing database collections and creating new ones")

        database?.let { database_ ->
            database_.getCollection(COLLECTION_FILE_DOWNLOAD_QUEUE)?.let {
                database_.deleteCollection(it.name, it.scope.name)
                database_.createCollection(it.name, it.scope.name)
            }
            database_.getCollection(COLLECTION_FILE_UPLOAD_QUEUE)?.let {
                database_.deleteCollection(it.name, it.scope.name)
                database_.createCollection(it.name, it.scope.name)
            }
        }
    }


    private fun initializeDatabase() {
        val config = DatabaseConfigurationFactory.newConfig(context.filesDir.toString())

        try {
            database = Database(DATABASE_NAME, config)
        } catch (e: CouchbaseLiteException) {
            Log.e(TAG, e.stackTraceToString())
        }

        createCollections()
    }

    private fun createCollections() {
        database?.createCollection(COLLECTION_FILE_DOWNLOAD_QUEUE)
        database?.createCollection(COLLECTION_FILE_UPLOAD_QUEUE)
    }

    companion object {

        const val DATABASE_NAME = "bytemedrive"
        const val COLLECTION_FILE_DOWNLOAD_QUEUE = "file_download_queue"
        const val COLLECTION_FILE_UPLOAD_QUEUE = "file_upload_queue"
    }
}