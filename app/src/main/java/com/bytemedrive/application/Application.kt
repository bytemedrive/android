package com.bytemedrive.application

import androidx.security.crypto.MasterKeys
import com.bytemedrive.BuildConfig
import com.bytemedrive.koin.accountModule
import com.bytemedrive.koin.databaseModule
import com.bytemedrive.koin.networkModule
import com.bytemedrive.koin.storeModule
import com.bytemedrive.koin.viewModelsModule
import com.bytemedrive.network.NetworkStatus
import com.bytemedrive.store.EncryptedPrefs
import com.stripe.android.PaymentConfiguration
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

val sharedPreferences: ByteMeSharedPreferences by lazy {
    Application.sharedPreferences!!
}

val encryptedSharedPreferences: EncryptedPrefs by lazy {
    Application.encryptedSharedPreferences!!
}

val httpClient: HttpClient by lazy {
    Application.httpClient!!
}

val networkStatus: NetworkStatus by lazy {
    Application.networkStatus!!
}

class Application : android.app.Application() {

    private var isKoinStarted = false

    companion object {

        var sharedPreferences: ByteMeSharedPreferences? = null

        var encryptedSharedPreferences: EncryptedPrefs? = null

        var httpClient: HttpClient? = null

        var networkStatus: NetworkStatus? = null
    }

    override fun onCreate() {
        super.onCreate()

        if (!isKoinStarted) {
            startKoin {
                androidContext(applicationContext)
                workManagerFactory()
                modules(accountModule, viewModelsModule, databaseModule, networkModule, storeModule)
            }

            isKoinStarted = true
        }

        networkStatus = NetworkStatus.init(this)
        httpClient = com.bytemedrive.network.HttpClient().client
        sharedPreferences = ByteMeSharedPreferences(applicationContext)
        encryptedSharedPreferences = EncryptedPrefs(applicationContext, MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC))

        PaymentConfiguration.init(applicationContext, BuildConfig.STRIPE_PUBLISHABLE_KEY)
    }
}