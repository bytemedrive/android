package com.bytemedrive.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldCheckbox(
    modifier: Modifier = Modifier,
    key: String,
    checked: Boolean,
    onChangeValue: (key: String, checked: Boolean) -> Unit,
    content: @Composable() () -> Unit,
) =
    Row(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = { onChangeValue(key, !checked) },
                role = Role.Checkbox
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.padding(end = 12.dp))
        content()
    }
