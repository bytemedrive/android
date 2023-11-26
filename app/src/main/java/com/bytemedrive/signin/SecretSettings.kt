package com.bytemedrive.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bytemedrive.R
import com.bytemedrive.application.sharedPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretSettings(onCloseModal: () -> Unit) {
    var backendUrl by remember { mutableStateOf(sharedPreferences.backendUrl) }

    Dialog(onDismissRequest = {}) {
        Surface(modifier = Modifier.wrapContentWidth().wrapContentHeight(), shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 16.dp)
            ) {
                Text(text = stringResource(R.string.secret_settings_title))
                OutlinedTextField(
                    value = backendUrl,
                    onValueChange = { backendUrl = it },
                    label = { Text(stringResource(R.string.secret_settings_field_backend_url_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onCloseModal()
                            sharedPreferences.backendUrlClear()
                        },
                    ) { Text(stringResource(R.string.common_button_label_reset)) }
                    TextButton(
                        onClick = {
                            onCloseModal()
                            sharedPreferences.backendUrl = backendUrl
                        },
                    ) { Text(stringResource(R.string.common_button_label_confirm)) }
                }
            }
        }
    }
}
