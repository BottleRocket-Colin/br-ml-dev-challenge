package com.br.ml.pathfinder.compose.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.br.ml.pathfinder.compose.resources.Dimens
import kotlinx.coroutines.launch

class NavItem(
    val route: String,
    val onClick: (() -> Unit)? = null,
    val content: @Composable (selected: Boolean) -> Unit,
)


@Composable fun NavDrawer(
    items: List<NavItem>,
    currentRoute: String,
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    Column (modifier = modifier) {
        // Custom Header
        header()

        // Nav item list
        //  TODO - Update for nested nav items.
        items.forEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(Dimens.minimum_touch_target)
                    .fillMaxWidth()
                    .clickable {
                        // Run custom onClick
                        it.onClick?.invoke() ?: run {
                            // Or navigate to route by default
                            navController.navigate(it.route)
                            coroutineScope.launch {
                                scaffoldState.drawerState.close()
                            }
                        }
                    }
            ) {
//                TODO - Make color provider for selected....
                it.content(currentRoute == it.route)
            }
        }

        // Push Footer to bottom
        Spacer(Modifier.weight(1f))

        // Custom Footer
        footer()
    }


}

// TODO - preview