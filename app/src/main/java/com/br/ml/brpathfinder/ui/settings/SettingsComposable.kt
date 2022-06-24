package com.br.ml.brpathfinder.ui.settings

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.br.ml.brpathfinder.ui.ComposeActivity
import com.br.ml.pathfinder.compose.navigation.Routes
import com.br.ml.pathfinder.compose.ui.settings.SettingScreen
import org.koin.androidx.viewmodel.ext.android.getViewModel

fun ComposeActivity.settingsComposable(navGraphBuilder: NavGraphBuilder, navController: NavController) {
    navGraphBuilder.composable(Routes.Settings) {
        val vm: SettingsViewModel = getViewModel()

        vm.ConnectBaseViewModel {
            SettingScreen(state = it.toState())
        }

        //  TODO - Strings.xml
        controls.title = "Settings"
    }
}
