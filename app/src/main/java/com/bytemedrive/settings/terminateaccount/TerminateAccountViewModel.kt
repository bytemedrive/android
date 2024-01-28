package com.bytemedrive.settings.terminateaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.datafile.control.DataFileRepository
import com.bytemedrive.file.root.UploadChunk
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.StoreRepository
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TerminateAccountViewModel(
    private val storeRepository: StoreRepository,
    private val walletRepository: WalletRepository,
    private val customerRepository: CustomerRepository,
    private val dataFileRepository: DataFileRepository
) : ViewModel() {

    val username = MutableStateFlow("")

    val password = MutableStateFlow("".toCharArray())

    val alertDialogAccountTerminated = MutableStateFlow(false)

    fun terminateAccount(onInvalidCredentials: () -> Unit) = viewModelScope.launch {
        customerRepository.getCustomer()?.let { customer ->
            customer.walletId?.let { walletId ->
                val dataFiles = dataFileRepository.getAllDataFiles()
                val usernameSha3 = ShaService.hashSha3(username.value)
                val credentialsSha3 = ShaService.hashSha3("${username.value}:${password.value.concatToString()}")
                val thumbnailChunkIds = dataFiles.flatMap { dataFile -> dataFile.thumbnails.flatMap { thumbnail -> thumbnail.chunks.map{ it.id.toString() } } }
                val dataFileChunkIds = dataFiles.flatMap { dataFile -> dataFile.chunks.map { it.id.toString() } }
                val allChunkIds = thumbnailChunkIds + dataFileChunkIds

                try {
                    storeRepository.deleteCustomer(usernameSha3, credentialsSha3)
                    walletRepository.deleteFiles(walletId, allChunkIds)
                    alertDialogAccountTerminated.update { true }
                } catch (e: Exception) {
                    onInvalidCredentials()
                }
            }
        }
    }
}