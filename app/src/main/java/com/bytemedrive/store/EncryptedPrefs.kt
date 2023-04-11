package com.bytemedrive.store

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.fasterxml.jackson.module.kotlin.jsonMapper
import java.util.UUID
import java.util.stream.Collectors


class EncryptedPrefs private constructor(private val encryptedSharedPreferences: SharedPreferences) {

    fun getUsername(): String? = encryptedSharedPreferences.getString(KEY_USERNAME, null)

    fun storeUsername(username: String) = encryptedSharedPreferences.edit().putString(KEY_USERNAME, username).apply()


    fun getCredentialsSha3(): String? = encryptedSharedPreferences.getString(KEY_CREDENTIALS_SHA3, null)

    fun storeCredentialsSha3(credentialsSha3: String) = encryptedSharedPreferences.edit().putString(KEY_CREDENTIALS_SHA3, credentialsSha3).apply()


    fun storeEventsSecretKey(secretKey: EventsSecretKey) {
        val keys = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
            ?.stream()
            ?.map { jsonMapper().readValue(it, EventsSecretKey::class.java) }
            ?.collect(Collectors.toMap({ it.id }, { it }))
            ?: HashMap<UUID, EventsSecretKey>()

        keys[secretKey.id] = secretKey

        val setOfJsons = keys.values.stream()
            .map { jsonMapper().writeValueAsString(it) }
            .collect(Collectors.toSet())

        encryptedSharedPreferences.edit().putStringSet(KEY_SECRET_KEYS, setOfJsons).apply()
    }

    fun getEventsSecretKey(id: UUID): EventsSecretKey? {
        val eventsSecretKey = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
            ?.stream()
            ?.map { jsonMapper().readValue(it, EventsSecretKey::class.java) }
            ?.filter { id == it.id }
            ?.findAny()

        return eventsSecretKey?.get()
    }

    fun getEventsSecretKey(algorithm: EncryptionAlgorithm): EventsSecretKey? {
        val eventsSecretKey = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
            ?.stream()
            ?.map { jsonMapper().readValue(it, EventsSecretKey::class.java) }
            ?.filter { algorithm == it.algorithm }
            ?.findAny()

        return eventsSecretKey?.get()
    }


    companion object {
        private const val FILE_NAME = "bytemedrive"
        private const val KEY_USERNAME = "username"
        private const val KEY_CREDENTIALS_SHA3 = "credentialsSha3"
        private const val KEY_SECRET_KEYS = "secret-keys"

        private var instance: EncryptedPrefs? = null

        fun getInstance(context: Context): EncryptedPrefs {
            if (instance == null) {
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                val preferences = EncryptedSharedPreferences.create(
                    FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                instance = EncryptedPrefs(preferences)
            }
            return instance!!
        }
    }
}