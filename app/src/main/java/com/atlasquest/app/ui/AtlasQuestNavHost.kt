package com.atlasquest.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atlasquest.app.data.model.Continent
import com.atlasquest.app.data.model.Region
import com.atlasquest.app.ui.screens.globe.GlobeScreen
import com.atlasquest.app.ui.screens.home.HomeScreen
import com.atlasquest.app.ui.screens.quiz.QuizScreen
import com.atlasquest.app.ui.screens.subregion.SubregionGlobeScreen
import com.atlasquest.app.ui.screens.results.ResultsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Globe : Screen("globe")
    data object RegionMap : Screen("map/{continent}") {
        fun createRoute(continent: String) = "map/$continent"
    }
    data object Quiz : Screen("quiz/{region}") {
        fun createRoute(region: String) = "quiz/$region"
    }
    data object Results : Screen("results/{score}/{total}") {
        fun createRoute(score: Int, total: Int) = "results/$score/$total"
    }
}

@Composable
fun AtlasQuestNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPlay = { navController.navigate(Screen.Globe.route) }
            )
        }
        composable(Screen.Globe.route) {
            GlobeScreen(
                onContinentSelected = { continent ->
                    navController.navigate(Screen.RegionMap.createRoute(continent.name))
                }
            )
        }
        composable(Screen.RegionMap.route) { backStackEntry ->
            val continentName = backStackEntry.arguments?.getString("continent")
            val continent = continentName?.let { runCatching { Continent.valueOf(it) }.getOrNull() }
            if (continent != null) {
                SubregionGlobeScreen(
                    continent = continent,
                    onStartQuiz = { regionId ->
                        navController.navigate(Screen.Quiz.createRoute(regionId))
                    }
                )
            }
        }
        composable(Screen.Quiz.route) { backStackEntry ->
            val regionId = backStackEntry.arguments?.getString("region") ?: ""
            val region = Region.fromId(regionId)
            QuizScreen(
                region = region,
                onQuizComplete = { score, total ->
                    navController.navigate(Screen.Results.createRoute(score, total)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.Results.route) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            ResultsScreen(
                score = score,
                total = total,
                onPlayAgain = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                onHome = { navController.popBackStack(Screen.Home.route, inclusive = false) }
            )
        }
    }
}
