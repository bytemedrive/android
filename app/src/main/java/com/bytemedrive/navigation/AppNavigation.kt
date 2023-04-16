package com.bytemedrive.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bytemedrive.file.bottomsheet.BottomSheetContext
import com.bytemedrive.file.bottomsheet.BottomSheetCreate
import com.bytemedrive.file.FileScreen
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun AppNavigation(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    appNavigator: AppNavigator = get()
) {
    LaunchedEffect("navigation") {
        appNavigator.sharedFlow.onEach {
            navHostController.navigate(it)
        }.launchIn(this)
    }

    NavHost(
        navController = navHostController,
        startDestination = AppNavigator.NavTarget.FILE.label,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = AppNavigator.NavTarget.FILE.label) { FileScreen() }

        bottomSheet(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT.label) { backstackEntry ->
            BottomSheetContext(backstackEntry.arguments?.getString("id")!!)
        }

        bottomSheet(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE.label) { BottomSheetCreate() }
    }
}

