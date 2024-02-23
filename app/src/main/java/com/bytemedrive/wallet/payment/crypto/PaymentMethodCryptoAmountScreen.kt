package com.bytemedrive.wallet.payment.crypto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.ButtonLoading
import com.bytemedrive.ui.component.FieldNumber
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PaymentMethodCryptoAmountScreen(
    paymentMethodCryptoAmountViewModel: PaymentMethodCryptoAmountViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {

    LaunchedEffect(Unit) {
        AppState.title.update { "Monero payment" }
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
    }

    val submitForm =
        { appNavigator.navigateTo(AppNavigator.NavTarget.PAYMENT_METHOD_CRYPTO_PAYMENT, mapOf("storageAmount" to paymentMethodCryptoAmountViewModel.amount.toString())) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(start = 24.dp, end = 36.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FieldNumber(
                modifier = Modifier
                    .weight(2f)
                    .padding(end = 8.dp),
                type = Long::class.java,
                value = if (paymentMethodCryptoAmountViewModel.amount == null) "" else paymentMethodCryptoAmountViewModel.amount.toString(),
                onValueChange = { value ->
                    paymentMethodCryptoAmountViewModel.amount = value
                },
                label = { Text(text = "Storage amount (GBM)") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { submitForm() }),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "~ ${paymentMethodCryptoAmountViewModel.priceConversion()} XMR"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            ButtonLoading(
                onClick = submitForm,
                enabled = paymentMethodCryptoAmountViewModel.amount != null && !paymentMethodCryptoAmountViewModel.loading,
                loading = paymentMethodCryptoAmountViewModel.loading
            ) {
                Text(text = "Next")
            }
        }
    }
}
