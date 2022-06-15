package com.br.ml.brpathfinder.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.br.ml.pathfinder.compose.navigation.Routes
import com.br.ml.pathfinder.domain.infrastructure.flow.MutableStateFlowDelegate
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.br.ml.pathfinder.compose.resources.PathFinderTheme


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
//                        ArchAppBar(
//                            state = activityViewModel.toArchAppBarState(),
//                            scaffoldState = scaffoldState,
//                            navController = navController,
//                            navIntercept = navIntercept
//                        )
                    },
                ) {
                    NavHost(navController = navController,startDestination = Routes.Main) {
//                        mainNavGraph(navController = navController, activity = this@ComposeActivity)
                    }
                }
            }
        }
    }

}
