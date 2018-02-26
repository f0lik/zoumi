package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.Article
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ArticleRepository : JpaRepository<Article, Long> {
    @Query("SELECT u.createdDate from Article u")
    fun getArticlesCreatedTime(): Optional<List<LocalDateTime>>

    @Query("SELECT COUNT(*) FROM Article")
    fun getArticleCount(): Int
}