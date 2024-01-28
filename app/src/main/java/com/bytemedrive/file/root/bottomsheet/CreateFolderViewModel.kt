package com.bytemedrive.file.root.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.EventFolderCreated
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CreateFolderViewModel(
    private val externalScope: CoroutineScope,
    private val eventPublisher: EventPublisher
) : ViewModel() {

    val name = MutableStateFlow("")

    fun createFolder(folderId: UUID?) = externalScope.launch {
        eventPublisher.publishEvent(EventFolderCreated(UUID.randomUUID(), name.value, parent = folderId))
    }
}