package com.bytemedrive.ui.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNCHECKED_CAST")
@Composable
fun <T: Number?>FieldNumber(
    modifier: Modifier = Modifier,
    value: Any?,
    type: Class<T>,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    label: @Composable (() -> Unit)? = null,
    onValueChange: (value: T?) -> Unit,
) {
    var innerValue by remember { mutableStateOf(value?.toString().orEmpty()) }

    OutlinedTextField(
        modifier = modifier,
        label = label,
        value = innerValue,
        onValueChange = { newValue ->
            when (type) {
                Int::class.java -> {
                    if (Regex("(^\\d*$)").matches(newValue)) {
                        innerValue = newValue
                        onValueChange(newValue.toIntOrNull() as T)
                    }
                }
                Long::class.java -> {
                    if (Regex("(^\\d*$)").matches(newValue)) {
                        innerValue = newValue
                        onValueChange(newValue.toLongOrNull() as T)
                    }
                }
                Float::class.java -> {
                    innerValue = newValue

                    if (Regex("(^\\d*$)|(^\\d+[.|,]\\d*$)").matches(newValue)) {
                        onValueChange(newValue.replace(",", ".").toFloatOrNull() as T)
                    }
                }
                Double::class.java -> {
                    innerValue = newValue

                    if (Regex("(^\\d*$)|(^\\d+[.|,]\\d*$)").matches(newValue)) {
                        onValueChange(newValue.replace(",", ".").toDoubleOrNull() as T)
                    }
                }
                else -> {
                    innerValue = newValue
                }

            }
        },
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
    )
}