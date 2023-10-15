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

class FileDownloadQueueRepository(private val databaseManager: DatabaseManager) {

    private val TAG = FileDownloadQueueRepository::class.qualifiedName

    private var collection: Collection = databaseManager.database.createCollection(COLLECTION_NAME)

    suspend fun getFiles() = withContext(Dispatchers.IO) {
        databaseManager.database.getCollection(COLLECTION_NAME)?.let { collection ->
            queryFileUpload(collection).execute().allResults().map(::mapFileDownload)
        }.orEmpty()
    }

    suspend fun addFile(dataFileLinkId: UUID) = withContext(Dispatchers.IO) {
        val doc = MutableDocument(dataFileLinkId.toString())

        collection.save(doc)
    }

    suspend fun deleteFile(documentId: String) = withContext(Dispatchers.IO) {
        collection.getDocument(documentId)?.let { document ->
            collection.delete(document)
        }
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