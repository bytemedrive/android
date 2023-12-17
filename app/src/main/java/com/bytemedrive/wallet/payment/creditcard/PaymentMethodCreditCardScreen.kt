package com.bytemedrive.wallet.payment.creditcard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.stripe.android.paymentsheet.PaymentSheetContract
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

val REGEX_NUMBER_DECIMAL = "\\d*".toRegex()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodCreditCardScreen(
    paymentMethodCreditCardViewModel: PaymentMethodCreditCardViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    val context = LocalContext.current
    val clientSecret by paymentMethodCreditCardViewModel.clientSecret.collectAsState()

    val stripeLauncher = rememberLauncherForActivityResult(
        contract = PaymentSheetContract(), // when using non-deprecated way via rememberPaymentSheet(), there is infinite loop issue (when onSuccess and onFailed functions are
        // passed as params) and card modal does not appear
        onResult = {
            paymentMethodCreditCardViewModel.handlePaymentResult(
                it,
                {
                    Toast.makeText(context, "Payment successful", Toast.LENGTH_SHORT).show()
                    appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
                },
                {
                    Toast.makeText(context, "Payment failed", Toast.LENGTH_SHORT).show()
                    appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
                }
            )
        }
    )
    clientSecret?.let {
        val args = PaymentSheetContract.Args.createPaymentIntentArgs(it)
        stripeLauncher.launch(args)
        paymentMethodCreditCardViewModel.onPaymentLaunched()
    }

    val gbm by paymentMethodCreditCardViewModel.gbm.collectAsState()

    LaunchedEffect(Unit) {
        AppState.title.update { "Add credit - credit card" }
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = gbm,
            onValueChange = { value ->
                if (REGEX_NUMBER_DECIMAL.matches(value)) {
                    paymentMethodCreditCardViewModel.gbm.update { value }
                }
            },
            label = { Text("Gigabytes per month (GBM)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Decimal),
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = { paymentMethodCreditCardViewModel.makePayment() },
            enabled = gbm.isNotEmpty()
        ) {
            Text(text = "Create payment")
        }
    }
}
