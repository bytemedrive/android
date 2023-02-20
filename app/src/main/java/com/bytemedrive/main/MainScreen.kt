package com.bytemedrive.main

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.rememberNavController
import com.bytemedrive.navigation.NavigationBottomBar
import com.bytemedrive.navigation.NavigationHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(pickFileLauncher: ActivityResultLauncher<String>) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Home", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = { pickFileLauncher.launch("*/*") }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "fab icon")
            }
        },
        content = { innerPadding ->
            NavigationHost(navController = navController, innerPadding = innerPadding)
        },
        bottomBar = {
            NavigationBottomBar(navController)
        }
    )
}