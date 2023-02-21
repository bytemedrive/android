package com.bytemedrive.main

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.navigation.NavigationBottomBar
import com.bytemedrive.navigation.NavigationHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        floatingActionButtonPosition = FabPosition.End,
        content = { NavigationHost(navController = navController, innerPadding = it) },
        bottomBar = { NavigationBottomBar(navController) }
    )
}