package com.bytemedrive.privacy

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences

object EncryptedStorage {

    lateinit var sharedPreferences: SharedPreferences

    fun initializeSharedPreferences(context: Context, masterKeyAlias: String) {
        sharedPreferences = EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getCustomerPassword() = sharedPreferences.getString(KEY_CUSTOMER_PASSWORD, null)!!.toCharArray()

    fun saveCustomerPassword(password: CharArray) = sharedPreferences.edit().putString(KEY_CUSTOMER_PASSWORD, password.concatToString()).apply()

    private const val FILE_NAME = "bytemedrive"
    private const val KEY_CUSTOMER_PASSWORD = "customer_password"
}