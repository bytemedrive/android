package com.bytemedrive.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FieldRadioGroup(
    modifier: Modifier = Modifier,
    items: List<RadioItem>,
    value: String,
    onChangeValue: (value: String) -> Unit
) =
    Column(Modifier.selectableGroup()) {
        items.forEach {
            val selected = it.value == value

            FieldRadio(modifier, it, selected, onChangeValue)
        }
    }
