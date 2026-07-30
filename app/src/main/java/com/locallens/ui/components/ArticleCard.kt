package com.locallens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.locallens.data.model.Article
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit = {}
) {
    val hasUrl = !article.url.isNullOrBlank()
    val validImageUrl = article.imageUrl?.takeIf { it.startsWith("http") }
    androidx.compose.runtime.SideEffect {
        android.util.Log.d("ArticleCard", "Article: title=${article.title}, imageUrl=${article.imageUrl}, validImageUrl=$validImageUrl, summary=${article.summary?.take(50)}")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasUrl, onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            validImageUrl?.let { imageUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = article.title ?: "Article image",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = article.title ?: "No headline",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (!article.summary.isNullOrBlank()) {
                    Text(
                        text = article.summary!!,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ArticleMeta(article = article)
            }
        }
    }
}

@Composable
private fun ArticleMeta(article: Article) {
    val apiDateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val dateText = article.publicationDate?.let {
        try {
            displayDateFormat.format(apiDateFormat.parse(it))
        } catch (e: Exception) {
            "Unknown date"
        }
    } ?: "Unknown date"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(dateText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(
            text = article.sourceCountry ?: "",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }

    if (!article.authors.isNullOrBlank()) {
        Text(
            text = article.authors,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    if (!article.category.isNullOrBlank()) {
        Text(
            text = article.category,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
