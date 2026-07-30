package com.locallens.ui.screens

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import com.google.gson.Gson
import com.locallens.data.model.Article

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data class Results(
        val countryCode: String
    ) : Screen("results?countryCode=$countryCode") {
        companion object {
            const val countryCodeArg = "countryCode"
            const val routePattern = "results?countryCode={countryCode}"
        }
    }

    data class ArticleDetailNav(
        val articleUrl: String,
        val articleJson: String? = null
    ) : Screen("article?url=$articleUrl${if (articleJson != null) "&article=${Uri.encode(articleJson)}" else ""}") {
        companion object {
            const val urlArg = "url"
            const val articleArg = "article"
            const val routePattern = "article?url={url}&article={article}"

            private val gson = Gson()

            fun createRoute(article: Article): String {
                val json = gson.toJson(article)
                return "article?url=${Uri.encode(article.url ?: "")}&article=${Uri.encode(json)}"
            }

            fun parseArticle(json: String?): Article? {
                return json?.let { gson.fromJson(it, Article::class.java) }
            }

            fun parseArticle(backStackEntry: NavBackStackEntry): Article? {
                return parseArticle(backStackEntry.arguments?.getString(articleArg))
            }
        }
    }
}