package com.bytemedrive

import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bytemedrive.privacy.FileEncrypt
import com.bytemedrive.koin.accountModule
import com.bytemedrive.koin.networkModule
import com.bytemedrive.koin.viewModelsModule
import com.bytemedrive.network.Endpoint
import com.bytemedrive.network.RestApiBuilder
import com.bytemedrive.ui.theme.ByteMeTheme
import io.earthbanc.mrv.config.ConfigProperty
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
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
            contentResolver.openInputStream(uri) .use {
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

        val restApiBuilder by inject<RestApiBuilder>()

        setContent {
            ByteMeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column() {
                        Row {
                            Button(onClick = { pickFileLauncher.launch("*/*") }) {
                                Text(text = "Pick file")
                            }
                            Button(
                                content = { Text(text = "Fooo") },
                                onClick = { CoroutineScope(Dispatchers.Main).launch { restApiBuilder.client.get(Endpoint.GET.url) { } }
                                })
                        }
                    }
                }
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