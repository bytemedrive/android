package com.bytemedrive.file.starred.bottomsheet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.datafile.entity.DataFileLink
import kotlinx.coroutines.launch
import java.util.UUID

class StarredBottomSheetContextFileViewModel(
    private val dataFileRepository: DataFileRepository
): ViewModel() {
    var dataFileLink by mutableStateOf<DataFileLink?>(null)

    fun initialize(dataFileLinkId: UUID) = viewModelScope.launch{
        dataFileLink = dataFileRepository.getDataFileLinkById(dataFileLinkId)
    }
}