package com.bytemedrive.signup

import com.bytemedrive.store.EncryptedSecretKey

data class CustomerSignUp(val credentialsSha3: String, val aesKey: EncryptedSecretKey, val rsaKey: EncryptedSecretKey, val ntruKey: EncryptedSecretKey)
