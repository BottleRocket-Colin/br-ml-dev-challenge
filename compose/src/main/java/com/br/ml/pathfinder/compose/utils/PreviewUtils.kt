package com.br.ml.pathfinder.compose.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.br.ml.pathfinder.compose.resources.PathFinderTheme


@Composable
fun Preview(content: @Composable () -> Unit) = PathFinderTheme(content = content)

@SuppressLint("UnrememberedMutableState")
@Composable
fun <T> T.asMutableState() = mutableStateOf(this)
