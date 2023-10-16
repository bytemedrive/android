package com.bytemedrive.navigation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.bytemedrive.store.AppState
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
    val topBarComposable by AppState.topBarComposable.collectAsState()

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(64f, 64f, 0f, 0f)
    ) {
        Scaffold(
            topBar = { topBarComposable?.let { it(toggleNav) } },
            content = { paddingValues -> AppNavHost(navHostController, paddingValues, startDestination) },
            bottomBar = { AppBottomMenu(navHostController) },
        )
    }
}
