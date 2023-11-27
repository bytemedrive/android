package com.bytemedrive.wallet.payment.creditcard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.wallet.root.StripePaymentRequest
import com.bytemedrive.wallet.root.WalletRepository
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodCreditCardViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    private val TAG = PaymentMethodCreditCardViewModel::class.qualifiedName

    val clientSecret = MutableStateFlow<String?>(null)

    val gbm = MutableStateFlow("")

    private val confirmPaymentParams = MutableStateFlow<ConfirmPaymentIntentParams?>(null)

    fun makePayment() {
        viewModelScope.launch {
            Log.i(TAG, "Creating payment intent for wallet id=${AppState.customer.value?.wallet!!} and gbm=${gbm.value.toLong()}")
            val paymentIntent = walletRepository.stripePayment(AppState.customer.value?.wallet!!, StripePaymentRequest(gbm.value.toLong()))
            Log.i(TAG, "Payment intent created. Price eur=${paymentIntent.priceEur}, client secret isEmpty=${paymentIntent.clientSecret.isEmpty()}")
            clientSecret.update { paymentIntent.clientSecret }
        }
    }

    fun onPaymentLaunched() {
        Log.i(TAG, "Called on payment launched")
        confirmPaymentParams.update { null }
    }

    fun handlePaymentResult(result: PaymentSheetResult, onCompleted: () -> Unit, onFailed: () -> Unit) {
        Log.i(TAG, "Payment result=$result")

        when(result) {
            PaymentSheetResult.Completed -> onCompleted()
            else -> onFailed()
        }
    }
}