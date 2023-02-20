package com.bytemedrive.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationBarItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val onPress: () -> Unit
)