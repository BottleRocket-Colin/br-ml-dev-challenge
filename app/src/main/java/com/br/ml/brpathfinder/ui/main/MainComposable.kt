package com.br.ml.brpathfinder.ui.main

import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.ui.ComposeActivity
import com.br.ml.pathfinder.compose.navigation.Routes
import org.koin.androidx.viewmodel.ext.android.getViewModel

fun ComposeActivity.mainComposable(navGraphBuilder: NavGraphBuilder, navController: NavController) {
    navGraphBuilder.composable(Routes.Home) {
        val vm: MainViewModel = getViewModel()

        vm.ConnectBaseViewModel {
//            SettingScreen(state = )
        }

        controls.title = stringResource(id = R.string.home_title)
    }

}