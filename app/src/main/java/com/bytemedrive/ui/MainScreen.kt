package com.bytemedrive.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.application.Application
import com.bytemedrive.application.GeneralError
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.application.NoInternet
import com.bytemedrive.application.RequestFailed
import com.bytemedrive.authentication.Login
import com.bytemedrive.store.AppState
import com.bytemedrive.network.NoInternetException
import com.bytemedrive.network.RequestFailedException
import java.net.UnknownHostException

@Composable
fun MainScreen(navHostController: NavHostController = rememberNavController()) {
    val authorized = AppState.authorized.collectAsState()
    val error by GlobalExceptionHandler.throwable.collectAsState()

    error?.let {
        Log.e("Main Screen", it.message, it.cause)

        when (it) {
            is UnknownHostException -> NoInternet()
            is RequestFailedException -> RequestFailed()
            is NoInternetException -> null
            else -> GeneralError(it.message)
        }
    }

    if (authorized.value) {
        Application(navHostController)
    } else {
        Login(navHostController)
    }
}