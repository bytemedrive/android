package com.bytemedrive.file.root

import android.util.Log
import com.bytemedrive.database.DatabaseManager
import com.bytemedrive.database.FileUpload
import com.couchbase.lite.Collection
import com.couchbase.lite.DataSource
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class QueueFileUploadRepository(private val databaseManager: DatabaseManager) {

    private val TAG = QueueFileUploadRepository::class.qualifiedName

    suspend fun getFiles() = withContext(Dispatchers.IO) {
        databaseManager.getCollectionFileUpload()?.let { collection ->
            queryFileUpload(collection).execute().use { resultSet -> resultSet.allResults().map(::mapFileUpload) }
        }.orEmpty()
    }

    suspend fun addFile(fileUpload: FileUpload) = withContext(Dispatchers.IO) {
        databaseManager.getCollectionFileUpload()?.let { collection ->
            Log.i(TAG, "Adding file ${fileUpload.name} to uploading queue")

            val document = MutableDocument(fileUpload.id.toString()).let {
                it.setString("name", fileUpload.name)
                it.setString("path", fileUpload.path)
                it.setString("folderId", fileUpload.folderId?.toString())
            }

            collection.save(document)
        }
    }

    suspend fun deleteFile(documentId: String) = withContext(Dispatchers.IO) {
        databaseManager.getCollectionFileUpload()?.purge(documentId)
    }

    private fun queryFileUpload(collection: Collection) = QueryBuilder
        .select(
            SelectResult.expression(Meta.id),
            SelectResult.property("name"),
            SelectResult.property("path"),
            SelectResult.property("folderId")
        )
        .from(DataSource.collection(collection))

    private fun mapFileUpload(result: Result): FileUpload {
        val id = result.getString("id")!!.let { UUID.fromString(it) }
        val name = result.getString("name")!!
        val path = result.getString("path")!!
        val folderId = result.getValue("folderId")?.let { UUID.fromString(it.toString()) }

        return FileUpload(id, name, path, folderId)
    }
}