package com.br.ml.brpathfinder.ui

import Icon
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.br.ml.pathfinder.compose.navigation.Routes
import com.br.ml.pathfinder.domain.infrastructure.flow.MutableStateFlowDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.br.ml.brpathfinder.R
import com.br.ml.brpathfinder.navigation.mainNavGraph
import com.br.ml.pathfinder.compose.resources.Dimens
import com.br.ml.pathfinder.compose.resources.PathFinderTheme
import com.br.ml.pathfinder.compose.ui.widgets.NavDrawer
import com.br.ml.pathfinder.compose.ui.widgets.NavItem
import com.br.ml.pathfinder.compose.ui.widgets.PathfinderAppBar


class ComposeActivity : ComponentActivity() {
    private val activityViewModel: ComposeActivityViewModel by viewModel()

    /**
     *   EMPTY_TOOLBAR_TITLE is used to show toolbar without a title.
     */
    companion object {
        const val EMPTY_TOOLBAR_TITLE = " "
    }

    // Lazy initialized public interface that provides access to view model
    val controls by lazy { Controls(activityViewModel) }
    class Controls(viewModel: ComposeActivityViewModel) {
        var title by MutableStateFlowDelegate(viewModel.title)
        var topLevel by MutableStateFlowDelegate((viewModel.topLevel))
    }

    var navIntercept: (() -> Boolean)? = null

    @Composable
    fun <T : BaseViewModel> T.ConnectBaseViewModel(block: @Composable (T) -> Unit) {
        // Reset Controls
        controls.title = ""
        controls.topLevel = false
        navIntercept = null

        // TODO - Handle global event from BaseViewModel here.
        block.invoke(this)
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val navController = rememberNavController()
            val scaffoldState = rememberScaffoldState()
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            PathFinderTheme {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        PathfinderAppBar(
                            scaffoldState,
                            activityViewModel.showToolbar.collectAsState(initial = true).value,
                            activityViewModel.title.collectAsState().value,
                            Icons.Default.Menu
                        )
                    },
                    drawerContent = {
                        NavDrawer(
                            items = navItems,
                            currentRoute = currentRoute ?: "",
                            navController,
                            scaffoldState
                        )
                    }
                ) {
                    NavHost(navController = navController,startDestination = Routes.Main) {
                        mainNavGraph(navController = navController, activity = this@ComposeActivity)
                    }
                }
            }
        }
    }

    private val navItems = listOf(
        NavItem(
            route = Routes.Home
        ) {
            PathfinderNavItem(icon = Icons.Default.Home, itemText = getString(R.string.home_title))
        },
        NavItem(
            route = Routes.Settings
        ) {
            PathfinderNavItem(icon = Icons.Default.Settings, itemText = getString(R.string.settings_title))
        }
    )

    @Composable
    fun PathfinderNavItem(
        icon: ImageVector,
        itemText: String,
    ) {
        icon.Icon()

        Text(
            text = itemText,
            modifier = Modifier.padding(start = Dimens.grid_3)
        )
    }

}
