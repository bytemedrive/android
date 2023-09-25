package com.bytemedrive.wallet.payment.creditcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.store.AppState
import com.bytemedrive.wallet.root.StripePaymentRequest
import com.bytemedrive.wallet.root.WalletRepository
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodCreditCardViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    val clientSecret = MutableStateFlow<String?>(null)

    val gbm = MutableStateFlow("")

    private val confirmPaymentParams = MutableStateFlow<ConfirmPaymentIntentParams?>(null)

    fun makePayment() {
        viewModelScope.launch {
            val paymentIntent = walletRepository.stripePayment(AppState.customer.value?.wallet!!, StripePaymentRequest(gbm.value.toLong()))
            clientSecret.update { paymentIntent.clientSecret }
        }
    }

    fun onPaymentLaunched() {
        confirmPaymentParams.update { null }
    }

    fun handlePaymentResult(result: PaymentSheetResult, onCompleted: () -> Unit, onFailed: () -> Unit) {
        when(result) {
            PaymentSheetResult.Completed -> onCompleted()
            else -> onFailed()
        }
    }
}