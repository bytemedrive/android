package com.bytemedrive.navigation

import android.content.Context
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bytemedrive.R
import org.koin.androidx.compose.get
import org.koin.compose.koinInject

fun getMenuItems(context: Context, appNavigator: AppNavigator): List<MenuItem.Navigation> =
    listOf(
        MenuItem.Navigation(
            context.getString(R.string.bottom_menu_my_files),
            AppNavigator.NavTarget.FILE,
            Icons.Filled.Folder
        ) { appNavigator.navigateTo(AppNavigator.NavTarget.FILE) },
        MenuItem.Navigation(
            context.getString(R.string.bottom_menu_starred),
            AppNavigator.NavTarget.STARRED,
            Icons.Filled.Star
        ) { appNavigator.navigateTo(AppNavigator.NavTarget.STARRED) },
    )

@Composable
fun AppBottomMenu(navHostController: NavHostController, appNavigator: AppNavigator = koinInject()) {
    val context = LocalContext.current
    val navItems = getMenuItems(context, appNavigator)
    val selectedItemDefault = remember { navItems.find { it.route?.label == navHostController.currentDestination?.route } }
    val selectedItem = remember { mutableStateOf(selectedItemDefault) }

    BottomNavigation(elevation = 10.dp, backgroundColor = MaterialTheme.colorScheme.primary) {
        navItems.forEach { item ->
            BottomNavigationItem(
                icon = { Icon(imageVector = item.icon, item.title, tint = Color.White) },
                label = { Text(text = item.title, color = MaterialTheme.colorScheme.inversePrimary) },
                selected = selectedItem.value == item,
                onClick = {
                    if (item.onPress != null) {
                        item.onPress.invoke()
                        selectedItem.value = item
                    }
                },
            )
        }
    }
}