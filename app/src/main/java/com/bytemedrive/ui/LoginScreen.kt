package com.bytemedrive.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.bytemedrive.navigation.LoginNavigation
import com.bytemedrive.navigation.Snackbar
import com.bytemedrive.service.ServiceManager
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    navHostController: NavHostController,
    serviceManager: ServiceManager = koinInject()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        serviceManager.stopServices(context)
    }


    Scaffold(
        snackbarHost = { Snackbar(snackbarHostState) },
        content = { LoginNavigation(navHostController = navHostController, innerPadding = it, snackbarHostState) },
    )
}