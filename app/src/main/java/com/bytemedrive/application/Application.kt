package com.bytemedrive.application

import androidx.security.crypto.MasterKeys
import com.bytemedrive.koin.accountModule
import com.bytemedrive.koin.networkModule
import com.bytemedrive.koin.storeModule
import com.bytemedrive.koin.viewModelsModule
import com.bytemedrive.store.EncryptedPrefs
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

val encryptedSharedPreferences: EncryptedPrefs by lazy {
    Application.encryptedSharedPreferences!!
}

val httpClient: HttpClient by lazy {
    Application.httpClient!!
}

class Application : android.app.Application() {

    private var isKoinStarted = false

    companion object {

        var encryptedSharedPreferences: EncryptedPrefs? = null

        var httpClient: HttpClient? = null
    }

    override fun onCreate() {
        super.onCreate()

        if (!isKoinStarted) {
            startKoin {
                androidContext(applicationContext)
                modules(accountModule, viewModelsModule, networkModule, storeModule)
            }
            isKoinStarted = true
        }

        httpClient = com.bytemedrive.network.HttpClient().client
        encryptedSharedPreferences = EncryptedPrefs(applicationContext, MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC))
    }
}