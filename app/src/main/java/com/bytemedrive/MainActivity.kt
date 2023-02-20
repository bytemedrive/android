package com.bytemedrive

import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.bytemedrive.koin.accountModule
import com.bytemedrive.koin.networkModule
import com.bytemedrive.koin.viewModelsModule
import com.bytemedrive.main.MainScreen
import com.bytemedrive.privacy.FileEncrypt
import com.bytemedrive.ui.theme.ByteMeTheme
import io.earthbanc.mrv.config.ConfigProperty
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : ComponentActivity() {
    private val TAG = MainActivity::class.qualifiedName
    private val salt = "dummmySaltToByte".toByteArray()
    private val password: String = "dummyPassword"

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), ::onFilePicked)

    private fun onFilePicked(uri: Uri?) {
        if (uri != null) {
            contentResolver.openInputStream(uri).use {
                if (it != null) {
                    val encryptedFile = FileEncrypt.encrypt(it.readBytes(), password, salt)
                    // send file to BE
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidContext(this@MainActivity)
            modules(accountModule, viewModelsModule, networkModule)
        }

        loadProperties(assets)

        setContent {
            ByteMeTheme {
                MainScreen(
                    pickFileLauncher
                )
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