package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.SimilarComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SimilarCommentRepository : JpaRepository<SimilarComment, Long> {
    @Query("SELECT t FROM SimilarComment t where t.firstCommentId = ?1 AND t.secondCommentId = ?2 OR t.firstCommentId = ?2 AND t.secondCommentId = ?1")
    fun findByFirstSecondCommentId(firstCommentId: Long, secondCommentId: Long): Optional<SimilarComment>

    @Query("SELECT COUNT(*) FROM SimilarComment t where t.firstCommentArticleId = ?1 OR t.secondCommentArticleId = ?1")
    fun getSuspiciousCommentCount(commentId: Long): Int

    @Query("SELECT COUNT(*) FROM SimilarComment")
    fun getSuspiciousCommentCount(): Int

    @Query("SELECT COUNT(*) FROM SimilarComment t where t.similarity BETWEEN ?1 AND ?2 ")
    fun getSuspiciousCommentCountBetween(lowerPercentage: Int, highestPercentage: Int): Int

    @Query("SELECT t FROM SimilarComment t where t.firstCommentArticleId = ?1 OR t.secondCommentArticleId = ?1")
    fun getSuspiciousComments(articleId: Long): Optional<List<SimilarComment>>
}