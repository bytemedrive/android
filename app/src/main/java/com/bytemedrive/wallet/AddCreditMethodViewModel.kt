package com.bytemedrive.wallet

import androidx.lifecycle.ViewModel
import com.bytemedrive.ui.component.RadioItem
import kotlinx.coroutines.flow.MutableStateFlow

class AddCreditMethodViewModel : ViewModel() {

    var method = MutableStateFlow(methodOptions[2].value)

    companion object {

        val methodOptions = listOf(
            RadioItem("creditCard", "Credit card", "Add more credits by filling up the credit card number. Save your card for future used.", false),
            RadioItem("crypto", "Crypto", "Supports all crypto wallets", false),
            RadioItem("creditCode", "Credit code", "Add credits to your account by QR code or coupon code"),
        )
    }
}