package com.bytemedrive.navigation

import SignUpScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bytemedrive.authentication.SignInScreen
import com.bytemedrive.authentication.TermsAndConditionsScreen

@Composable
fun LoginNavigation(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navHostController,
        startDestination = Route.SIGN_IN,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Route.SIGN_IN) { SignInScreen(navHostController, snackbarHostState) }
        composable(route = Route.SIGN_UP) { SignUpScreen(navHostController, snackbarHostState) }
        composable(route = Route.TERMS_AND_CONDITIONS) { TermsAndConditionsScreen() }
    }
}

class LoginActions(navHostController: NavHostController) {

    val goToSignIn: () -> Unit = { navHostController.navigate(Route.SIGN_IN) }
    val goToSignUp: () -> Unit = { navHostController.navigate(Route.SIGN_UP) }
    val goToTermsAndConditions: () -> Unit = { navHostController.navigate(Route.TERMS_AND_CONDITIONS) }
}