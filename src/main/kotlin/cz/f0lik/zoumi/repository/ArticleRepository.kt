package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.Article
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.QueryByExampleExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ArticleRepository : JpaRepository<Article, Long>, QueryByExampleExecutor<Article> {
    @Query("SELECT u.createdDate from Article u")
    fun getArticlesCreatedTime(): Optional<List<LocalDateTime>>

    fun findByKeyWordsContaining(keyWord: String, pageable: Pageable): Page<Article>
}