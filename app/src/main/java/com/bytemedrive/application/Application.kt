package com.bytemedrive.application

import androidx.security.crypto.MasterKey
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
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

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
                androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
                androidContext(applicationContext)
                workManagerFactory()
                modules(databaseModule, accountModule, viewModelsModule, networkModule, storeModule)
            }

            isKoinStarted = true
        }

        networkStatus = NetworkStatus.init(this)
        httpClient = com.bytemedrive.network.HttpClient().client
        sharedPreferences = ByteMeSharedPreferences(applicationContext)
        encryptedSharedPreferences = EncryptedPrefs(applicationContext, MasterKey.Builder(this).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build())

        val stripePublishableKey =
            if (sharedPreferences?.backendUrl?.startsWith("https://api.bytemedrive.com") == true)
                BuildConfig.STRIPE_PUBLISHABLE_KEY_LIVE
            else BuildConfig.STRIPE_PUBLISHABLE_KEY_TEST

        PaymentConfiguration.init(applicationContext, stripePublishableKey)
    }
}