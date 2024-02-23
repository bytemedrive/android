package com.bytemedrive.wallet.payment.crypto

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.price.Prices
import com.bytemedrive.price.PricesRepository
import kotlinx.coroutines.launch

class PaymentMethodCryptoAmountViewModel(private val pricesRepository: PricesRepository) : ViewModel() {

    var amount by mutableStateOf<Long?>(null)

    var prices by mutableStateOf<Prices?>(null)

    var loading by mutableStateOf(false)

    init {
        viewModelScope.launch {
            loading = true

            prices = pricesRepository.getPrices()

            loading = false
        }
    }

    fun priceConversion(): Double = prices?.let { prices -> amount?.times(prices.gbmPriceInXmr) } ?: 0.0
}