package com.bytemedrive.wallet.payment.creditcard

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.ButtonLoading
import com.bytemedrive.ui.component.FieldNumber
import com.stripe.android.paymentsheet.PaymentSheetContract
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PaymentMethodCreditCardScreen(
    paymentMethodCreditCardViewModel: PaymentMethodCreditCardViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    val context = LocalContext.current
    val activity = LocalContext.current as Activity

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
    paymentMethodCreditCardViewModel.clientSecret?.let {
        val args = PaymentSheetContract.Args.createPaymentIntentArgs(it)
        stripeLauncher.launch(args)
    }

    LaunchedEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        AppState.title.update { "Add credit - credit card" }
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
    }

    DisposableEffect(Unit) {
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FieldNumber(
                modifier = Modifier
                    .weight(2f)
                    .padding(end = 8.dp),
                value = paymentMethodCreditCardViewModel.amount,
                type = Long::class.java,
                onValueChange = { value ->
                    paymentMethodCreditCardViewModel.amount = value
                },
                label = { Text("Gigabytes per month (GBM)") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Decimal),
                keyboardActions = KeyboardActions(onDone = { paymentMethodCreditCardViewModel.makePayment() }),
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "~ ${
                    if (paymentMethodCreditCardViewModel.prices != null && paymentMethodCreditCardViewModel.amount != null) paymentMethodCreditCardViewModel.amount!!.times(
                        paymentMethodCreditCardViewModel.prices!!.gbmPriceInEur
                    ).toString() else "0"
                } EUR",
            )
        }

        ButtonLoading(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = { paymentMethodCreditCardViewModel.makePayment() },
            enabled = paymentMethodCreditCardViewModel.amount != null && !paymentMethodCreditCardViewModel.loading,
            loading = paymentMethodCreditCardViewModel.loading
        ) {
            Text(text = "Create payment")
        }
    }
}
