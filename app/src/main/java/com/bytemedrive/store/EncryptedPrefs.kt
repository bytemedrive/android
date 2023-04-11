package com.bytemedrive.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.UUID
import java.util.stream.Collectors
import kotlin.streams.toList


class EncryptedPrefs(context: Context, masterKeyAlias: String) {

    private val encryptedSharedPreferences: SharedPreferences

    fun getUsername(): String? = encryptedSharedPreferences.getString(KEY_USERNAME, null)

    fun storeUsername(username: String) {
        Log.i("com.bytemedrive.store", "EncryptedPrefs storing username: ${username}")
        encryptedSharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getCredentialsSha3(): String? = encryptedSharedPreferences.getString(KEY_CREDENTIALS_SHA3, null)

    fun storeCredentialsSha3(credentialsSha3: String) {
        Log.i("com.bytemedrive.store", "EncryptedPrefs storing credentialsSha3: ${credentialsSha3}.")
        encryptedSharedPreferences.edit().putString(KEY_CREDENTIALS_SHA3, credentialsSha3).apply()
    }

    fun storeEvent(vararg events: EventObjectWrapper): List<EventObjectWrapper> {
        Log.i("com.bytemedrive.store", "EncryptedPrefs storing ${events.size} events")
        val storeEventsAsJson = encryptedSharedPreferences.getString(KEY_EVENTS, "[]")!!
        val eventsMapWrapper: MutableList<EventMapWrapper> = StoreJsonConfig.mapper.readValue(storeEventsAsJson)
        val eventsObjectWrapper = eventsMapWrapper.stream()
            .map { it.toEventObjectWrapper() }
            .toList()
            .toMutableList()

        val eventsToStore = events.filter { event -> eventsMapWrapper.find { storedEvent -> storedEvent.id == event.id } == null }.toList()
        if (eventsToStore.isNotEmpty()) {
            eventsObjectWrapper.addAll(eventsToStore)
            encryptedSharedPreferences.edit().putString(KEY_EVENTS, StoreJsonConfig.mapper.writeValueAsString(eventsObjectWrapper)).apply()
            encryptedSharedPreferences.edit().putLong(KEY_EVENTS_COUNT, eventsObjectWrapper.size.toLong()).apply()
        }
        return eventsObjectWrapper
    }

    fun getEvents(): List<EventObjectWrapper> {
        Log.i("com.bytemedrive.store", "EncryptedPrefs getting events")
        val storedEventsAsJson = encryptedSharedPreferences.getString(KEY_EVENTS, "[]")!!
        return StoreJsonConfig.mapper.readValue<List<EventMapWrapper>>(storedEventsAsJson)
            .stream()
            .map { it.toEventObjectWrapper() }
            .toList()
    }

    fun getEventsCount(): Long = encryptedSharedPreferences.getLong(KEY_EVENTS_COUNT, 0)

    fun storeEventsSecretKey(secretKey: EventsSecretKey) {
        Log.i("com.bytemedrive.store", "EncryptedPrefs storing secret key of ${secretKey.algorithm} algorithm with id: ${secretKey.id}")
        val keys = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
            ?.stream()
            ?.map { StoreJsonConfig.mapper.readValue(it, EventsSecretKey::class.java) }
            ?.collect(Collectors.toMap({ it.id }, { it }))
            ?: HashMap<UUID, EventsSecretKey>()

        keys[secretKey.id] = secretKey

        val setOfJsons = keys.values.stream()
            .map { StoreJsonConfig.mapper.writeValueAsString(it) }
            .collect(Collectors.toSet())

        encryptedSharedPreferences.edit().putStringSet(KEY_SECRET_KEYS, setOfJsons).apply()
    }

    fun getEventsSecretKey(id: UUID): EventsSecretKey? {
        val eventsSecretKey = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
            ?.stream()
            ?.map { StoreJsonConfig.mapper.readValue(it, EventsSecretKey::class.java) }
            ?.filter { id == it.id }
            ?.findAny()

        return eventsSecretKey?.get()
    }

    fun getEventsSecretKey(algorithm: EncryptionAlgorithm): EventsSecretKey? {
        val eventsSecretKey = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
            ?.stream()
            ?.map { StoreJsonConfig.mapper.readValue(it, EventsSecretKey::class.java) }
            ?.filter { algorithm == it.algorithm }
            ?.findAny()

        if (eventsSecretKey != null && eventsSecretKey.isPresent) {
            return eventsSecretKey.get()
        }
        return null
    }

    fun clean() {
        encryptedSharedPreferences.edit().putString(KEY_USERNAME, null).apply()
        encryptedSharedPreferences.edit().putString(KEY_CREDENTIALS_SHA3, null).apply()
        encryptedSharedPreferences.edit().putString(KEY_EVENTS, null).apply()
        encryptedSharedPreferences.edit().putLong(KEY_EVENTS_COUNT, 0).apply()
        encryptedSharedPreferences.edit().putStringSet(KEY_SECRET_KEYS, null).apply()
    }

    companion object {
        private const val FILE_NAME = "bytemedrive"
        private const val KEY_USERNAME = "username"
        private const val KEY_CREDENTIALS_SHA3 = "credentialsSha3"
        private const val KEY_SECRET_KEYS = "secret-keys"
        private const val KEY_EVENTS = "events"
        private const val KEY_EVENTS_COUNT = "events-count"
    }

    init {
        this.encryptedSharedPreferences = EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}