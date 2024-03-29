package com.bytemedrive.settings.terminateaccount

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.signin.SignInManager
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.ButtonLoading
import com.bytemedrive.ui.component.FieldPassword
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminateAccountScreen(
    terminateAccountViewModel: TerminateAccountViewModel = koinViewModel(),
    signInManager: SignInManager = koinInject(),
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        AppState.title.update {   "Terminate account"}
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
    }

    val showToastInvalidCredentials = { Toast.makeText(context, "Invalid credentials", Toast.LENGTH_LONG).show() }

    if (terminateAccountViewModel.alertDialogAccountTerminated) {
        val onConfirmation = {
            signInManager.signOut(context)
            terminateAccountViewModel.alertDialogAccountTerminated = false
        }
        AlertDialogAccountTerminated(onConfirmation = onConfirmation)
    }

    Box(Modifier.imePadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Please enter your credentials again to confirm that you are terminating your ByteMe Drive account.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = terminateAccountViewModel.username,
                onValueChange = { terminateAccountViewModel.username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true
            )
            FieldPassword(
                value = terminateAccountViewModel.password,
                onValueChange = { terminateAccountViewModel.password = it },
                label = "Password",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    terminateAccountViewModel.terminateAccount(showToastInvalidCredentials)
                })
            )
            Text(
                "You are about to terminate your account. This action is irreversible. After confirmation, your files will be permanently deleted and you will lose access to any remaining credit.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            ButtonLoading(
                onClick = { terminateAccountViewModel.terminateAccount(showToastInvalidCredentials) },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                loading = terminateAccountViewModel.loading,
                enabled = !terminateAccountViewModel.loading,
            ) {
                Text(text = "Terminate account", color = Color.White)
            }
        }
    }
}

@Composable
private fun AlertDialogAccountTerminated(onConfirmation: () -> Unit, ) {
    AlertDialog(
        icon = {
            Icon(imageVector = Icons.Outlined.NoAccounts, contentDescription = "Account terminated")
        },
        title = {
            Text(text = "Account terminated", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Text(text = "Your account was terminated.", textAlign = TextAlign.Center)
        },
        onDismissRequest = {
            onConfirmation()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Sign out")
            }
        },
    )
}