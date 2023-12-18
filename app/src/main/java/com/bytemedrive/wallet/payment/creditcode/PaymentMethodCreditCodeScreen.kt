package com.bytemedrive.wallet.payment.creditcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bytemedrive.R
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.ButtonLoading
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodCreditCodeScreen(
    paymentMethodCreditCodeViewModel: PaymentMethodCreditCodeViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    val formState by paymentMethodCreditCodeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        AppState.title.update { "Add credit - credit code" }
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 36.dp),
        ) {
            IconButton(
                modifier = Modifier
                    .align(End)
                    .padding(vertical = 32.dp),
                onClick = { }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.QrCode,
                    contentDescription = "QR code",
                    tint = Color.Black,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = formState.code,
                onValueChange = { value -> paymentMethodCreditCodeViewModel.uiState.update { it.copy(code = value, error = null) } },
                label = { Text(text = "Code") },
                supportingText = {
                    if (formState.error == PaymentMethodCreditCodeFormState.ErrorCode.NOT_FOUND) {
                        Text(text = "Invalid code", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(text = "Paste your code or scan QR code")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { paymentMethodCreditCodeViewModel.redeemCoupon() }),
                isError = formState.error == PaymentMethodCreditCodeFormState.ErrorCode.NOT_FOUND,
            )
            Text(
                modifier = Modifier.padding(top = 100.dp),
                text = "Add credits to your account by QR code or coupon code"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.ADD_CREDIT_METHOD) },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Localized description",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.common_back))
            }
            ButtonLoading(
                onClick = { paymentMethodCreditCodeViewModel.redeemCoupon() },
                loading = formState.loading,
                enabled = formState.code.isNotEmpty() && !formState.loading
            ) {
                Text(text = "Validate your code")
            }
        }
    }
}