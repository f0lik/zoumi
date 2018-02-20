package cz.f0lik.zoumi.repository

import cz.f0lik.zoumi.model.SimilarComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SimilarCommentRepository : JpaRepository<SimilarComment, Long>