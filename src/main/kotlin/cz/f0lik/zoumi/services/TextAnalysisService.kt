package cz.f0lik.zoumi.services

interface TextAnalysisService {
    fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean
}