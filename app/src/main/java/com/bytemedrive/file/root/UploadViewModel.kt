package com.bytemedrive.file.root

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.folder.Folder
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.time.ZonedDateTime
import java.util.UUID

class UploadViewModel(
    private val externalScope: CoroutineScope,
    private val eventPublisher: EventPublisher,
    private val folderRepository: FolderRepository,
    private val dataFileRepository: DataFileRepository,
    ) : ViewModel() {

    var folders by mutableStateOf(emptyList<Folder>())

    init {
        viewModelScope.launch {
            folders = folderRepository.getAllFolders()
        }
    }

    fun uploadFile(inputStream: InputStream, documentFile: DocumentFile, cacheDir: File, folderId: UUID?) = externalScope.launch {
        val dataFileId = UUID.randomUUID()
        val tmpOriginalFile = withContext(Dispatchers.IO) {
            val tmpOriginalFile = File.createTempFile(dataFileId.toString(), ".${documentFile.name?.split(".")?.last() ?: "bin"}", cacheDir)

            tmpOriginalFile.outputStream().use { outputStream ->
                inputStream.use { inputStream -> inputStream.copyTo(outputStream, FileManager.BUFFER_SIZE_DEFAULT) }
            }

            tmpOriginalFile
        }
        val checksum = ShaService.checksum(tmpOriginalFile.inputStream())
        val sameDataFile = dataFileRepository.getDataFileByChecksum(checksum)

        if (sameDataFile == null) {
            eventPublisher.publishEvent(
                EventFileUploadQueued(
                    dataFileId,
                    documentFile.name.orEmpty(),
                    documentFile.length(),
                    UUID.randomUUID(),
                    ZonedDateTime.now(),
                    tmpOriginalFile.absolutePath,
                    folderId
                )
            )
        } else {
            eventPublisher.publishEvent(EventFileCopied(sameDataFile.id, UUID.randomUUID(), folderId, "Copy of ${sameDataFile.name}"))
        }
    }
}