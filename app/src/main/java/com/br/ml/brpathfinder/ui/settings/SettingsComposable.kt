package com.br.ml.brpathfinder.ui.settings

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.ui.ComposeActivity
import com.br.ml.pathfinder.compose.navigation.Routes
import com.br.ml.pathfinder.compose.ui.settings.SettingScreen
import org.koin.androidx.viewmodel.ext.android.getViewModel

fun ComposeActivity.settingsComposable(navGraphBuilder: NavGraphBuilder) {
    navGraphBuilder.composable(Routes.Settings) {
        val vm: SettingsViewModel = getViewModel()

        vm.ConnectBaseViewModel {
            SettingScreen(state = it.toState())
        }

        controls.title = stringResource(id = R.string.settings_title)
    }
}
