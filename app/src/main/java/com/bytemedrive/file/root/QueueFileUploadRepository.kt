package com.bytemedrive.file.root

import com.bytemedrive.database.DatabaseManager
import com.bytemedrive.database.FileUpload
import com.bytemedrive.network.JsonConfig
import com.couchbase.lite.Collection
import com.couchbase.lite.DataSource
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import com.couchbase.lite.queryChangeFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

class QueueFileUploadRepository(databaseManager: DatabaseManager) {

    private val TAG = QueueFileUploadRepository::class.qualifiedName

    private val collection = databaseManager.database.createCollection(COLLECTION_NAME)

    fun watchFiles() = queryFileUpload(collection).queryChangeFlow().mapNotNull { change ->
        val err = change.error

        if (err != null) {
            throw err
        }

        change.results?.allResults()?.map(::mapFileUpload).orEmpty()
    }

    fun getFiles() = queryFileUpload(collection).execute().allResults().map(::mapFileUpload)

    fun addFile(document: FileUpload) {
        val json = JsonConfig.mapper.writeValueAsString(document)
        val doc = MutableDocument()
        doc.setJSON(json)

        collection.save(doc)
    }

    fun deleteFile(documentId: String) {
        collection.getDocument(documentId)?.let { document ->
            collection.delete(document)
        }
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
        val id = result.getString("id")!!
        val name = result.getString("name")!!
        val path = result.getString("path")!!
        val folderId = result.getString("folderId")

        return FileUpload(id, name, path, folderId)
    }

    companion object {

        const val COLLECTION_NAME = "file_upload_queue"
    }
}