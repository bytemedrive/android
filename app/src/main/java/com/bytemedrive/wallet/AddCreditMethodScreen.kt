package com.bytemedrive.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bytemedrive.R
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.ui.component.FieldRadioGroup
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddCreditMethodScreen(
    addCreditMethodViewModel: AddCreditMethodViewModel = koinViewModel(),
    appNavigator: AppNavigator = get()
) {

    val method by addCreditMethodViewModel.method.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        FieldRadioGroup(
            items = AddCreditMethodViewModel.methodOptions,
            value = method,
            onChangeValue = { addCreditMethodViewModel.method.value = it }
        )

        Button(
            onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.ADD_CREDIT_CODE) }
        ) {
            Text(text = stringResource(R.string.common_next))
        }
    }
}