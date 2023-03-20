package com.bytemedrive.application

import AppTopBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.bytemedrive.navigation.AppBottomMenu
import com.bytemedrive.navigation.AppNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Application(navHostController: NavHostController) {
    Scaffold(
        topBar = { AppTopBar() },
        content = { AppNavigation(navHostController = navHostController, innerPadding = it) },
        bottomBar = { AppBottomMenu(navHostController) },
    )
}
