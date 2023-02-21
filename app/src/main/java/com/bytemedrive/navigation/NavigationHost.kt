package com.bytemedrive.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bytemedrive.authentication.SignInScreen
import com.bytemedrive.upload.UploadScreen

@Composable
fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Route.SIGN_IN,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Route.SIGN_IN) { SignInScreen() }
        composable(route = Route.UPLOAD) { UploadScreen() }
    }
}

class MainActions(navController: NavHostController) {
    val goToSignIn: () -> Unit = { navController.navigate(Route.SIGN_IN) }
    val goToUpload: () -> Unit = { navController.navigate(Route.UPLOAD) }
}