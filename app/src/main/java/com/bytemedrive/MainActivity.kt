package com.bytemedrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.ui.MainScreen
import com.bytemedrive.ui.theme.ByteMeTheme
import com.google.crypto.tink.streamingaead.StreamingAeadConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.koin.android.ext.android.get
import java.security.Security

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        System.loadLibrary("sqlcipher")
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler)

        setContent {
            ByteMeTheme {
                MainScreen()
            }
        }

        get<SignInManager>().autoSignIn(this)
    }
}