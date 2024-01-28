package com.bytemedrive.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.bytemedrive.navigation.LoginNavigation
import com.bytemedrive.navigation.Snackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(
    navHostController: NavHostController,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { Snackbar(snackbarHostState) },
        content = { LoginNavigation(navHostController = navHostController, innerPadding = it, snackbarHostState) },
    )
}