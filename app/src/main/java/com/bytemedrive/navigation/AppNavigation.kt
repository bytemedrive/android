package com.bytemedrive.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bytemedrive.file.FileScreen
import com.bytemedrive.upload.UploadScreen

@Composable
fun AppNavigation(navHostController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navHostController,
        startDestination = Route.FILE,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Route.FILE) { FileScreen(navHostController) }
        composable(route = Route.UPLOAD) { UploadScreen() }
    }
}

class AppActions(navHostController: NavHostController) {

    val goToMyFiles: () -> Unit = { navHostController.navigate(Route.FILE) }
}