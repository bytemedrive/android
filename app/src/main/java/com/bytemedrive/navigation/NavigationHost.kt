package com.bytemedrive.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Route.SIGN_IN,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Route.SIGN_IN) {
//            SignInScreen()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Sign in screen")
            }
        }
    }
}

class MainActions(navController: NavHostController) {
    val goToSignIn: () -> Unit = { navController.navigate(Route.SIGN_IN) }
    val goToHome: () -> Unit = { navController.navigate(Route.HOME) }
}