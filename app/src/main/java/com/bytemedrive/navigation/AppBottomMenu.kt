package com.bytemedrive.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

fun getMenuItems(actions: MainActions): List<AppBottomMenuItem> =
    listOf(
        AppBottomMenuItem(Route.FILE, "My Files", Route.FILE, Icons.Filled.Folder, actions.goToMyFiles),
    )

@Composable
fun AppBottomMenu(navController: NavHostController) {
    val actions = remember(navController) { MainActions(navController) }
    val navItems = getMenuItems(actions)
    val selectedItem = remember { mutableStateOf(Route.SIGN_IN) }

    BottomNavigation(elevation = 10.dp, backgroundColor = MaterialTheme.colorScheme.primary) {
        navItems.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = item.icon, item.title, tint = Color.White) },
                label = { Text(text = item.title, color = MaterialTheme.colorScheme.inversePrimary) },
                selected = selectedItem.value == item.key,
                onClick = {
                    item.onPress()
                    selectedItem.value = item.key
                },
            )
        }
    }
}