package com.bytemedrive.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bytemedrive.file.FileScreen
import com.bytemedrive.file.bottomsheet.BottomSheetContextFile
import com.bytemedrive.file.bottomsheet.BottomSheetContextFolder
import com.bytemedrive.file.bottomsheet.BottomSheetCreate
import com.bytemedrive.wallet.AddCreditCodeScreen
import com.bytemedrive.wallet.AddCreditMethodScreen
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun AppNavHost(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: AppNavigator.NavTarget,
    appNavigator: AppNavigator = get()
) {
    LaunchedEffect("navigation") {
        appNavigator.sharedFlow.onEach {
            when (it) {
                AppNavigator.NavTarget.BACK.label -> navHostController.popBackStack()
                else -> navHostController.navigate(it)
            }
        }.launchIn(this)
    }

    NavHost(
        navController = navHostController,
        startDestination = startDestination.label,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(
            route = AppNavigator.NavTarget.FILE.label,
            arguments = listOf(navArgument("folderId") { nullable = true })
        ) { backstackEntry ->
            FileScreen(backstackEntry.arguments?.getString("folderId"))
        }
        composable(route = AppNavigator.NavTarget.ADD_CREDIT_METHOD.label) { AddCreditMethodScreen() }
        composable(route = AppNavigator.NavTarget.ADD_CREDIT_CODE.label) { AddCreditCodeScreen() }

        bottomSheet(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FILE.label) { backstackEntry ->
            BottomSheetContextFile(backstackEntry.arguments?.getString("id")!!)
        }

        bottomSheet(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FOLDER.label) { backstackEntry ->
            BottomSheetContextFolder(backstackEntry.arguments?.getString("id")!!)
        }

        bottomSheet(
            route = AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE.label,
            arguments = listOf(navArgument("folderId") { nullable = true })
        ) { backstackEntry ->
            BottomSheetCreate(backstackEntry.arguments?.getString("folderId"))
        }
    }
}

