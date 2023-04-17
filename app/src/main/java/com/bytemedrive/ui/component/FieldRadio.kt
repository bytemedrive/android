package com.bytemedrive.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (item.enabled) 1f else 0.5f)
            .selectable(
                selected = selected,
                onClick = { onChangeValue(item.value) },
                role = Role.RadioButton,
                enabled = item.enabled
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            modifier = Modifier.padding(horizontal = 18.dp),
            selected = selected,
            onClick = null
        )
        Column(
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (!item.subLabel.isNullOrBlank()) {
                Text(
                    text = item.subLabel,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }


    }

data class RadioItem(val value: String, val label: String, val subLabel: String?, val enabled: Boolean = true)