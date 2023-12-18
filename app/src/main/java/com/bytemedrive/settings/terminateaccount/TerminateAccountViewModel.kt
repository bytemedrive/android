package com.bytemedrive.settings.terminateaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val walletRepository: WalletRepository
) : ViewModel() {

    val username = MutableStateFlow("")

    val password = MutableStateFlow("".toCharArray())

    val alertDialogAccountTerminated = MutableStateFlow(false)

    fun terminateAccount(onInvalidCredentials: () -> Unit) = viewModelScope.launch {
        val usernameSha3 = ShaService.hashSha3(username.value)
        val credentialsSha3 = ShaService.hashSha3("${username.value}:${password.value.concatToString()}")
        val walletId = AppState.customer?.wallet!!
        val thumbnailChunkIds = AppState.customer!!.dataFiles.value.flatMap { dataFile -> dataFile.thumbnails.flatMap { thumbnail -> thumbnail.chunks.map{ it.id.toString() } } }
        val dataFileChunkIds = AppState.customer!!.dataFiles.value.flatMap { dataFile -> dataFile.chunks.map { it.id.toString() } }
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