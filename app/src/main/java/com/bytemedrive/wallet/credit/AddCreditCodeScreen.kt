package com.bytemedrive.wallet.credit

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.bytemedrive.store.AppState
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCodeScreen(
    addCreditCodeViewModel: AddCreditCodeViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    LaunchedEffect("initialize") {
        AppState.title.value = "Add credit"
    }

    val code by addCreditCodeViewModel.code.collectAsState()

    val onSubmit = {
        addCreditCodeViewModel.redeemCoupon(AppState.customer.value?.wallet!!, code)
        appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 36.dp),
        ) {
            IconButton(
                modifier = Modifier.align(End).padding(vertical = 32.dp),
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
                value = code,
                onValueChange = { addCreditCodeViewModel.code.value = it },
                label = { Text(text = "Code") },
                supportingText = { Text(text = "Paste your code or scan QR code") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )
            Text(
                modifier = Modifier.padding(top = 100.dp),
                text = "Add credits to your account by QR code or coupon code"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
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
            Button(
                onClick = onSubmit
            ) {
                Text(text = "Validate your code")
            }
        }
    }
}