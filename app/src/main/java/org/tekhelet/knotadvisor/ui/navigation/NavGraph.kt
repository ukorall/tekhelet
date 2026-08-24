package org.tekhelet.knotadvisor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.tekhelet.knotadvisor.ui.AppViewModel
import org.tekhelet.knotadvisor.ui.screens.*

private object Routes {
    const val HOME = "home"
    const val QUESTIONNAIRE = "questionnaire"
    const val TIE_WIND = "tie_wind"
    const val TIE_VISUAL = "tie_visual"
    const val RESULTS = "results"
    const val LIBRARY = "library"
    const val HISTORY = "history"
    const val METHOD_DETAIL = "method_detail/{methodId}"
    fun methodDetail(id: String) = "method_detail/$id"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartQuestionnaire = {
                    viewModel.resetQuestionnaire()
                    navController.navigate(Routes.QUESTIONNAIRE)
                },
                onBrowseLibrary = { navController.navigate(Routes.LIBRARY) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.QUESTIONNAIRE) {
            QuestionnaireScreen(
                viewModel = viewModel,
                onFinishedPrimaryQuestions = {
                    viewModel.computeResults()
                    navigateAfterPrimaryQuestions(navController, viewModel)
                }
            )
        }

        composable(Routes.TIE_WIND) {
            WindCountTieBreakerScreen(
                viewModel = viewModel,
                onContinue = {
                    navigateAfterWindTieBreaker(navController, viewModel)
                }
            )
        }

        composable(Routes.TIE_VISUAL) {
            VisualTieBreakerScreen(
                viewModel = viewModel,
                onChoiceMade = {
                    navController.navigate(Routes.RESULTS) {
                        popUpTo(Routes.QUESTIONNAIRE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESULTS) {
            ResultsScreen(
                viewModel = viewModel,
                onOpenDetail = { methodId -> navController.navigate(Routes.methodDetail(methodId)) },
                onFinalize = { methodId ->
                    viewModel.saveFinalChoice(methodId)
                    navController.navigate(Routes.methodDetail(methodId)) {
                        popUpTo(Routes.RESULTS) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.LIBRARY) {
            MethodsLibraryScreen(
                methods = viewModel.allMethods,
                onSelect = { methodId -> navController.navigate(Routes.methodDetail(methodId)) }
            )
        }

        composable(Routes.HISTORY) {
            val consultations by viewModel.history.collectAsState(initial = emptyList())
            HistoryScreen(consultations = consultations, methods = viewModel.allMethods)
        }

        composable(Routes.METHOD_DETAIL) { backStackEntry ->
            val methodId = backStackEntry.arguments?.getString("methodId")
            val method = viewModel.allMethods.find { it.id == methodId }
            if (method != null) {
                MethodDetailScreen(method = method)
            }
        }
    }
}

private fun navigateAfterPrimaryQuestions(navController: NavHostController, viewModel: AppViewModel) {
    val destination = when {
        viewModel.showWindCountTieBreaker -> Routes.TIE_WIND
        viewModel.showVisualTieBreaker -> Routes.TIE_VISUAL
        else -> Routes.RESULTS
    }
    navController.navigate(destination) {
        popUpTo(Routes.QUESTIONNAIRE) { inclusive = destination == Routes.RESULTS }
    }
}

private fun navigateAfterWindTieBreaker(navController: NavHostController, viewModel: AppViewModel) {
    viewModel.dismissWindCountTieBreaker()
    val destination = if (viewModel.showVisualTieBreaker) Routes.TIE_VISUAL else Routes.RESULTS
    navController.navigate(destination) {
        popUpTo(Routes.QUESTIONNAIRE) { inclusive = destination == Routes.RESULTS }
    }
}
