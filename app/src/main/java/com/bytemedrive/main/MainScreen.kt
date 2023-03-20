package com.bytemedrive.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.application.Application
import com.bytemedrive.authentication.SignInScreen
import com.bytemedrive.customer.Customer

@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    val authorized = Customer.authorized.collectAsState()

    if (authorized.value) {
        Application(navController)
    } else {
        SignInScreen()
    }
}