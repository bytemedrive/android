package com.bytemedrive.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytemedrive.BuildConfig
import com.bytemedrive.R
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.SnackbarVisualsWithError
import com.bytemedrive.ui.component.ButtonLoading
import com.bytemedrive.ui.component.FieldPassword
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    snackbarHostState: SnackbarHostState,
    signInViewModel: SignInViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val username by signInViewModel.username.collectAsState()
    val password by signInViewModel.password.collectAsState()
    val loading by signInViewModel.loading.collectAsState()

    var secretSettingsCounter by remember { mutableStateOf(0) }

    if (secretSettingsCounter > 4) {
        SecretSettings { secretSettingsCounter = 0 }
    }

    val scrollState = rememberScrollState()

    val signIn = {
        val validation = signInViewModel.validateForm()

        if (validation?.isNotEmpty() == true) {
            // TODO: Use another way to show errors -> directly at the fields
            scope.launch { snackbarHostState.showSnackbar(SnackbarVisualsWithError(validation, true)) }
        } else {
            val onFailure = { scope.launch { snackbarHostState.showSnackbar(SnackbarVisualsWithError("Invalid credentials")) } }

            signInViewModel.signIn(context, onFailure)
        }
    }

    Box(
        modifier = Modifier.imePadding().fillMaxSize().verticalScroll(scrollState).padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { secretSettingsCounter++ }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(modifier = Modifier.size(100.dp), imageVector = ImageVector.vectorResource(id = R.drawable.byteme_logo_symbol_color), contentDescription = "Logo")
            Text(
                text = "ByteMe Drive",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight(500)
            )
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
                fontSize = 22.sp,
                fontWeight = FontWeight(500)
            )
            OutlinedTextField(
                value = username,
                onValueChange = { value -> signInViewModel.username.update { value } },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )
            FieldPassword(
                value = password,
                onValueChange = { value -> signInViewModel.password.update { value } },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    signIn()
                })
            )
            ButtonLoading(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                onClick = { signIn() },
                loading = loading,
                enabled = !loading
            ) {
                Text(text = "Sign in")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Don't have an account?", fontSize = 16.sp)
                TextButton(onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.SIGN_UP) }) {
                    Text(text = "Sign up now", fontSize = 16.sp)
                }
            }
        }

        Text(
            modifier = Modifier.align(Alignment.BottomCenter),
            text = stringResource(R.string.common_app_version, BuildConfig.VERSION_NAME),
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}
