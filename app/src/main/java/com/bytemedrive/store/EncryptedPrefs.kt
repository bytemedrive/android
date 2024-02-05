package com.bytemedrive.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bytemedrive.network.JsonConfig.mapper
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID
import java.util.stream.Collectors

class EncryptedPrefs(context: Context, masterKey: MasterKey) {

    private val TAG = EncryptedPrefs::class.qualifiedName

    private val encryptedSharedPreferences: SharedPreferences

    var username: String?
        get() = encryptedSharedPreferences.getString(KEY_USERNAME, null)
        set(value) {
            Log.i(TAG, "EncryptedPrefs storing username: $value")
            encryptedSharedPreferences.edit().putString(KEY_USERNAME, value).apply()
        }

    var credentialsSha3: String?
        get() = encryptedSharedPreferences.getString(KEY_CREDENTIALS_SHA3, null)
        set(value) {
            Log.i(TAG, "EncryptedPrefs storing credentialsSha3: $value.")
            encryptedSharedPreferences.edit().putString(KEY_CREDENTIALS_SHA3, value).apply()
        }

    var secretKeys: MutableSet<String>?
        get() = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
        set(value) = encryptedSharedPreferences.edit().putStringSet(KEY_SECRET_KEYS, value).apply()

    var dbPasswordBase64: String?
        get() = encryptedSharedPreferences.getString(KEY_DB_PASSWORD, null)
        set(value) = encryptedSharedPreferences.edit().putString(KEY_DB_PASSWORD, value).apply()

    fun storeEventsSecretKey(secretKey: EventsSecretKey) {
        Log.i(TAG, "EncryptedPrefs storing secret key of ${secretKey.algorithm} algorithm with id: ${secretKey.id}")

        val keys = secretKeys
            ?.stream()
            ?.map { mapper.readValue(it, EventsSecretKey::class.java) }
            ?.collect(Collectors.toMap({ it.id }, { it }))
            ?: HashMap<UUID, EventsSecretKey>()

        keys[secretKey.id] = secretKey

        val setOfJsons = keys.values.stream()
            .map { mapper.writeValueAsString(it) }
            .collect(Collectors.toSet())

        secretKeys = setOfJsons
    }

    fun getEventsSecretKey(id: UUID): EventsSecretKey? {
        val eventsSecretKey = secretKeys
            ?.stream()
            ?.map { mapper.readValue(it, EventsSecretKey::class.java) }
            ?.filter { id == it.id }
            ?.findAny()

        return eventsSecretKey?.get()
    }

    fun getEventsSecretKey(algorithm: EncryptionAlgorithm): EventsSecretKey? {
        val eventsSecretKey = secretKeys
            ?.stream()
            ?.map { mapper.readValue(it, EventsSecretKey::class.java) }
            ?.filter { algorithm == it.algorithm }
            ?.findAny()

        if (eventsSecretKey != null && eventsSecretKey.isPresent) {
            return eventsSecretKey.get()
        }

        return null
    }

    fun getDbPassword(): ByteArray {
        if (dbPasswordBase64 != null) {
            return Base64.getDecoder().decode(dbPasswordBase64)
        }
        val dbPassword = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(dbPassword)
        dbPasswordBase64 = Base64.getEncoder().encodeToString(dbPassword)
        return dbPassword
    }

    fun clean() {
        username = null
        credentialsSha3 = null
        secretKeys = null
    }

    companion object {

        private const val FILE_NAME = "bytemedrive"
        private const val KEY_USERNAME = "username"
        private const val KEY_CREDENTIALS_SHA3 = "credentialsSha3"
        private const val KEY_SECRET_KEYS = "secret-keys"
        private const val KEY_DB_PASSWORD = "db-password"
    }

    init {
        this.encryptedSharedPreferences = EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}