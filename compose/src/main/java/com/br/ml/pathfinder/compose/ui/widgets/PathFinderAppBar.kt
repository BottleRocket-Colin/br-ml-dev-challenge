package com.br.ml.pathfinder.compose.ui.widgets

import Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import com.bottlerocketstudios.launchpad.compose.widgets.slidingappbar.SlidingAppBar
import com.br.ml.pathfinder.compose.utils.toggle
import kotlinx.coroutines.launch

@Composable
fun PathfinderAppBar(
    scaffoldState: ScaffoldState,
    visible: Boolean,
    title: String,
    navIcon: ImageVector
) {
    val coroutineScope = rememberCoroutineScope()

    SlidingAppBar(
        visible = visible,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.h4
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.toggle()
                    }
                }
            ) {
                navIcon.Icon()
            }
        }
    )
}
