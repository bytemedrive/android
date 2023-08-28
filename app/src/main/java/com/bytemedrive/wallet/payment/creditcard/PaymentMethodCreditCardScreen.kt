package com.bytemedrive.wallet.payment.creditcard

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bytemedrive.R
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import com.bytemedrive.stripe.TokenProvider
import com.bytemedrive.ui.component.NoPermission
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.Reader
import com.stripe.stripeterminal.log.LogLevel
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PaymentMethodCreditCardScreen(
    paymentMethodCreditCardViewModel: PaymentMethodCreditCardViewModel = koinViewModel(),
    appNavigator: AppNavigator = get(),
) {
    val context = LocalContext.current
    val screenPermissionsState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect("initialize") {
        AppState.title.value = "Add credit - credit card"

        screenPermissionsState.launchPermissionRequest()
    }

    if (!screenPermissionsState.status.isGranted) {
        NoPermission(stringResource(R.string.payment_method_credit_card_alert_permission_text), screenPermissionsState::launchPermissionRequest)
    } else {
        val listener = object : TerminalListener {
            override fun onUnexpectedReaderDisconnect(reader: Reader) {
                println(" Aaaaaaaaaaaaaaaaaaaaaaaaa")
            }
        }

        val tokenProvider = TokenProvider()

        if (!Terminal.isInitialized()) {
            Terminal.initTerminal(context, LogLevel.INFO, tokenProvider, listener)
        }


        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { paymentMethodCreditCardViewModel.createPaymentIntent() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Create payment")
            }
        }
    }
}