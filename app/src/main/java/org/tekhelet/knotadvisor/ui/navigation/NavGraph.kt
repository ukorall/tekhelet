package org.tekhelet.knotadvisor.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.tekhelet.knotadvisor.model.JourneyStation
import org.tekhelet.knotadvisor.model.Topic
import org.tekhelet.knotadvisor.ui.AppViewModel
import org.tekhelet.knotadvisor.ui.screens.*

object Routes {
    const val HOME = "home"
    const val JOURNEY = "journey"
    const val WHETHER = "whether"
    const val HOW_MANY = "how_many"
    const val HOW_HOME = "how_home"
    const val TYING_GUIDE = "tying_guide"
    const val EXTRAS = "extras"
    const val QUESTIONNAIRE = "questionnaire"
    const val LOOK = "look"
    const val TIE_WIND = "tie_wind"
    const val TIE_VISUAL = "tie_visual"
    const val RESULTS = "results"
    const val LIBRARY = "library"
    const val TREE = "tree"
    const val BUILDER = "builder"
    const val HISTORY = "history"
    const val FEEDBACK = "feedback"
    const val FINGERPRINT = "fingerprint"
    const val METHOD_DETAIL = "method_detail/{methodId}"
    fun methodDetail(id: String) = "method_detail/$id"
}

private fun titleFor(route: String?): String = when {
    route == null || route == Routes.HOME -> "בורר קשירת תכלת"
    route == Routes.JOURNEY -> "המסע"
    route == Routes.WHETHER -> Topic.WHETHER.title
    route == Routes.HOW_MANY -> Topic.HOW_MANY.title
    route == Routes.HOW_HOME -> Topic.HOW.title
    route == Routes.TYING_GUIDE -> Topic.TYING_GUIDE.title
    route == Routes.EXTRAS -> Topic.EXTRAS.title
    route == Routes.QUESTIONNAIRE -> "השאלון"
    route == Routes.LOOK -> "מה יפה בעיניך"
    route == Routes.TIE_WIND -> "שאלת המשך"
    route == Routes.TIE_VISUAL -> "מה יפה בעיניך"
    route == Routes.RESULTS -> "התוצאות"
    route == Routes.LIBRARY -> "ספריית השיטות"
    route == Routes.TREE -> "מפת השיטות"
    route == Routes.BUILDER -> "בונה ההרכב"
    route == Routes.HISTORY -> "ייעוצים קודמים"
    route == Routes.FEEDBACK -> "הערה בשבילי"
    route == Routes.FINGERPRINT -> "טביעת אצבע"
    route.startsWith("method_detail") -> "פירוט שיטה"
    else -> "בורר קשירת תכלת"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(viewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val atHome = route == null || route == Routes.HOME

    Scaffold(
        topBar = {
            // סרגל עליון שקט ונמוך: שם המסך יושב עכשיו בתוך הדף עצמו (PageHeader),
            // בגופן הספר ובגודל שיש לו מקום לנשום בעברית. הסרגל נשאר רק בשביל
            // הניווט, ולכן הוא בצבע הנייר ובלי כותרת גדולה שתתחרה בכותרת האמיתית.
            TopAppBar(
                title = {
                    Text(
                        titleFor(route),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    if (!atHome) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה למסך הקודם")
                        }
                    }
                },
                actions = {
                    if (!atHome) {
                        IconButton(onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                            }
                        }) {
                            Icon(Icons.Filled.Home, contentDescription = "חזרה למסך הבית")
                        }
                    }
                }
            )
        }
    ) { inner ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(inner)
        ) {
            composable(Routes.HOME) {
                TopicsHomeScreen(
                    contentError = viewModel.contentError,
                    journey = viewModel.journey,
                    consultingFor = viewModel.consultingFor,
                    mode = viewModel.mode,
                    onSetMode = viewModel::updateMode,
                    onOpenFingerprint = { navController.navigate(Routes.FINGERPRINT) },
                    onSelectTopic = { navController.navigate(routeForTopic(it)) },
                    onStartJourney = { viewModel.startJourney(); navController.navigate(Routes.JOURNEY) },
                    onContinueJourney = { navController.navigate(Routes.JOURNEY) },
                    onOpenHistory = { navController.navigate(Routes.HISTORY) },
                    onOpenFeedback = { navController.navigate(Routes.FEEDBACK) },
                    onSetConsultingFor = viewModel::updateConsultingFor
                )
            }

            composable(Routes.FINGERPRINT) { FingerprintScreen() }

            composable(Routes.JOURNEY) {
                JourneyScreen(
                    journey = viewModel.journey,
                    consultingFor = viewModel.consultingFor,
                    onOpenStation = { station ->
                        viewModel.goToStation(station)
                        navController.navigate(routeForStation(station))
                    },
                    onCompleteCurrent = { viewModel.completeCurrentStation() },
                    onPause = { viewModel.pauseJourney(); navController.popBackStack() },
                    onReset = { viewModel.resetJourney() }
                )
            }

            composable(Routes.WHETHER) { TopicNotesScreen(Topic.WHETHER) }
            composable(Routes.HOW_MANY) { TopicNotesScreen(Topic.HOW_MANY) }

            composable(Routes.HOW_HOME) {
                HowHomeScreen(
                    onStartQuestionnaire = {
                        viewModel.resetQuestionnaire()
                        navController.navigate(Routes.QUESTIONNAIRE)
                    },
                    onBrowseLibrary = { navController.navigate(Routes.LIBRARY) },
                    onOpenTree = { navController.navigate(Routes.TREE) },
                    onOpenBuilder = { navController.navigate(Routes.BUILDER) }
                )
            }

            composable(Routes.TYING_GUIDE) {
                TyingGuideScreen(
                    methods = viewModel.allMethods,
                    initialComposition = viewModel.customComposition,
                    onOpenBuilder = { navController.navigate(Routes.BUILDER) }
                )
            }

            composable(Routes.EXTRAS) {
                ExtrasScreen(
                    onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                    onOpenTree = { navController.navigate(Routes.TREE) }
                )
            }

            composable(Routes.BUILDER) { CompositionBuilderScreen(viewModel) }

            composable(Routes.TREE) {
                MethodTreeScreen(
                    methods = viewModel.allMethods,
                    onOpenMethod = { navController.navigate(Routes.methodDetail(it)) }
                )
            }

            composable(Routes.QUESTIONNAIRE) {
                QuestionnaireScreen(
                    viewModel = viewModel,
                    onFinishedPrimaryQuestions = { navController.navigate(Routes.LOOK) }
                )
            }

            composable(Routes.LOOK) {
                LookPreferenceScreen(
                    selected = viewModel.lookPreference,
                    knotPreference = viewModel.knotLookPreference,
                    onSelect = viewModel::chooseLook,
                    onSelectKnot = viewModel::chooseKnotLook,
                    onContinue = {
                        viewModel.computeResults()
                        navigateAfterPrimaryQuestions(navController, viewModel)
                    }
                )
            }

            composable(Routes.TIE_WIND) {
                WindCountTieBreakerScreen {
                    navigateAfterWindTieBreaker(navController, viewModel)
                }
            }

            composable(Routes.TIE_VISUAL) {
                VisualTieBreakerScreen(viewModel) {
                    navController.navigate(Routes.RESULTS) {
                        popUpTo(Routes.LOOK) { inclusive = true }
                    }
                }
            }

            composable(Routes.RESULTS) {
                ResultsScreen(
                    viewModel = viewModel,
                    onOpenDetail = { navController.navigate(Routes.methodDetail(it)) },
                    onFinalize = { methodId ->
                        viewModel.saveFinalChoice(methodId)
                        navController.navigate(Routes.methodDetail(methodId))
                    }
                )
            }

            composable(Routes.LIBRARY) {
                MethodsLibraryScreen(
                    methods = viewModel.allMethods,
                    onSelect = { navController.navigate(Routes.methodDetail(it)) }
                )
            }

            composable(Routes.HISTORY) {
                val consultations by viewModel.history.collectAsState(initial = emptyList())
                HistoryScreen(
                    consultations = consultations,
                    methods = viewModel.allMethods,
                    onDelete = viewModel::deleteConsultation
                )
            }

            composable(Routes.FEEDBACK) {
                FeedbackScreen(viewModel, fromScreen = "home")
            }

            composable(Routes.METHOD_DETAIL) { entry ->
                val methodId = entry.arguments?.getString("methodId")
                val method = viewModel.allMethods.find { it.id == methodId }
                if (method != null) {
                    MethodDetailScreen(
                        method = method,
                        onUseInGuide = {
                            viewModel.loadCompositionFrom(method)
                            navController.navigate(Routes.TYING_GUIDE)
                        }
                    )
                }
            }
        }
    }
}

private fun routeForTopic(topic: Topic): String = when (topic) {
    Topic.WHETHER -> Routes.WHETHER
    Topic.HOW_MANY -> Routes.HOW_MANY
    Topic.HOW -> Routes.HOW_HOME
    Topic.TYING_GUIDE -> Routes.TYING_GUIDE
    Topic.EXTRAS -> Routes.EXTRAS
}

private fun routeForStation(station: JourneyStation): String = routeForTopic(station.topic)

private fun navigateAfterPrimaryQuestions(navController: NavHostController, viewModel: AppViewModel) {
    val destination = when {
        viewModel.showWindCountTieBreaker -> Routes.TIE_WIND
        viewModel.showVisualTieBreaker -> Routes.TIE_VISUAL
        else -> Routes.RESULTS
    }
    navController.navigate(destination) {
        popUpTo(Routes.LOOK) { inclusive = destination == Routes.RESULTS }
    }
}

private fun navigateAfterWindTieBreaker(navController: NavHostController, viewModel: AppViewModel) {
    viewModel.dismissWindCountTieBreaker()
    val destination = if (viewModel.showVisualTieBreaker) Routes.TIE_VISUAL else Routes.RESULTS
    navController.navigate(destination) {
        popUpTo(Routes.LOOK) { inclusive = destination == Routes.RESULTS }
    }
}
