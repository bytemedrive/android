package com.bytemedrive.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.file.root.FileViewModel
import com.bytemedrive.file.starred.StarredViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TopBarViewModel(
    fileViewModel: FileViewModel,
    starredViewModel: StarredViewModel
) : ViewModel() {

    val barType = MutableStateFlow(BarType.SCREEN)

    init {
        viewModelScope.launch {
            combine(
                fileViewModel.fileAndFolderSelected,
                starredViewModel.fileAndFolderSelected,
            ) { fileAndFolderSelectedRoot,
                fileAndFolderSelectedStarred ->

                when {
                    fileAndFolderSelectedRoot.isNotEmpty() -> BarType.SELECTION_FILE
                    fileAndFolderSelectedStarred.isNotEmpty() -> BarType.SELECTION_STARRED
                    else -> BarType.SCREEN
                }
            }.collect { item -> barType.value = item }
        }
    }
}

enum class BarType(type: String) {
    SCREEN("screen"),
    SELECTION_STARRED("selectionStarred"),
    SELECTION_FILE("selectionFile"),
}