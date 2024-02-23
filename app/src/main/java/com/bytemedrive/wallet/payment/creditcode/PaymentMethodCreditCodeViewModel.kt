package com.bytemedrive.wallet.payment.creditcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.customer.control.CustomerRepository
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.network.RequestFailedException
import com.bytemedrive.store.EventPublisher
import com.bytemedrive.wallet.root.WalletRepository
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodCreditCodeViewModel(
    private val walletRepository: WalletRepository,
    private val eventPublisher: EventPublisher,
    private val appNavigator: AppNavigator,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val uiState = MutableStateFlow(PaymentMethodCreditCodeFormState("", false))

    fun redeemCoupon() = viewModelScope.launch {
        uiState.update { it.copy(loading = true) }

        customerRepository.getCustomer()?.let { customer ->
            customer.walletId?.let { walletId ->
                val code = uiState.value.code

                try {
                    walletRepository.redeemCoupon(walletId, code)
                    eventPublisher.publishEvent(EventCouponRedeemed(walletId, code))
                    appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
                } catch (exception: RequestFailedException) {
                    if (exception.response.status == HttpStatusCode.NotFound) {
                        uiState.update { it.copy(error = PaymentMethodCreditCodeFormState.ErrorCode.NOT_FOUND) }
                    } else {
                        throw exception
                    }
                } finally {
                    uiState.update { it.copy(loading = false) }
                }
            }
        }
    }
}