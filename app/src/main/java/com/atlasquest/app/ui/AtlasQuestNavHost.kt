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
    data object Quiz : Screen("quiz/{mode}") {
        fun createRoute(mode: String) = "quiz/$mode"
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
                onStartQuiz = { mode ->
                    navController.navigate(Screen.Quiz.createRoute(mode))
                }
            )
        }
        composable(Screen.Quiz.route) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "classic"
            QuizScreen(
                mode = mode,
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
