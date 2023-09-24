package com.bytemedrive.wallet.credit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCryptoMethodAmountScreen(
    addCryptoMethodViewModel: AddCryptoMethodAmountViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {

    LaunchedEffect("initialize") {
        AppState.title.value = "Monero payment"
    }

    val amount by addCryptoMethodViewModel.amount.collectAsState()
    val prices by addCryptoMethodViewModel.prices.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 36.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(3f).padding(end = 8.dp),
                value = if (amount == null) "" else amount.toString(),
                onValueChange = {
                    if (it.matches(Regex("\\d*"))) {
                        addCryptoMethodViewModel.amount.value = it.toIntOrNull()
                    }
                },
                label = { Text(text = "Storage amount (GBM)") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = { }),
            )
            Text(text = "~ ${if (prices != null && amount != null) amount!!.times(prices!!.gbmPriceInXmr).toString() else "0"} XMR", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(
                onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.ADD_CRYPTO_METHOD_PAYMENT, mapOf("storageAmount" to amount.toString())) },
                enabled = amount != null
            ) {
                Text(text = "Next")
            }
        }
    }
}
