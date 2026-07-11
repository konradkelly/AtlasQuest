package com.atlasquest.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.atlasquest.app.ui.screens.home.HomeScreen
import com.atlasquest.app.ui.screens.quiz.QuizScreen
import com.atlasquest.app.ui.screens.results.ResultsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Quiz : Screen("quiz")
    data object Results : Screen("results/{score}/{total}/{xp}") {
        fun createRoute(score: Int, total: Int, xp: Int) = "results/$score/$total/$xp"
    }
}

@Composable
fun AtlasQuestNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onPlay = { navController.navigate(Screen.Quiz.route) }
            )
        }
        composable(Screen.Quiz.route) {
            QuizScreen(
                onQuizComplete = { score, total, xpEarned ->
                    navController.navigate(Screen.Results.createRoute(score, total, xpEarned)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
        composable(Screen.Results.route) { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val total = backStackEntry.arguments?.getString("total")?.toIntOrNull() ?: 0
            val xpEarned = backStackEntry.arguments?.getString("xp")?.toIntOrNull() ?: 0
            ResultsScreen(
                score = score,
                total = total,
                xpEarned = xpEarned,
                onPlayAgain = {
                    navController.navigate(Screen.Quiz.route) { popUpTo(Screen.Home.route) }
                },
                onHome = { navController.popBackStack(Screen.Home.route, inclusive = false) }
            )
        }
    }
}
