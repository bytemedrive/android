package com.bytemedrive.navigation

import SignUpScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bytemedrive.signin.SignInScreen
import com.bytemedrive.signup.TermsAndConditionsScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject

@Composable
fun LoginNavigation(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    snackbarHostState: SnackbarHostState,
    appNavigator: AppNavigator = koinInject(),
) {
    LaunchedEffect(Unit) {
        appNavigator.sharedFlow.onEach {
            navHostController.navigate(it)
        }.launchIn(this)
    }

    NavHost(
        navController = navHostController,
        startDestination = AppNavigator.NavTarget.SIGN_IN.label,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = AppNavigator.NavTarget.SIGN_IN.label) { SignInScreen(snackbarHostState) }
        composable(route = AppNavigator.NavTarget.SIGN_UP.label) { SignUpScreen(snackbarHostState) }
        composable(route = AppNavigator.NavTarget.TERMS_AND_CONDITIONS.label) { TermsAndConditionsScreen() }
    }
}
