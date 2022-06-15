package com.br.ml.brpathfinder.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class ComposeActivityViewModel : BaseViewModel() {
    // UI
    val title = MutableStateFlow("")
    val showToolbar = title.map { it.isNotEmpty() }
    val topLevel = MutableStateFlow(false)
}
