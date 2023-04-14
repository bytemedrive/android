package com.bytemedrive.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bytemedrive.R
import com.bytemedrive.navigation.LoginActions
import com.bytemedrive.navigation.SnackbarVisualsWithError
import com.bytemedrive.ui.component.FieldPassword
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    signInViewModel: SignInViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val actions = LoginActions(navHostController)
    val username by signInViewModel.username.collectAsState()
    val password by signInViewModel.password.collectAsState()

    val signInHandler = {
        val validation = signInViewModel.validateForm()

        if (validation?.isNotEmpty() == true) {
            // TODO: Use another way to show errors -> directly at the fields
            scope.launch { snackbarHostState.showSnackbar(SnackbarVisualsWithError(validation, true)) }
        } else {
            val onFailure = { scope.launch { snackbarHostState.showSnackbar(SnackbarVisualsWithError("Invalid credentials")) } }

            signInViewModel.signIn(context, onFailure)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            onValueChange = { signInViewModel.setUsername(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
        )
        FieldPassword(
            value = password,
            onValueChange = { signInViewModel.setPassword(it) },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { signInHandler() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Sign in")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Don't have an account?", fontSize = 16.sp)
            TextButton(onClick = { actions.goToSignUp() }) {
                Text(text = "Sign up now", fontSize = 16.sp)
            }
        }
    }
}
