package com.bytemedrive.stripe

import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException

class TokenProvider : ConnectionTokenProvider {
    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        try {
            callback.onSuccess("pk_test_51N7wTaL2hkomI7ZgL9WwjYWPcJV5UIUpCyRsBgsj1kzzeaOKBuwZzDO5V6Ejwdgln7udEGIFK4sT8l3dt09zIHJ900HOjKJ74S")
        } catch (e: Exception) {
            callback.onFailure(
                ConnectionTokenException("Failed to fetch connection token", e)
            )
        }
    }
}