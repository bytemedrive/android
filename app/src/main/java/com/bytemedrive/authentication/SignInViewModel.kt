package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.security.crypto.MasterKeys
import com.bytemedrive.customer.Customer
import com.bytemedrive.privacy.EncryptedStorage

class SignInViewModel() : ViewModel() {

    fun signIn(context: Context, username: String, password: CharArray) {

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedStorage.initializeSharedPreferences(context, masterKeyAlias)
        EncryptedStorage.saveCustomerPassword(password)

        Customer.setUsername(username)
    }
}