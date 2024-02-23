package com.bytemedrive.wallet.payment.creditcard

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.price.Prices
import com.bytemedrive.price.PricesRepository
import com.bytemedrive.wallet.root.StripePaymentRequest
import com.bytemedrive.wallet.root.WalletRepository
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodCreditCardViewModel(
    private val walletRepository: WalletRepository,
    private val customerRepository: CustomerRepository,
    private val pricesRepository: PricesRepository
) : ViewModel() {

    private val TAG = PaymentMethodCreditCardViewModel::class.qualifiedName

    var clientSecret by mutableStateOf<String?>(null)

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

    fun makePayment() = viewModelScope.launch {
        loading = true

        customerRepository.getCustomer()?.let { customer ->
            customer.walletId?.let { walletId ->
                amount?.let { amount ->
                    Log.i(TAG, "Creating payment intent for wallet id=${walletId} and gbm=${amount}")
                    val paymentIntent = walletRepository.stripePayment(walletId, StripePaymentRequest(amount))

                    Log.i(TAG, "Payment intent created. Price eur=${paymentIntent.priceEur}, client secret isEmpty=${paymentIntent.clientSecret.isEmpty()}")
                    clientSecret = paymentIntent.clientSecret
                }
            }
        }

        loading = false
    }

    fun handlePaymentResult(result: PaymentSheetResult, onCompleted: () -> Unit, onFailed: () -> Unit) {
        Log.i(TAG, "Payment result=$result")

        when (result) {
            PaymentSheetResult.Completed -> onCompleted()
            else -> onFailed()
        }
    }
}