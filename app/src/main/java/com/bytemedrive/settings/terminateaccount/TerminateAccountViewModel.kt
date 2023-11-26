package com.bytemedrive.settings.terminateaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.store.AppState
import com.bytemedrive.store.StoreRepository
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
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
        val walletId = AppState.customer.value?.wallet!!
        val thumbnailChunkIds =
            AppState.customer.value?.dataFiles?.flatMap { dataFile -> dataFile.thumbnails.flatMap { thumbnail -> thumbnail.chunksIds.map { it.toString() } } }.orEmpty()
        val dataFileChunkIds = AppState.customer.value?.dataFiles?.flatMap { dataFile -> dataFile.chunksIds.map { it.toString() } }.orEmpty()
        val allChunkIds = thumbnailChunkIds + dataFileChunkIds

        try {
            storeRepository.deleteCustomer(usernameSha3, credentialsSha3)
            walletRepository.deleteFiles(walletId, allChunkIds)
            alertDialogAccountTerminated.value = true
        } catch (e: Exception) {
            onInvalidCredentials()
        }
    }
}