package com.bytemedrive.wallet.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.FieldRadioGroup
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AddCreditMethodScreen(
    addCreditMethodViewModel: AddCreditMethodViewModel = koinViewModel(),
    appNavigator: AppNavigator = koinInject(),
) {
    LaunchedEffect(Unit) {
        AppState.title.update { "Add credit" }
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
    }

    val scrollState = rememberScrollState()

    val method by addCreditMethodViewModel.method.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FieldRadioGroup(
            modifier = Modifier.fillMaxWidth(),
            items = AddCreditMethodViewModel.methodOptions,
            value = method,
            onChangeValue = { value ->  addCreditMethodViewModel.method.update { value } }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
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