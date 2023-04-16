package com.bytemedrive.application

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bytemedrive.R

@Composable
fun GeneralError(message: String?) = Alert(
    title = stringResource(R.string.common_error),
    text = message ?: stringResource(R.string.error_alert_general_error_text)
)

@Composable
fun NoInternet() = Alert(title = stringResource(R.string.error_alert_no_internet_title), text = stringResource(R.string.error_alert_no_internet_text))

@Composable
fun RequestFailed() {
    val title = stringResource(R.string.error_alert_request_failed_title)
    val text = stringResource(R.string.error_alert_request_failed_text)

    Alert(title = title, text = text)
}

@Composable
private fun Alert(title: String, text: String) =
    AlertDialog(
        onDismissRequest = { GlobalExceptionHandler.clear() },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { Button(onClick = { GlobalExceptionHandler.clear() }) { Text(stringResource(R.string.common_ok)) } }
    )

