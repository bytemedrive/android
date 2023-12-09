package com.bytemedrive

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.ui.MainScreen
import com.bytemedrive.ui.theme.ByteMeTheme
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler)

        setContent {
            ByteMeTheme {
                MainScreen()
            }
        }

        get<SignInManager>().autoSignIn(this)
    }
}