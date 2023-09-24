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
    }
}