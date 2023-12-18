package com.bytemedrive.wallet.payment.creditcode

data class PaymentMethodCreditCodeFormState(val code: String, val loading: Boolean, val error: ErrorCode? = null) {
    enum class ErrorCode {
        NOT_FOUND
    }
}
