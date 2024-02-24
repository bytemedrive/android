package com.bytemedrive.file.shared.preview

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.shared.FileManager
import kotlinx.coroutines.launch
import java.io.File

class FilePreviewViewModel(
    private val dataFileRepository: DataFileRepository
) : ViewModel() {

    var loading by mutableStateOf(true)

    var thumbnails by mutableStateOf<List<Thumbnail>>(emptyList())

    var thumbnailIndex by mutableStateOf(0)

    fun initialize(filePreview: FilePreview, context: Context) = viewModelScope.launch {
        loading = true

        val dataFileLinks = dataFileRepository.getDataFileLinksByIds(filePreview.dataFileLinkIds)
        val dataFiles = dataFileRepository.getDataFilesByIds(dataFileLinks.map { it.dataFileId })

        thumbnails = dataFileLinks
            .sortedBy { filePreview.dataFileLinkIds.indexOf(it.id) }
            .mapIndexed { index, dataFileLink ->
                dataFiles
                    .find { it.id == dataFileLink.dataFileId }
                    ?.let { dataFile ->
                        val thumbnailDataFile = dataFile.thumbnails.find { thumbnail -> thumbnail.resolution == Resolution.P1280 }

                        if (dataFileLink.id == filePreview.initialDataFileLink.id) {
                            thumbnailIndex = index
                        }

                        thumbnailDataFile?.let {
                            val thumbnailName = FileManager.getThumbnailName(dataFile.id, thumbnailDataFile.resolution)
                            val filePath = "${context.filesDir}/$thumbnailName"
                            val file = File(filePath)

                            if (file.exists()) Thumbnail(file, dataFileLink.name, dataFile.contentType) else null
                        }
                    }
            }
            .filterNotNull()


        loading = false
    }

    class Thumbnail(val file: File, val name: String, val contentType: String?)
}