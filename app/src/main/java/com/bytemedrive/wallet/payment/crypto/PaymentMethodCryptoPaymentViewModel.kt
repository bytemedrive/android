package com.bytemedrive.wallet.payment.crypto

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.store.AppState
import com.bytemedrive.wallet.root.MoneroPaymentRequest
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Duration
import java.time.ZonedDateTime

class PaymentMethodCryptoPaymentViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    val walletAddress = MutableStateFlow<String?>(null)

    val amount = MutableStateFlow<BigDecimal?>(null)

    val expirationAt = MutableStateFlow<ZonedDateTime?>(null)

    val expiresIn = MutableStateFlow("")

    val loading = MutableStateFlow(true)

    fun init(storageAmount: Int) {
        viewModelScope.launch {
            loading.update { true }

            val payment = walletRepository.createMoneroPayment(AppState.customer?.wallet!!, MoneroPaymentRequest(storageAmount))

            walletAddress.update { payment.walletAddress }
            amount.update { payment.amount }
            expirationAt.update { payment.expirationAt }

            val timer = object: CountDownTimer(Duration.between(ZonedDateTime.now(), expirationAt.value).toMillis(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1_000 * 60 * 60)
                    val hoursString = if (hours < 10) "0$hours" else hours
                    val minutes = (millisUntilFinished - hours * (1_000 * 60 * 60)) / (1_000 * 60)
                    val minutesString = if (minutes < 10) "0$minutes" else minutes
                    val seconds = (millisUntilFinished - hours * (1_000 * 60 * 60) - minutes * (1_000 * 60)) / 1_000
                    val secondsString = if (seconds < 10) "0$seconds" else seconds

                    expiresIn.update { "$hoursString:$minutesString:$secondsString" }
                }

                override fun onFinish() {

                }
            }
            timer.start()

            loading.update { false }
        }
    }
}