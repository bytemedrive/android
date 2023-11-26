package com.bytemedrive.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    appNavigator: AppNavigator = koinInject()
) {
    LaunchedEffect("initialize") {
        AppState.title.value = "Settings"
        AppState.topBarComposable.value = { TopBarAppContentBack() }
    }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {}
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Column {
                TextButton(onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.TERMINATE_ACCOUNT) }) {
                    Text(text = "Terminate account", color = MaterialTheme.colors.error)
                }
            }
        }
    }
}