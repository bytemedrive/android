package com.bytemedrive.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class AppBottomMenuItem(
    val key: String,
    val title: String,
    val route: String,
    val icon: ImageVector,
    val onPress: () -> Unit
)