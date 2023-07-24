package com.bytemedrive.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.application.GeneralError
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.application.NoInternet
import com.bytemedrive.application.RequestFailed
import com.bytemedrive.navigation.AppNavigation
import com.bytemedrive.store.AppState
import com.bytemedrive.network.NoInternetException
import com.bytemedrive.network.RequestFailedException
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import java.net.UnknownHostException

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun MainScreen() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navHostController: NavHostController = rememberNavController(bottomSheetNavigator)

    val authorized = AppState.authorized.collectAsState()
    val error by GlobalExceptionHandler.throwable.collectAsState()

    error?.let {
        Log.e("Main Screen", it.message, it)

        when (it) {
            is UnknownHostException -> NoInternet()
            is RequestFailedException -> RequestFailed()
            is NoInternetException -> null
            else -> GeneralError(it.message)
        }
    }

    if (authorized.value) {
        AppNavigation(navHostController, bottomSheetNavigator)
    } else {
        Login(navHostController)
    }
}