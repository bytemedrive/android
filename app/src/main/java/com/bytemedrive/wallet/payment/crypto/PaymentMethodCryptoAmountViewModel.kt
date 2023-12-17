package com.bytemedrive.wallet.payment.crypto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.price.Prices
import com.bytemedrive.price.PricesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodCryptoAmountViewModel(private val pricesRepository: PricesRepository) : ViewModel() {

    val amount = MutableStateFlow<Int?>(null)

    val prices = MutableStateFlow<Prices?>(null)

    val loading = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            loading.update { true }

            prices.update { pricesRepository.getPrices() }

            loading.update { false }
        }
    }
}