package com.bytemedrive.file.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.EventFolderCreated
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CreateFolderViewModel(private val eventPublisher: EventPublisher) : ViewModel() {

    val name = MutableStateFlow("")

    fun createFolder(folderId: String?, onSuccess: () -> Unit) = viewModelScope.launch {
        AppState.customer.value!!.folders.find { it.id.toString() == folderId }.let { folder ->
            eventPublisher.publishEvent(EventFolderCreated(UUID.randomUUID(), name.value, folder?.id))
            onSuccess()
        }
    }
}