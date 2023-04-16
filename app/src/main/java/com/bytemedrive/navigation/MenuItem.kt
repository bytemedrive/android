package com.bytemedrive.navigation

import androidx.compose.ui.graphics.vector.ImageVector

sealed class MenuItem {
    data class Navigation(val title: String, val route: AppNavigator.NavTarget?, val icon: ImageVector, val onPress: () -> Unit) : MenuItem()
    object Divider : MenuItem()
    data class Label(val title: String) : MenuItem()
}