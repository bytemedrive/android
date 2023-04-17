package com.bytemedrive.wallet

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
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.FieldRadioGroup
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddCreditMethodScreen(
    addCreditMethodViewModel: AddCreditMethodViewModel = koinViewModel(),
    appNavigator: AppNavigator = get(),
) {
    LaunchedEffect("initialize") {
        AppState.title.value = "Add credit"
    }

    val method by addCreditMethodViewModel.method.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        FieldRadioGroup(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 40.dp),
            items = AddCreditMethodViewModel.methodOptions,
            value = method,
            onChangeValue = { addCreditMethodViewModel.method.value = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, end = 24.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.ADD_CREDIT_CODE) }
            ) {
                Text(text = stringResource(R.string.common_next))
            }
        }
    }
}