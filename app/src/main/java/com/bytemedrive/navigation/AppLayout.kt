package com.bytemedrive.navigation

import AppTopBar
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun AppLayout(
    navHostController: NavHostController,
    bottomSheetNavigator: BottomSheetNavigator,
    startDestination: AppNavigator.NavTarget,
    toggleNav: suspend () -> Unit,
) {
    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(64f, 64f, 0f, 0f)
    ) {
        Scaffold(
            topBar = { AppTopBar(toggleNav, navHostController) },
            content = { paddingValues -> AppNavHost(navHostController, paddingValues, startDestination) },
            bottomBar = { AppBottomMenu(navHostController) },
        )
    }
}
