package com.bytemedrive.file.root

import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.database.FileUpload
import com.bytemedrive.file.shared.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID

class UploadViewModel(
    private val queueFileUploadRepository: QueueFileUploadRepository,
) : ViewModel() {

    fun uploadFile(inputStream: InputStream, documentFile: DocumentFile, cacheDir: File, folderId: UUID?) = viewModelScope.launch {
        val dataFileId = UUID.randomUUID()
        val tmpOriginalFile = withContext(Dispatchers.IO) {
            File.createTempFile(dataFileId.toString(), ".${MimeTypeMap.getSingleton().getExtensionFromMimeType(documentFile.type)}", cacheDir)
        }

        inputStream.use {
            inputStream.copyTo(tmpOriginalFile.outputStream(), FileManager.BUFFER_SIZE)
        }

        queueFileUploadRepository.addFile(FileUpload(dataFileId, documentFile.name.orEmpty(), tmpOriginalFile.absolutePath, folderId))
    }
}