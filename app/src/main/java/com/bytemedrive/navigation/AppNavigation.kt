package com.bytemedrive.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bytemedrive.file.FileScreen
import com.bytemedrive.file.UploadScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get

@Composable
fun AppNavigation(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    appNavigator: AppNavigator = get()
) {
    LaunchedEffect("navigation") {
        appNavigator.sharedFlow.onEach {
            navHostController.navigate(it)
        }.launchIn(this)
    }

    NavHost(
        navController = navHostController,
        startDestination = AppNavigator.NavTarget.FILE.label,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = AppNavigator.NavTarget.FILE.label) { FileScreen() }
        composable(route = AppNavigator.NavTarget.UPLOAD.label) { UploadScreen() }
    }
}

