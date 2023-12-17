package com.bytemedrive.wallet.payment.creditcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class PaymentMethodCreditCodeViewModel(private val walletRepository: WalletRepository, private val eventPublisher: EventPublisher) : ViewModel() {

    var code = MutableStateFlow("")

    val loading = MutableStateFlow(false)

    fun redeemCoupon(walletId: UUID, couponCode: String) = viewModelScope.launch {
        loading.update { true }

        walletRepository.redeemCoupon(walletId, couponCode)
        eventPublisher.publishEvent(EventCouponRedeemed(walletId, couponCode))

        loading.update { false }
    }
}