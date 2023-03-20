package com.bytemedrive.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.MasterKeys
import com.bytemedrive.customer.Customer
import com.bytemedrive.event.Event
import com.bytemedrive.event.EventMapWrapper
import com.bytemedrive.event.EventRepository
import com.bytemedrive.event.EventType
import com.bytemedrive.event.MyClassDeserializer
import com.bytemedrive.event.ReadService
import com.bytemedrive.privacy.AesService
import com.bytemedrive.privacy.EncryptedStorage
import com.bytemedrive.privacy.ShaService
import com.bytemedrive.upload.EventFileUploaded
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Base64
import java.util.Objects

class SignInViewModel(private val eventRepository: EventRepository) : ViewModel() {

    fun signIn(context: Context, username: String, password: CharArray) {

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        EncryptedStorage.initializeSharedPreferences(context, masterKeyAlias)
        EncryptedStorage.saveCustomerPassword(password)

        Customer.setUsername(username)

        viewModelScope.launch {
            val data = eventRepository.fetch(ShaService.hashSha3(username))

            data.forEach {
                val gson = GsonBuilder().registerTypeAdapter(Event::class.java, MyClassDeserializer()).create()

                val eventEncrypted = Base64.getDecoder().decode(it)
                val eventBytes = AesService.decrypt(eventEncrypted, password, ShaService.hashSha3(email).encodeToByteArray())
//                val event = Json.decodeFromString<Event<Any>>(String(eventBytes))
                var event = gson.fromJson(String(eventBytes), EventMapWrapper::class.java)
                val appEvent = ReadService.convertToAppEvent(event)


                println(appEvent)


            }
        }
    }
}