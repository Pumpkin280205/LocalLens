package com.locallens

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.locallens.data.model.Article
import com.locallens.ui.screens.ArticleDetailScreen
import com.locallens.ui.screens.HomeScreen
import com.locallens.ui.screens.HomeViewModel
import com.locallens.ui.screens.ResultsScreen
import com.locallens.ui.screens.ResultsViewModel
import com.locallens.ui.screens.Screen
import com.locallens.ui.theme.LocalLensTheme
import com.locallens.utils.LocationPermissions

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }

    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            homeViewModel.onUseMyLocation()
        } else {
            homeViewModel.onLocationPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = Screen.Home.route) {
                        composable(route = Screen.Home.route) {
                            val viewModel: HomeViewModel = viewModel()
                            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                            HomeScreen(
                                uiState = uiState,
                                onUseMyLocation = {
                                    if (LocationPermissions.hasLocationPermission(this@MainActivity)) {
                                        viewModel.onUseMyLocation()
                                    } else {
                                        requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                },
                                onCountrySelected = { viewModel.onCountrySelected(it) },
                                onLoadNews = {
                                    val countryCode = uiState.selectedCountryCode
                                    navController.navigate(
                                        Screen.Results(countryCode).route
                                    )
                                }
                            )
                        }
                        composable(
                            route = Screen.Results.routePattern,
                            arguments = listOf(
                                navArgument(Screen.Results.countryCodeArg) { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val viewModel: ResultsViewModel = viewModel()
                            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
                            ResultsScreen(
                                backStackEntry = backStackEntry,
                                uiState = uiState,
                                onArticleTap = { article ->
                                    navController.navigate(
                                        Screen.ArticleDetailNav.createRoute(article)
                                    )
                                },
                                onLoadMore = { viewModel.loadMore() },
                                onRefresh = { viewModel.loadNews(
                                    backStackEntry.arguments?.getString("countryCode") ?: "us"
                                ) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = Screen.ArticleDetailNav.routePattern,
                            arguments = listOf(
                                navArgument(Screen.ArticleDetailNav.urlArg) {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument(Screen.ArticleDetailNav.articleArg) {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val article = Screen.ArticleDetailNav.parseArticle(backStackEntry)
                            ArticleDetailScreen(
                                article = article ?: Article(
                                    id = null,
                                    title = null,
                                    url = backStackEntry.arguments?.getString(Screen.ArticleDetailNav.urlArg),
                                    imageUrl = null,
                                    summary = null,
                                    authors = null,
                                    category = null,
                                    publicationDate = null,
                                    sourceCountry = null
                                ),
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}