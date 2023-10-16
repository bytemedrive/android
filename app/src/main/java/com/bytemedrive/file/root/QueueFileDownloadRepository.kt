package com.bytemedrive.file.root

import com.bytemedrive.database.DatabaseManager
import com.couchbase.lite.Collection
import com.couchbase.lite.DataSource
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import java.util.UUID

class QueueFileDownloadRepository(private val databaseManager: DatabaseManager) {

    private val TAG = QueueFileDownloadRepository::class.qualifiedName

    fun getFiles(): List<UUID> = databaseManager.getCollectionFileDownload()?.let { collection ->
        queryFileUpload(collection).execute().use { resultSet -> resultSet.allResults().map(::mapFileDownload) }
    }.orEmpty()

    fun addFile(dataFileLinkId: UUID) = databaseManager.getCollectionFileDownload()?.let { collection ->
        val doc = MutableDocument(dataFileLinkId.toString())

        collection.save(doc)
    }

    fun deleteFile(documentId: String) = databaseManager.getCollectionFileDownload()?.let { collection ->
        collection.getDocument(documentId)?.let { document ->
            collection.delete(document)
        }
    }

    private fun queryFileUpload(collection: Collection) =
        QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(collection))

    private fun mapFileDownload(result: Result) = result.getString("id")!!.let { UUID.fromString(it) }
}