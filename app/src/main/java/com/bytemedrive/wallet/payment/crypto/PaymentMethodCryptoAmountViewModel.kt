package com.bytemedrive.wallet.payment.crypto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.price.Prices
import com.bytemedrive.price.PricesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PaymentMethodCryptoAmountViewModel(private val pricesRepository: PricesRepository) : ViewModel() {

    val amount = MutableStateFlow<Int?>(null)

    val prices = MutableStateFlow<Prices?>(null)

    init {
        viewModelScope.launch {
            prices.value = pricesRepository.getPrices()
        }
    }
}