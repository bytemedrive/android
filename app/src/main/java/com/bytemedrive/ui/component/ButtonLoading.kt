package com.bytemedrive.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ButtonLoading(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Button(modifier = modifier, enabled = enabled, onClick = onClick) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, )
        } else {
            content()
        }
    }
}
