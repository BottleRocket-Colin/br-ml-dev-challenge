package com.br.ml.pathfinder.compose.utils

import androidx.compose.material.DrawerState

suspend fun DrawerState.toggle() = if (isOpen) close() else open()