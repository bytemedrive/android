package com.bytemedrive.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bytemedrive.R
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.store.AppState
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun AppNavigation(
    navHostController: NavHostController,
    bottomSheetNavigator: BottomSheetNavigator,
    signInManager: SignInManager = get(),
    appNavigator: AppNavigator = get(),
) {
    val startDestination = AppNavigator.NavTarget.FILE
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navItems = getMenuItems(context, signInManager, appNavigator)

    val selectedItemDefault = remember { navItems.find { it is MenuItem.Navigation && it.route == startDestination } as MenuItem.Navigation? }
    val selectedItem = remember { mutableStateOf(selectedItemDefault) }

    ModalNavigationDrawer(
        gesturesEnabled = drawerState.isOpen,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .align(Alignment.TopStart),
                    ) {
                        navItems.forEach {
                            when (it) {
                                is MenuItem.Navigation ->
                                    NavigationDrawerItem(
                                        icon = { Icon(it.icon, contentDescription = null) },
                                        label = { Text(it.title) },
                                        selected = it == selectedItem.value,
                                        onClick = {
                                            if (it.onPress != null) {
                                                it.onPress.invoke()
                                                scope.launch { drawerState.close() }
                                                selectedItem.value = it
                                            }
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )

                                is MenuItem.Divider -> Divider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                                is MenuItem.Label -> Text(modifier = Modifier.padding(horizontal = 12.dp), text = it.title)
                            }
                        }
                    }
                }

            }
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLayout(navHostController, bottomSheetNavigator, startDestination) { drawerState.open() }
            }
        }
    )
}

private fun getMenuItems(context: Context, signInManager: SignInManager, appNavigator: AppNavigator): List<MenuItem> = listOf(
    MenuItem.Navigation(
        AppState.customer.value?.username?.value.orEmpty(),
        null,
        Icons.Default.Person,
    ),
    MenuItem.Divider,
    MenuItem.Label(context.getString(R.string.menu_app_my_data)),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_used_storage, usedStorage()),
        null,
        Icons.Default.Language
    ),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_credit_amount, AppState.customer.value?.balanceGbm ?: 0),
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
    ),
    MenuItem.Navigation(
        context.getString(R.string.menu_app_bin),
        AppNavigator.NavTarget.BIN,
        Icons.Default.Delete
    ),
    MenuItem.Navigation(
        context.getString(R.string.common_sign_out),
        AppNavigator.NavTarget.SIGN_IN,
        Icons.Default.Logout
    ) { signInManager.signOut() },
)

private fun usedStorage() = AppState.customer.value?.dataFiles?.sumOf { it.sizeBytes }?.div(1_073_741_824.0)?.let { DecimalFormat("#.##").format(it) } ?: 0
