package com.locallens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.locallens.data.model.Article
import com.locallens.ui.components.ArticleCard
import com.locallens.ui.state.ArticleUiState
import com.locallens.ui.screens.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    backStackEntry: NavBackStackEntry,
    uiState: ArticleUiState,
    onArticleTap: (Article) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "us"
    val category = backStackEntry.arguments?.getString("category") ?: "general"

    LaunchedEffect(backStackEntry) {
        onRefresh()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top app bar
        TopAppBar(
            title = { Text("$countryCode / $category") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        // Status messages
        if (uiState.isLoading && uiState.articles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (uiState.error != null && uiState.articles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = onRefresh) {
                        Text("Retry")
                    }
                }
            }
            return@Column
        }

        // Article list
        if (uiState.articles.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (category == "general") "No articles found."
                    else "No articles found for \"$category\" category."
                )
            }
            return@Column
        }

        // Total count
        if (uiState.totalResults > 0) {
            Text(
                text = "${uiState.totalResults} results found",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.articles) { article ->
                ArticleCard(article = article, onClick = { onArticleTap(article) })
            }

            // Load more item
            if (uiState.hasMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoadingMore) {
                            CircularProgressIndicator()
                        } else {
                            OutlinedButton(onClick = onLoadMore) {
                                Text("Load more")
                            }
                        }
                    }
                }
            }
        }
    }
}