package com.bytemedrive.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.R
import com.bytemedrive.application.GeneralError
import com.bytemedrive.application.GlobalExceptionHandler
import com.bytemedrive.application.RequestFailed
import com.bytemedrive.navigation.AppNavigation
import com.bytemedrive.network.NetworkStatus
import com.bytemedrive.network.NoInternetException
import com.bytemedrive.network.RequestFailedException
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.theme.ByteMeTheme
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import java.net.SocketException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun MainScreen() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navHostController: NavHostController = rememberNavController(bottomSheetNavigator)

    val authorized = AppState.authorized.collectAsState()
    val error by GlobalExceptionHandler.throwable.collectAsState()
    val connected by NetworkStatus.connected.collectAsState()

    error?.let {
        Log.e("Main Screen", it.message, it)

        when (it) {
            is UnknownHostException, is NoInternetException, is SocketException -> null
            is RequestFailedException -> RequestFailed()
            else -> GeneralError(it.message)
        }
    }

    if (!connected) {
        TopBarConnectionStatus()
    }

    if (authorized.value) {
        AppNavigation(navHostController, bottomSheetNavigator)
    } else {
        Login(navHostController)
    }
}

@Composable
private fun TopBarConnectionStatus() {
    Box(modifier = Modifier.zIndex(2f)) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .align(Alignment.TopCenter)
                .padding(4.dp),
            text = stringResource(R.string.top_bar_no_internet_connection),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.inversePrimary
        )
    }
}