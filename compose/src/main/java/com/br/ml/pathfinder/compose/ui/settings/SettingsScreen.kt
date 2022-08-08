package com.br.ml.pathfinder.compose.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.br.ml.pathfinder.compose.resources.PathFinderTheme
import com.br.ml.pathfinder.compose.utils.asMutableState


class SettingsState(
    val vibrateActive: State<Boolean>,
    val onVibrateChanged: (Boolean) -> Unit,
)


@Composable
fun SettingScreen(state: SettingsState) {
    Column(Modifier.fillMaxSize()) {
        SettingsToggle(
            label = "Enable Vibration",
//            TODO - Move to strings.xml
            checked = state.vibrateActive.value,
            onChange = state.onVibrateChanged
        )
        Divider()
    }
}


@Preview(showSystemUi =  true)
@Composable
private fun PreviewSettingsScreen() {
    PathFinderTheme {
        SettingScreen(
            SettingsState(
                vibrateActive = true.asMutableState(),
                onVibrateChanged = {}
            )
        )
    }
}