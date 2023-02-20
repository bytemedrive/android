package com.bytemedrive

import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bytemedrive.privacy.FileEncrypt
import com.bytemedrive.ui.theme.ByteMeTheme

class MainActivity : ComponentActivity() {
    private val salt = "dummmySaltToByte".toByteArray()
    private val password: String = "dummyPassword"

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent(), ::onFilePicked)

    private fun onFilePicked(uri: Uri?) {
        if (uri != null) {
            val inputStream = contentResolver.openInputStream(uri)

            if (inputStream != null) {
                val encryptedFile = FileEncrypt.encryptFile(inputStream.readBytes(), password, salt)
                // send file to BE
            }

            inputStream?.close()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ByteMeTheme {
        Greeting("Android")
    }
}