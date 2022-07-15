package com.br.ml.pathfinder.compose.resources

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val Peach = Color(0xFFF48174)
private val Purple = Color(0xFF7d6bb0)
private val Green = Color(0xFF00574B)
private val Pink = Color(0xFFD81B60)
val SplashBackground = Color(0xFFFBFAFB)


val LightColors = lightColors(
    background = Color.White,
    primary = Peach,
    primaryVariant = Purple,
    secondary = Green,
    secondaryVariant = Pink,
)

val DarkColors = darkColors()


@Composable
fun ProvideColors(
    colors: Colors,
    content: @Composable () -> Unit
) {
    val colorPalette = remember { colors }
    CompositionLocalProvider(LocalAppColors provides colorPalette, content = content)
}

// TODO update this to use compositionLocalOf when a set of dark mode colors has been added
private val LocalAppColors = staticCompositionLocalOf {
    LightColors
}
