package com.bytemedrive

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bytemedrive.config.ConfigProperty
import com.bytemedrive.koin.accountModule
import com.bytemedrive.koin.networkModule
import com.bytemedrive.koin.viewModelsModule
import com.bytemedrive.main.MainScreen
import com.bytemedrive.ui.theme.ByteMeTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.IOException
import java.io.InputStream
import java.util.Properties

class MainActivity : ComponentActivity() {

    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(accountModule, viewModelsModule, networkModule)
        }

        loadProperties(assets)

        setContent {
            ByteMeTheme {
                MainScreen()
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
}