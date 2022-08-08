package com.br.ml.brpathfinder.ui.splash

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.br.ml.brpathfinder.ui.ComposeActivity
import com.br.ml.pathfinder.compose.navigation.Routes
import com.br.ml.pathfinder.compose.ui.splash.SplashScreen

fun ComposeActivity.splashComposable(navGraphBuilder: NavGraphBuilder, navController: NavController) {
    navGraphBuilder.composable(Routes.Splash) {
        SplashScreen {
            navController.navigate(Routes.Settings)
        }
    }
}