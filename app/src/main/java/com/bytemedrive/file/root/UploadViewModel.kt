package com.bytemedrive.file.root

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
    private val fileUploadQueueRepository: FileUploadQueueRepository,
) : ViewModel() {

    fun uploadFile(inputStream: InputStream, documentFile: DocumentFile, cacheDir: File, folderId: String?, onComplete: () -> Unit) = viewModelScope.launch {
        val dataFileId = UUID.randomUUID()
        val tmpOriginalFile = withContext(Dispatchers.IO) {
            File.createTempFile(dataFileId.toString(), null, cacheDir)
        }

        inputStream.use {
            inputStream.copyTo(tmpOriginalFile.outputStream(), FileManager.BUFFER_SIZE)
        }

        fileUploadQueueRepository.addFile(
            FileUpload(
                dataFileId.toString(),
                documentFile.name.orEmpty(),
                tmpOriginalFile.absolutePath,
                folderId
            )
        )

        onComplete()
    }
}