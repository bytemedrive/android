package com.bytemedrive.wallet.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bytemedrive.R
import com.bytemedrive.file.root.TopBarFile
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.FieldRadioGroup
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddCreditMethodScreen(
    addCreditMethodViewModel: AddCreditMethodViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    LaunchedEffect("initialize") {
        AppState.title.value = "Add credit"
        AppState.topBarComposable.value = { TopBarAppContentBack() }
    }

    val method by addCreditMethodViewModel.method.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FieldRadioGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 40.dp),
            items = AddCreditMethodViewModel.methodOptions,
            value = method,
            onChangeValue = { addCreditMethodViewModel.method.value = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    when(method) {
                        AddCreditMethodViewModel.methodOptions[0].value -> appNavigator.navigateTo(AppNavigator.NavTarget.PAYMENT_METHOD_CREDIT_CARD)
                        AddCreditMethodViewModel.methodOptions[1].value -> appNavigator.navigateTo(AppNavigator.NavTarget.PAYMENT_METHOD_CRYPTO_AMOUNT)
                        AddCreditMethodViewModel.methodOptions[2].value -> appNavigator.navigateTo(AppNavigator.NavTarget.PAYMENT_METHOD_CREDIT_CODE)
                    }
                }
            ) {
                Text(text = stringResource(R.string.common_next))
            }
        }
    }
}