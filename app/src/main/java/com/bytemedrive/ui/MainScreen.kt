package com.bytemedrive.main

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.application.Application
import com.bytemedrive.authentication.Login
import com.bytemedrive.store.AppState
import com.bytemedrive.store.EncryptedPrefs
import com.bytemedrive.store.EncryptionAlgorithm

@Composable
fun MainScreen(navHostController: NavHostController = rememberNavController()) {
    val context = LocalContext.current

    val authorized = AppState.authorized.collectAsState()

    if (authorized.value) {
        Application(navHostController)
    } else {
        Login(navHostController)
    }
}