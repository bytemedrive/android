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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bytemedrive.R
import com.bytemedrive.navigation.LoginActions
import com.bytemedrive.navigation.SnackbarVisualsWithError
import com.bytemedrive.signup.SignUpViewModel
import com.bytemedrive.ui.component.FieldCheckbox
import com.bytemedrive.ui.component.FieldPassword
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navHostController: NavHostController,
    snackbarHostState: SnackbarHostState,
    signUpViewModel: SignUpViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val actions = LoginActions(navHostController)
    val username by signUpViewModel.username.collectAsState()
    val password by signUpViewModel.password.collectAsState()
    val passwordConfirm by signUpViewModel.passwordConfirm.collectAsState()
    val termsAndConditions by signUpViewModel.termsAndConditions.collectAsState()

    val signUpHandler = {
        val validation = signUpViewModel.validateForm()

        if (validation?.isNotEmpty() == true) {
            // TODO: Use another way to show errors -> directly at the fields
            scope.launch { snackbarHostState.showSnackbar(SnackbarVisualsWithError(validation, true)) }
        } else {
            val onFailure = { scope.launch { snackbarHostState.showSnackbar(SnackbarVisualsWithError("Username is already being used")) } }

            signUpViewModel.signUp(onFailure)
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
            text = "Sign up",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight(500)
        )
        OutlinedTextField(
            value = username,
            onValueChange = { signUpViewModel.setUsername(it) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
        )
        FieldPassword(
            value = password,
            onValueChange = { signUpViewModel.setPassword(it) },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
        )
        FieldPassword(
            value = passwordConfirm,
            onValueChange = { signUpViewModel.setPasswordConfirm(it) },
            label = "Password confirm",
            modifier = Modifier.fillMaxWidth(),
            showEye = false
        )
        FieldCheckbox(
            modifier = Modifier.padding(vertical = 12.dp),
            key = "termsAndConditions",
            checked = termsAndConditions,
            onChangeValue = { _, checked -> signUpViewModel.setTermsAndConditions(checked) }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Agree to our")
                TextButton(onClick = { actions.goToTermsAndConditions() }) {
                    Text(text = "Term & Conditions")
                }
            }
        }
        Button(
            onClick = { signUpHandler() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Sign up")
        }
        TextButton(onClick = { actions.goToSignIn() }) {
            Text(text = "Sign in")
        }
    }
}