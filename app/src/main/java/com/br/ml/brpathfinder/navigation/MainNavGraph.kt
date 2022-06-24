package com.br.ml.brpathfinder.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.br.ml.brpathfinder.ui.ComposeActivity
import com.br.ml.brpathfinder.ui.settings.settingsComposable
import com.br.ml.brpathfinder.ui.splash.splashComposable
import com.br.ml.pathfinder.compose.navigation.Routes

fun NavGraphBuilder.mainNavGraph(navController: NavController, activity: ComposeActivity) {
    with(activity) {
        navigation(startDestination = Routes.Splash, route = Routes.Main) {
            splashComposable(this, navController)
            settingsComposable(this, navController)
        }
    }
}