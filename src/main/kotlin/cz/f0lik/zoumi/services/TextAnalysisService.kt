package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Comment
import java.util.*

interface TextAnalysisService {
    fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean
    fun getSuspiciousCommentsCount(articleId: Long): Int
    fun getSuspiciousComments(articleId: Long): HashMap<Comment, Int>
    fun checkAllArticles()
}