package com.br.ml.pathfinder.compose.resources

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


@Composable
fun PathFinderTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColors else LightColors
    val configuration = LocalConfiguration.current
    val dimensions = if (configuration.screenWidthDp <= 360) smallDimensions else sw360Dimensions

    ProvideDimens(dimensions = dimensions) {
        ProvideColors(colors = colors) {
            MaterialTheme(
                colors = colors,
                typography = AppTypography
            ) {
                content()
            }
        }
    }
}

object PathFinderTheme {
    val colors: Colors
        @Composable
        get() = LocalAppColors.current

    val dimens: Dimensions
        @Composable
        get() = LocalAppDimens.current
}

val Dimens: Dimensions
    @Composable
    get() = PathFinderTheme.dimens

val Colors: Colors
    @Composable
    get() = PathFinderTheme.colors
