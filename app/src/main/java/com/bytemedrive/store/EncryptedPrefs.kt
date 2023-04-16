package com.bytemedrive.store

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import com.bytemedrive.network.JsonConfig.mapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.UUID
import java.util.stream.Collectors
import kotlin.streams.toList

class EncryptedPrefs(context: Context, masterKeyAlias: String) {
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

    var events: String
        get() = encryptedSharedPreferences.getString(KEY_EVENTS, "[]")!!
        set(value) = encryptedSharedPreferences.edit().putString(KEY_EVENTS, value).apply()

    var eventsCount: Long
        get() = encryptedSharedPreferences.getLong(KEY_EVENTS_COUNT, 0)
        set(value) = encryptedSharedPreferences.edit().putLong(KEY_EVENTS_COUNT, value).apply()

    var secretKeys: MutableSet<String>?
        get() = encryptedSharedPreferences.getStringSet(KEY_SECRET_KEYS, mutableSetOf())
        set(value) = encryptedSharedPreferences.edit().putStringSet(KEY_SECRET_KEYS, value).apply()

    fun storeEvent(vararg events: EventObjectWrapper): List<EventObjectWrapper> {
        Log.i(TAG, "EncryptedPrefs storing ${events.size} events")

        val eventsMapWrapper: MutableList<EventMapWrapper> = mapper.readValue(this.events)
        val eventsObjectWrapper = eventsMapWrapper.stream()
            .map { it.toEventObjectWrapper() }
            .toList()
            .toMutableList()
        val eventsToStore = events.filter { event -> eventsMapWrapper.find { storedEvent -> storedEvent.id == event.id } == null }.toList()

        if (eventsToStore.isNotEmpty()) {
            eventsObjectWrapper.addAll(eventsToStore)
            this.events = mapper.writeValueAsString(eventsObjectWrapper)
            eventsCount = eventsObjectWrapper.size.toLong()
        }

        return eventsObjectWrapper
    }

    fun getEvents(): List<EventObjectWrapper> {
        Log.i(TAG, "EncryptedPrefs getting events")

        return mapper.readValue<List<EventMapWrapper>>(events)
            .stream()
            .map { it.toEventObjectWrapper() }
            .toList()
    }

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

    fun clean() {
        username = null
        credentialsSha3 = null
        events = "[]"
        eventsCount = 0
        secretKeys = null
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