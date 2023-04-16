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
import org.koin.androidx.compose.get

fun getMenuItems(appNavigator: AppNavigator): List<AppBottomMenuItem> =
    listOf(
        AppBottomMenuItem(
            AppNavigator.NavTarget.FILE.label,
            "My Files",
            AppNavigator.NavTarget.FILE.label,
            Icons.Filled.Folder
        ) { appNavigator.navigateTo(AppNavigator.NavTarget.FILE) },
    )

@Composable
fun AppBottomMenu(appNavigator: AppNavigator = get()) {
    val navItems = getMenuItems(appNavigator)
    val selectedItem = remember { mutableStateOf(AppNavigator.NavTarget.SIGN_IN.label) }

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