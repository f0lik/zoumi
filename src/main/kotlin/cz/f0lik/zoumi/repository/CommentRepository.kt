package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.*

interface CommentRepository : JpaRepository<Comment, Long> {
    @Query("SELECT u.created from Comment u where u.article.id = ?1")
    fun getCommentsCreatedTime(articleId: Long): Optional<List<LocalDateTime>>

    @Query("SELECT COUNT(*) from Comment where article.id = ?1")
    fun getCommentCount(articleId: Long): Int

    @Query("SELECT c from Comment c where c.article.id = ?1 AND c.isNew=true")
    fun getNewComments(articleId: Long): Optional<List<Comment>>

    @Query("SELECT c from Comment c where c.article.id = ?1 AND c.isCounted=false")
    fun getCommentsToRecount(articleId: Long): Optional<List<Comment>>

    @Query("SELECT distinct c.article.id from Comment c where c.isNew=true")
    fun getArticleIdsOfNewComments(): Optional<List<Long>>

    @Query("SELECT distinct c.article.id from Comment c where c.isCounted=false")
    fun getArticleIdsOfNotCountedComments(): Optional<List<Long>>
}