package com.bytemedrive.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.bytemedrive.R
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCodeScreen(
    addCreditCodeViewModel: AddCreditCodeViewModel = koinViewModel(),
    appNavigator: AppNavigator = get()
) {

    val code by addCreditCodeViewModel.code.collectAsState()

    Column {
        OutlinedTextField(
            value = code,
            onValueChange = { addCreditCodeViewModel.code.value = it },
            label = { Text(text = "Code") }
        )

        Row {
            Button(
                onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.ADD_CREDIT_METHOD) }
            ) {
                Text(text = stringResource(R.string.common_back))
            }
            Button(
                onClick = { addCreditCodeViewModel.redeemCoupon(AppState.customer.value?.wallet!!, code) }
            ) {
                Text(text = "Validate your code")
            }
        }

    }
}