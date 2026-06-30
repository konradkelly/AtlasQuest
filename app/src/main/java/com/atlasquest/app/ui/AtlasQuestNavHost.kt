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
    data object Results : Screen("results/{region}/{score}/{total}/{xp}") {
        fun createRoute(region: String, score: Int, total: Int, xp: Int) =
            "results/$region/$score/$total/$xp"
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
                },
                onStartQuiz = { regionId ->
                    navController.navigate(Screen.Quiz.createRoute(regionId))
                }
            )
        }
        composable(Screen.RegionMap.route) { backStackEntry ->
            val continentName = backStackEntry.arguments?.getString("continent")
            val continent = continentName?.let { runCatching { Continent.valueOf(it) }.getOrNull() }
            if (continent != null) {
                SubregionGlobeScreen(
                    continent = continent,
                    onBack = { navController.popBackStack() },
                    onStartQuiz = { regionId ->
                        navController.navigate(Screen.Quiz.createRoute(regionId))
                    }
                )
            }
        }
        composable(Screen.Quiz.route) { backStackEntry ->
            // QuizViewModel also reads the {region} nav arg from its SavedStateHandle.
            val regionId = backStackEntry.arguments?.getString("region").orEmpty()
            QuizScreen(
                onQuizComplete = { score, total, xpEarned ->
                    navController.navigate(Screen.Results.createRoute(regionId, score, total, xpEarned)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.Results.route) { backStackEntry ->
            val region = backStackEntry.arguments?.getString("region")?.let(Region::fromId)
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            val xpEarned = backStackEntry.arguments?.getString("xp")?.toIntOrNull() ?: 0
            ResultsScreen(
                score = score,
                total = total,
                xpEarned = xpEarned,
                onPlayAgain = {
                    when {
                        // Eurasia is picked straight from the globe, so there's no
                        // subregion screen to return to.
                        region == null || region.topLevel ->
                            navController.navigate(Screen.Globe.route) { popUpTo(Screen.Home.route) }
                        else ->
                            navController.navigate(Screen.RegionMap.createRoute(region.continent.name)) {
                                popUpTo(Screen.Home.route)
                            }
                    }
                },
                onHome = { navController.popBackStack(Screen.Home.route, inclusive = false) }
            )
        }
    }
}
