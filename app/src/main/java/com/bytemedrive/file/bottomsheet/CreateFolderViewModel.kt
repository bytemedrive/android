package com.bytemedrive.file.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.folder.EventFolderCreated
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CreateFolderViewModel(private val eventPublisher: EventPublisher) : ViewModel() {

    val name = MutableStateFlow("")

    fun createFolder(onSuccess: () -> Unit) = viewModelScope.launch {
        eventPublisher.publishEvent(EventFolderCreated(UUID.randomUUID(), name.value))
        onSuccess()
    }
}