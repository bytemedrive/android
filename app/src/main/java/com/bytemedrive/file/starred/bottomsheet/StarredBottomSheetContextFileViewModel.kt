package com.bytemedrive.file.starred.bottomsheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.datafile.entity.DataFileLink
import com.bytemedrive.file.root.EventFileStarAdded
import com.bytemedrive.file.root.EventFileStarRemoved
import com.bytemedrive.file.root.QueueFileDownloadRepository
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

class StarredBottomSheetContextFileViewModel(
    private val externalScope: CoroutineScope,
    private val dataFileRepository: DataFileRepository,
    private val fileManager: FileManager,
    private val queueFileDownloadRepository: QueueFileDownloadRepository,
    private val eventPublisher: EventPublisher,
): ViewModel() {
    var dataFileLink by mutableStateOf<DataFileLink?>(null)

    fun initialize(dataFileLinkId: UUID) = viewModelScope.launch{
        dataFileLink = dataFileRepository.getDataFileLinkById(dataFileLinkId)
    }

    fun removeFile(dataFileLinkId: UUID) = externalScope.launch {
        fileManager.removeFile(dataFileLinkId)
    }

    fun downloadFile(id: UUID) = viewModelScope.launch {
        queueFileDownloadRepository.addFile(id)
    }

    fun toggleStarredFile(id: UUID, value: Boolean) = externalScope.launch {
        when (value) {
            true -> eventPublisher.publishEvent(EventFileStarRemoved(id))
            false -> eventPublisher.publishEvent(EventFileStarAdded(id))
        }
    }
}