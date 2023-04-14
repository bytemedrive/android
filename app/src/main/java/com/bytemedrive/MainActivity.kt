package com.bytemedrive

import android.content.Context
import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bytemedrive.config.ConfigProperty
import com.bytemedrive.koin.accountModule
import com.bytemedrive.koin.networkModule
import com.bytemedrive.koin.storeModule
import com.bytemedrive.koin.viewModelsModule
import com.bytemedrive.main.MainScreen
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EncryptedPrefs
import com.bytemedrive.store.EncryptionAlgorithm
import com.bytemedrive.store.EventSyncService
import com.bytemedrive.ui.theme.ByteMeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.IOException
import java.io.InputStream
import java.util.Properties
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {

    private val TAG = MainActivity::class.qualifiedName

    private var jobSync: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init encrypted prefs
        EncryptedPrefs.getInstance(this)

        startKoin {
            androidContext(this@MainActivity)
            modules(accountModule, viewModelsModule, networkModule, storeModule)
        }

        loadProperties(assets)

        setContent {
            ByteMeTheme {
                MainScreen()
            }
        }

        jobSync = synchronizeEvents(this)
    }

    private fun synchronizeEvents(context: Context): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            val username = EncryptedPrefs.getInstance(context).getUsername()
            val credentialsSha3 = EncryptedPrefs.getInstance(context).getCredentialsSha3()
            val eventsSecretKey = EncryptedPrefs.getInstance(context).getEventsSecretKey(EncryptionAlgorithm.AES256)
            if (username != null && credentialsSha3 != null && eventsSecretKey != null) {
                Log.i(TAG, "Try autologin for username: $username")
                AppState.loginSuccess()
            } else {
                Log.i(TAG, "Autologin not possible")
            }
            val eventSyncService: EventSyncService = get()
            while (isActive) {
                eventSyncService.syncEvents(context)
                delay(32.seconds)
            }
        }
    }

    private fun loadProperties(assets: AssetManager) {
        try {
            val properties = Properties()
            val inputStream: InputStream = assets.open("config.properties")

            inputStream.use {
                properties.load(inputStream)
            }

            ConfigProperty.setProperties(properties)
            Log.i(TAG, "Properties loaded successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load properties from config.properties.", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        jobSync?.cancel()
    }
}