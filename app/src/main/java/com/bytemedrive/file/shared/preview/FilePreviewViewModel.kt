package com.bytemedrive.file.shared.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import com.bytemedrive.file.root.DataFile
import com.bytemedrive.file.root.Resolution
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.privacy.AesService
import com.bytemedrive.store.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FilePreviewViewModel(
    private val fileManager: FileManager,
) : ViewModel() {

    val loading = MutableStateFlow(true)

    val thumbnails = MutableStateFlow<List<Thumbnail>>(emptyList())

    val thumbnailIndex = MutableStateFlow<Int?>(null)

    fun getThumbnails(initialDataFileId: UUID, dataFileIds: List<UUID>, context: Context) = viewModelScope.launch {
        loading.value = true

        thumbnails.value = AppState.customer.value?.dataFiles
            ?.filter { dataFile -> dataFileIds.contains(dataFile.id) && dataFile.contentType == MimeTypes.IMAGE_JPEG }
            ?.mapIndexed { index, dataFile_ ->
                val thumbnailDataFile = dataFile_.thumbnails.find { thumbnail -> thumbnail.resolution == Resolution.P1280 }

                if (dataFile_.id == initialDataFileId) {
                    thumbnailIndex.value = index
                }

                thumbnailDataFile?.let {
                    val encryptedFile = fileManager.rebuildFile(it.chunksViewIds, "${dataFile_.id}-thumbnail-${it.resolution}-encrypted", it.contentType, context.cacheDir)
                    val fileDecrypted = AesService.decryptWithKey(encryptedFile.readBytes(), it.secretKey)
                    val bitmap = BitmapFactory.decodeByteArray(fileDecrypted, 0, fileDecrypted.size)

                    Thumbnail(bitmap, dataFile_)
                }
            }
            ?.filterNotNull()
            .orEmpty()

        loading.value = false
    }

    class Thumbnail(val bitmap: Bitmap, val dataFile: DataFile)
}