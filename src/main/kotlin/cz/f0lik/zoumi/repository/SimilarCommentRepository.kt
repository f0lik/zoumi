package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.SimilarComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SimilarCommentRepository : JpaRepository<SimilarComment, Long> {
    @Query("SELECT t FROM SimilarComment t where t.firstCommentId = ?1 AND t.secondCommentId = ?2 OR t.firstCommentId = ?2 AND t.secondCommentId = ?1")
    fun findByFirstSecondCommentId(id1: Long, id2: Long): Optional<SimilarComment>
}