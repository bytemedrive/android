package com.bytemedrive.file.starred

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MimeTypes
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.EventFileDeleted
import com.bytemedrive.file.root.FileRepository
import com.bytemedrive.file.shared.FileManager
import com.bytemedrive.file.shared.control.FileListItemRepository
import com.bytemedrive.file.shared.entity.FileListItem
import com.bytemedrive.file.shared.entity.ItemType
import com.bytemedrive.file.shared.preview.FilePreview
import com.bytemedrive.folder.EventFolderDeleted
import com.bytemedrive.folder.FolderManager
import com.bytemedrive.folder.FolderRepository
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.EventPublisher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class StarredViewModel(
    private val appNavigator: AppNavigator,
    private val eventPublisher: EventPublisher,
    private val fileRepository: FileRepository,
    private val fileManager: FileManager,
    private val folderManager: FolderManager,
    private val dataFileRepository: DataFileRepository,
    private val folderRepository: FolderRepository,
    private val customerRepository: CustomerRepository,
    private val fileListItemRepository: FileListItemRepository
) : ViewModel() {

    var fileListItems = fileListItemRepository.getAllStaredPaged()

    var starred = MutableStateFlow(listOf<FileListItem>())

    val itemsSelected = MutableStateFlow(emptyList<FileListItem>())

    val dataFilePreview = MutableStateFlow<FilePreview?>(null)

    private var watchJob: Job? = null

    fun init() {
        watchItems()
    }

    fun clickFileAndFolder(item: FileListItem) {
        val anyFileSelected = itemsSelected.value.isNotEmpty()

        if (anyFileSelected) {
            longClickFileAndFolder(item)
        } else {
            when (item.type) {
                ItemType.FOLDER -> appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to item.id.toString()))

                ItemType.FILE -> {
                    viewModelScope.launch {
                        dataFileRepository.getDataFileLinkById(item.id)?.let { dataFileLink ->
                            val dataFile = dataFileRepository.getDataFileById(dataFileLink.dataFileId)
                            val dataFileIdsStarred = dataFileRepository.getDataFileLinksStarred(starred = true).map { it.dataFileId }

                            if (dataFile?.contentType == MimeTypes.IMAGE_JPEG) {
                                dataFilePreview.update { FilePreview(dataFile, dataFileIdsStarred) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun longClickFileAndFolder(item: FileListItem) =
        itemsSelected.update {
            if (it.contains(item)) {
                itemsSelected.value - item
            } else {
                itemsSelected.value + item
            }
        }

    fun clearSelectedItems() = itemsSelected.update { emptyList() }

    fun removeItems(ids: List<UUID>) = viewModelScope.launch {
        val dataFileLinks = dataFileRepository.getAllDataFileLinks()
        val folders = folderRepository.getAllFolders()

        customerRepository.getCustomer()?.let { customer ->
            dataFileLinks.filter { ids.contains(it.id) }.map { dataFileLink ->
                val physicalFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                if (physicalFileRemovable && customer.walletId != null) {
                    fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                }

                dataFileLink.id
            }.takeIf { it.isNotEmpty() }?.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

            // TODO: Remove all data file links and files in deleted folder at once in one EventFolderDeleted
            folders.filter { ids.contains(it.id) }.map { folder ->
                fileManager.findAllFilesRecursively(folder.id, folders, dataFileLinks).map { dataFileLink ->
                    val physicalFileRemovable = dataFileLinks.none { it.id == dataFileLink.id }

                    if (physicalFileRemovable && customer.walletId != null) {
                        fileRepository.remove(customer.walletId, dataFileLink.dataFileId)
                    }

                    dataFileLink.id
                }.takeIf { it.isNotEmpty() }?.let { filesToRemove -> eventPublisher.publishEvent(EventFileDeleted(filesToRemove)) }

                (folderManager.findAllFoldersRecursively(folder.id, folders) + folder).map { it.id }
            }.flatten().takeIf { it.isNotEmpty() }?.let { foldersToRemove -> eventPublisher.publishEvent(EventFolderDeleted(foldersToRemove)) }
        }
    }

    fun toggleAllItems(context: Context) {
        if (itemsSelected.value.size == starred.value.size) {
            itemsSelected.update { emptyList() }
        } else {
            itemsSelected.update { starred.value }
            Toast.makeText(context, "${starred.value.size} items selected", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs() {
        watchJob?.cancel()
    }

    private fun watchItems() {
        watchJob = viewModelScope.launch {
            combine(folderRepository.getAllFoldersFlow(starred = true), dataFileRepository.getDataFileLinksStarredFlow(starred = true)) { folders, dataFileLinks ->
                val tempFolders = folders.map { FileListItem(it.id, it.name, ItemType.FOLDER, it.starred, false) }
                val tempFileLinks = dataFileLinks.map { FileListItem(it.id, it.name, ItemType.FILE, it.starred, false) }

                tempFolders + tempFileLinks
            }.collectLatest { items ->
                starred.update { items }
            }
        }
    }
}