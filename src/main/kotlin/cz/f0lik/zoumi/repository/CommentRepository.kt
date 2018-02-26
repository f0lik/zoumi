package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.*

interface CommentRepository : JpaRepository<Comment, Long> {
    @Query("SELECT u.created from Comment u where u.article.id = ?1")
    fun getCommentsCreatedTime(articleId: Long): Optional<List<LocalDateTime>>
}