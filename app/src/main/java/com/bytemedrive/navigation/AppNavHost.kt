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
import com.bytemedrive.file.root.FileScreen
import com.bytemedrive.file.root.bottomsheet.FileBottomSheetContextFile
import com.bytemedrive.file.root.bottomsheet.FileBottomSheetContextFolder
import com.bytemedrive.file.shared.bottomsheet.FileBottomSheetCreate
import com.bytemedrive.file.starred.StarredScreen
import com.bytemedrive.file.starred.bottomsheet.StarredBottomSheetContextFile
import com.bytemedrive.file.starred.bottomsheet.StarredBottomSheetContextFolder
import com.bytemedrive.wallet.payment.AddCreditMethodScreen
import com.bytemedrive.wallet.payment.creditcard.PaymentMethodCreditCardScreen
import com.bytemedrive.wallet.payment.creditcode.PaymentMethodCreditCodeScreen
import com.bytemedrive.wallet.payment.crypto.PaymentMethodCryptoAmountScreen
import com.bytemedrive.wallet.payment.crypto.PaymentMethodCryptoPaymentScreen
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun AppNavHost(
    navHostController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: AppNavigator.NavTarget,
    appNavigator: AppNavigator = koinInject()
) {
    LaunchedEffect("navigation") {
        appNavigator.sharedFlow.onEach {
            when (it) {
                AppNavigator.NavTarget.BACK.label -> navHostController.popBackStack()
                AppNavigator.NavTarget.CLEAR.label -> navHostController.navigate(AppNavigator.NavTarget.FILE.label) {
                    popUpTo(navHostController.graph.id) {
                        inclusive = true
                    }
                }
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
            val folderId = backstackEntry.arguments?.getString("folderId")?.let { UUID.fromString(it) }

            FileScreen(folderId)
        }

        composable(route = AppNavigator.NavTarget.ADD_CREDIT_METHOD.label) { AddCreditMethodScreen() }
        composable(route = AppNavigator.NavTarget.PAYMENT_METHOD_CREDIT_CARD.label) { PaymentMethodCreditCardScreen() }
        composable(route = AppNavigator.NavTarget.PAYMENT_METHOD_CREDIT_CODE.label) { PaymentMethodCreditCodeScreen() }
        composable(route = AppNavigator.NavTarget.PAYMENT_METHOD_CRYPTO_AMOUNT.label) { PaymentMethodCryptoAmountScreen() }
        composable(
            route = AppNavigator.NavTarget.PAYMENT_METHOD_CRYPTO_PAYMENT.label,
            arguments = listOf(navArgument("storageAmount") {})
        ) {
                backstackEntry -> PaymentMethodCryptoPaymentScreen(backstackEntry.arguments?.getString("storageAmount")?.toInt() ?: 0)
        }
        composable(route = AppNavigator.NavTarget.STARRED.label) { StarredScreen() }

        bottomSheet(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FILE.label) { backstackEntry ->
            val dataFileLinkId = backstackEntry.arguments?.getString("id")!!.let { UUID.fromString(it) }

            FileBottomSheetContextFile(dataFileLinkId)
        }

        bottomSheet(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT_FOLDER.label) { backstackEntry ->
            val folderId = backstackEntry.arguments?.getString("id")!!.let { UUID.fromString(it) }

            FileBottomSheetContextFolder(folderId)
        }

        bottomSheet(AppNavigator.NavTarget.STARRED_BOTTOM_SHEET_CONTEXT_FILE.label) { backstackEntry ->
            val dataFileLinkId = backstackEntry.arguments?.getString("id")!!.let { UUID.fromString(it) }

            StarredBottomSheetContextFile(dataFileLinkId)
        }

        bottomSheet(AppNavigator.NavTarget.STARRED_BOTTOM_SHEET_CONTEXT_FOLDER.label) { backstackEntry ->
            val folderId = backstackEntry.arguments?.getString("id")!!.let { UUID.fromString(it) }

            StarredBottomSheetContextFolder(folderId)
        }

        bottomSheet(
            route = AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE.label,
            arguments = listOf(navArgument("folderId") { nullable = true })
        ) { backstackEntry ->
            val folderId = backstackEntry.arguments?.getString("folderId")?.let { UUID.fromString(it) }

            FileBottomSheetCreate(folderId)
        }
    }
}

