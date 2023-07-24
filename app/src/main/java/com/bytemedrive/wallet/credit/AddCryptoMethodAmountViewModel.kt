package com.bytemedrive.wallet.credit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.price.Prices
import com.bytemedrive.price.PricesRepository
import com.bytemedrive.store.AppState
import com.bytemedrive.wallet.root.MoneroPaymentRequest
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AddCryptoMethodAmountViewModel(private val pricesRepository: PricesRepository) : ViewModel() {

    val amount = MutableStateFlow<Int?>(null)

    val prices = MutableStateFlow<Prices?>(null)

    init {
        viewModelScope.launch {
            prices.value = pricesRepository.getPrices()
        }
    }
}