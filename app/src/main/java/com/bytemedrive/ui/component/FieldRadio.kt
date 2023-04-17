package com.bytemedrive.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FieldRadio(
    modifier: Modifier = Modifier,
    item: RadioItem,
    selected: Boolean,
    onChangeValue: (value: String) -> Unit,
) =

    Column(
        modifier = modifier
            .height(48.dp)
            .selectable(
                selected = selected,
                onClick = { onChangeValue(item.value) },
                role = Role.RadioButton,
                enabled = item.enabled
            )
            .background(Color(0xC6FFFBFE))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null
            )
            Text(
                text = item.label,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (!item.subLabel.isNullOrBlank()) {
            Text(
                text = item.subLabel,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }

data class RadioItem(val value: String, val label: String, val subLabel: String?, val enabled: Boolean = true)