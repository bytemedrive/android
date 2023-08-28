package com.bytemedrive.wallet.payment.creditcard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.stripe.TokenProvider
import com.bytemedrive.wallet.payment.creditcode.EventCouponRedeemed
import com.bytemedrive.wallet.root.WalletRepository
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.PaymentIntentCallback
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.PaymentIntent
import com.stripe.stripeterminal.external.models.PaymentIntentParameters
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.external.models.TerminalException
import com.stripe.stripeterminal.log.LogLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class PaymentMethodCreditCardViewModel(context: Context) : ViewModel() {

//    init {
//        // Create your listener object. Override any methods that you want to be notified about
//        val listener = object : TerminalListener {
//            override fun onUnexpectedReaderDisconnect(reader: Reader) {
//                TODO("Not yet implemented")
//            }
//        }
//
//        val tokenProvider = TokenProvider()
//
//        if (!Terminal.isInitialized()) {
//            Terminal.initTerminal(context, LogLevel.WARNING, tokenProvider, listener)
//        }
//    }

    fun createPaymentIntent() {
        val params = PaymentIntentParameters.Builder()
            .setAmount(1000)
            .setCurrency("czk")
            .build()

        Terminal.getInstance().createPaymentIntent(params, object: PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                // Placeholder for collecting a payment method with paymentIntent
            }

            override fun onFailure(exception: TerminalException) {
                // Placeholder for handling exception
            }
        })
    }

}