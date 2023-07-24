package com.bytemedrive.wallet.credit

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bytemedrive.store.AppState
import com.bytemedrive.wallet.root.MoneroPaymentRequest
import com.bytemedrive.wallet.root.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Duration
import java.time.ZonedDateTime

class AddCryptoMethodPaymentViewModel(private val walletRepository: WalletRepository) : ViewModel() {

    val walletAddress = MutableStateFlow<String?>(null)

    val amount = MutableStateFlow<BigDecimal?>(null)

    val expiration = MutableStateFlow<ZonedDateTime?>(null)

    val expiresIn = MutableStateFlow("")

    fun init(storageAmount: Int) {
        viewModelScope.launch {
            val payment = walletRepository.createMoneroPayment(AppState.customer.value?.wallet!!, MoneroPaymentRequest(storageAmount))

            walletAddress.value = payment.walletAddress
            amount.value = payment.amount
            expiration.value = payment.expiration

            val timer = object: CountDownTimer(Duration.between(ZonedDateTime.now(), expiration.value).toMillis(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val hours = millisUntilFinished / (1_000 * 60 * 60)
                    val hoursString = if (hours < 10) "0$hours" else hours
                    val minutes = (millisUntilFinished - hours * (1_000 * 60 * 60)) / (1_000 * 60)
                    val minutesString = if (minutes < 10) "0$minutes" else minutes
                    val seconds = (millisUntilFinished - hours * (1_000 * 60 * 60) - minutes * (1_000 * 60)) / 1_000
                    val secondsString = if (seconds < 10) "0$seconds" else seconds

                    expiresIn.value = "$hoursString:$minutesString:$secondsString"
                }

                override fun onFinish() {

                }
            }
            timer.start()
        }
    }

}