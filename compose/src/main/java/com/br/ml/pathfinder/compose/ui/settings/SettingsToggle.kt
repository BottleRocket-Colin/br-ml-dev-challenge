package com.br.ml.pathfinder.compose.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.br.ml.pathfinder.compose.utils.Preview

@Composable
fun SettingsToggle(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(8.dp)
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewSettingsToggleOn() {
    Preview {
        SettingsToggle(
            label = "Label",
            checked = true,
            onChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsToggleOff() {
    Preview {
        SettingsToggle(
            label = "Label",
            checked = false,
            onChange = {}
        )
    }
}