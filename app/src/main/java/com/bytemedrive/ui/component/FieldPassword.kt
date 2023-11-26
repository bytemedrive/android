package com.bytemedrive.ui.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FieldPassword(
    modifier: Modifier = Modifier,
    label: String,
    value: CharArray,
    keyboardOptions: KeyboardOptions,
    onValueChange: (CharArray) -> Unit,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    showEye: Boolean? = true,
    focusRequester: FocusRequester = FocusRequester(),
) {
    var passwordVisibility by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.joinToString(""),
        onValueChange = { onValueChange(it.toCharArray()) },
        label = { Text(label) },
        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val visibilityIcon = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

            if (showEye == true) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            routePointerChangesTo(
                                onDown = { _ -> passwordVisibility = true },
                                onUp = { _ -> passwordVisibility = false }
                            )
                        }
                ) {
                    Icon(visibilityIcon, contentDescription = if (passwordVisibility) "Hide password" else "Show password")
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardOptions.capitalization, keyboardOptions.autoCorrect, KeyboardType.Password, keyboardOptions.imeAction),
        keyboardActions = keyboardActions,
        modifier = modifier
            .autofill(
                autofillTypes = listOf(AutofillType.Password),
                onFill = { onValueChange(it.toCharArray()) },
            )
            .focusRequester(focusRequester),
    )
}

private suspend fun PointerInputScope.routePointerChangesTo(
    onDown: (PointerInputChange) -> Unit = {},
    onMove: (PointerInputChange) -> Unit = {},
    onUp: (PointerInputChange) -> Unit = {}
) {
    awaitEachGesture {
        do {
            val event = awaitPointerEvent()
            event.changes.forEach {
                when (event.type) {
                    PointerEventType.Press -> onDown(it)
                    PointerEventType.Move -> onMove(it)
                    PointerEventType.Release -> onUp(it)
                }
                it.consume()
            }
        } while (event.changes.any { it.pressed })
    }
}