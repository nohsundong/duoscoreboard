package com.myowndime.duoscoreboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myowndime.duoscoreboard.ui.history.HistoryRoute
import com.myowndime.duoscoreboard.ui.scoreboard.ScoreboardRoute
import com.myowndime.duoscoreboard.ui.settings.SettingsRoute

object Routes {
    const val SCOREBOARD: String = "scoreboard"
    const val HISTORY: String = "history"
    const val SETTINGS: String = "settings"
}

@Composable
fun DuoScoreboardNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SCOREBOARD,
        modifier = modifier,
    ) {
        composable(Routes.SCOREBOARD) {
            ScoreboardRoute(
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.HISTORY) {
            HistoryRoute(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
