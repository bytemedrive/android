package com.bytemedrive.file.shared.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.datafile.entity.DataFile
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.shared.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class FilePreviewViewModel(
    private val dataFileRepository: DataFileRepository
) : ViewModel() {

    val loading = MutableStateFlow(true)

    val thumbnails = MutableStateFlow<List<Thumbnail>>(emptyList())

    val thumbnailIndex = MutableStateFlow<Int?>(null)

    fun initialize(filePreview: FilePreview, context: Context) = viewModelScope.launch {
        loading.update { true }

        val dataFiles = dataFileRepository.getDataFilesByIds(filePreview.dataFileIds)

        thumbnails.update {
            dataFiles
                .filter { dataFile -> dataFile.contentType == MimeTypes.IMAGE_JPEG }
                .mapIndexed { index, dataFile_ ->
                    val thumbnailDataFile = dataFile_.thumbnails.find { thumbnail -> thumbnail.resolution == Resolution.P1280 }

                    if (dataFile_.id == filePreview.initialDataFile.id) {
                        thumbnailIndex.update { index }
                    }

                    thumbnailDataFile?.let {
                        val thumbnailName = FileManager.getThumbnailName(dataFile_.id, thumbnailDataFile.resolution)
                        val filePath = "${context.filesDir}/$thumbnailName"
                        val file = File(filePath)

                        if (file.exists()) Thumbnail(BitmapFactory.decodeFile("${context.filesDir}/$thumbnailName"), dataFile_) else null
                    }
                }
                .filterNotNull()
        }

        loading.update { false }
    }

    class Thumbnail(val bitmap: Bitmap, val dataFile: DataFile)
}