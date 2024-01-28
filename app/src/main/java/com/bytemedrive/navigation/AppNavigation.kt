package com.bytemedrive.navigation

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bytemedrive.BuildConfig
import com.bytemedrive.R
import com.bytemedrive.signin.SignInManager
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation(
    navHostController: NavHostController,
    bottomSheetNavigator: BottomSheetNavigator,
    appNavigationViewModel: AppNavigationViewModel = koinViewModel(),
    signInManager: SignInManager = koinInject(),
    appNavigator: AppNavigator = koinInject(),
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val screenPermissionsState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

        LaunchedEffect(Unit) {
            screenPermissionsState.launchPermissionRequest()
        }
    }   

    val startDestination = AppNavigator.NavTarget.FILE
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navItems = getMenuItems(context, signInManager, appNavigator, appNavigationViewModel.usedStorage, appNavigationViewModel.balanceGbm)
    val scrollState = rememberScrollState()

    val selectedItemDefault = remember { navItems.find { it is MenuItem.Navigation && it.route == startDestination } as MenuItem.Navigation? }
    var selectedItem by remember { mutableStateOf(selectedItemDefault) }

    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                        .weight(1f)
                        .verticalScroll(scrollState),
                ) {
                    navItems.forEach {
                        when (it) {
                            is MenuItem.Navigation ->
                                NavigationDrawerItem(
                                    icon = { Icon(it.icon, contentDescription = null) },
                                    label = { Text(it.title) },
                                    selected = it == selectedItem,
                                    onClick = {
                                        if (it.onPress != null) {
                                            it.onPress.invoke()
                                            scope.launch { drawerState.close() }
                                            selectedItem = it
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                            is MenuItem.Divider -> Divider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                            is MenuItem.Label -> Text(modifier = Modifier.padding(horizontal = 12.dp), text = it.title)
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = stringResource(R.string.menu_app_username_label, appNavigationViewModel.username),
                        fontSize = 12.sp
                    )
                    Text(
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = stringResource(R.string.common_app_version, BuildConfig.VERSION_NAME),
                        fontSize = 12.sp
                    )
                }

            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLayout(navHostController, bottomSheetNavigator, startDestination) {
                    appNavigationViewModel.getMenuData()
                    drawerState.open()
                }
            }
        }
    )
}

private fun getMenuItems(
    context: Context,
    signInManager: SignInManager,
    appNavigator: AppNavigator,
    usedStorage: Double,
    balanceGbm: Long
): List<MenuItem> = listOf(
    MenuItem.Navigation(
        "Dashboard",
        AppNavigator.NavTarget.FILE,
        Icons.Default.Home
    ) { appNavigator.navigateTo(AppNavigator.NavTarget.FILE) },
    MenuItem.Divider,
    MenuItem.Label(context.getString(R.string.menu_app_my_data)),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_used_storage, DecimalFormat("#.##").format(usedStorage)),
        null,
        Icons.Default.Language
    ),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_credit_amount, balanceGbm),
        null,
        Icons.Default.WbSunny
    ),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_credit_add),
        AppNavigator.NavTarget.ADD_CREDIT_METHOD,
        Icons.Default.AddCard
    ) { appNavigator.navigateTo(AppNavigator.NavTarget.ADD_CREDIT_METHOD) },
    MenuItem.Divider,
    MenuItem.Label(context.getString(R.string.menu_app_my_account)),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_settings),
        AppNavigator.NavTarget.SETTINGS,
        Icons.Default.Settings
    ) { appNavigator.navigateTo(AppNavigator.NavTarget.SETTINGS) },
    MenuItem.Navigation(
        context.getString(R.string.common_sign_out),
        AppNavigator.NavTarget.SIGN_IN,
        Icons.Default.Logout
    ) { signInManager.signOut(context) },
)
