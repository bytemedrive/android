package com.bytemedrive.file.root

import com.bytemedrive.database.DatabaseManager
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

class QueueFileDownloadRepository(databaseManager: DatabaseManager) {

    private val TAG = QueueFileDownloadRepository::class.qualifiedName

    private var collection: Collection = databaseManager.database.createCollection(COLLECTION_NAME)

    fun getFiles() = queryFileUpload(collection).execute().allResults().map(::mapFileDownload)

    fun addFile(dataFileLinkId: UUID) {
        val doc = MutableDocument(dataFileLinkId.toString())

        collection.save(doc)
    }

    fun deleteFile(documentId: String) = collection.getDocument(documentId)?.let { document ->
        collection.delete(document)
    }

    private fun queryFileUpload(collection: Collection) =
        QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(collection))

    private fun mapFileDownload(result: Result) = result.getString("id")!!.let { UUID.fromString(it) }

    companion object {

        const val COLLECTION_NAME = "file_download_queue"
    }
}