package com.br.ml.brpathfinder.di

import com.br.ml.brpathfinder.ui.ComposeActivityViewModel
import com.br.ml.brpathfinder.ui.main.MainViewModel
import com.br.ml.brpathfinder.ui.settings.SettingsViewModel
import com.br.ml.brpathfinder.utils.preferences.PreferencesImplementation
import com.br.ml.pathfinder.domain.utils.PreferencesInterface
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

val AppModule = module {
    viewModel { SettingsViewModel() }
    viewModel { ComposeActivityViewModel() }
    viewModel { MainViewModel() }

    single<PreferencesInterface> { PreferencesImplementation() }
}
