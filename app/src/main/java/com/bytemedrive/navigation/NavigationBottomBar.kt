package com.bytemedrive.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

fun getNavigationBarItems(actions: MainActions): List<NavigationBarItem> =
    listOf(
        NavigationBarItem("Sign in", Route.SIGN_IN, Icons.Filled.Login, actions.goToSignIn),
        NavigationBarItem("My Files", Route.FILE, Icons.Filled.Folder, actions.goToMyFiles),
        NavigationBarItem("Upload", Route.UPLOAD, Icons.Filled.Upload, actions.goToUpload),
    )

@Composable
fun NavigationBottomBar(navController: NavHostController) {
    val actions = remember(navController) { MainActions(navController) }
    val navItems = getNavigationBarItems(actions)
    val selectedItem = remember { mutableStateOf(navItems[0]) }

    BottomNavigation(elevation = 10.dp, backgroundColor = MaterialTheme.colorScheme.primary) {
        navItems.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = item.icon, "") },
                label = { Text(text = item.title, color = MaterialTheme.colorScheme.inversePrimary) },
                selected = selectedItem.value == item,
                onClick = {
                    item.onPress()
                    selectedItem.value = item
                },
            )
        }
    }
}