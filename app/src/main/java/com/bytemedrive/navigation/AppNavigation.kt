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
fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Route.FILE,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Route.FILE) { FileScreen(navController) }
        composable(route = Route.UPLOAD) { UploadScreen() }
    }
}

class MainActions(navController: NavHostController) {

    val goToMyFiles: () -> Unit = { navController.navigate(Route.FILE) }
}