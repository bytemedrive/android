package com.bytemedrive.privacy

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
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

    fun getCustomerEmail() = sharedPreferences.getString(KEY_CUSTOMER_EMAIL, null)!!

    fun getCustomerPassword() = sharedPreferences.getString(KEY_CUSTOMER_PASSWORD, null)!!.toCharArray()

    fun saveCustomerCredentials(email: String, password: CharArray) =
        sharedPreferences.edit {
            putString(KEY_CUSTOMER_EMAIL, email).apply()
            putString(KEY_CUSTOMER_PASSWORD, password.toString()).apply() // TODO: How to store CharArray? Or how to store password that you cannot convert to String
        }

    private const val FILE_NAME = "bytemedrive"
    private const val KEY_CUSTOMER_EMAIL = "customer_email"
    private const val KEY_CUSTOMER_PASSWORD = "customer_password"
}