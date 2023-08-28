package com.bytemedrive.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bytemedrive.R

@Composable
fun NoPermission(text: String, requestPermission: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(0.4f),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.common_missing_permissions), style = MaterialTheme.typography.bodyLarge)
            Text(text = text)
            if (requestPermission != null) {
                Button(onClick = requestPermission) {
                    Text(text = stringResource(R.string.common_update_permissions))
                }
            }
        }
    }
}