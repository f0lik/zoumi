package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("statsService")
class StatsService {
    @Autowired
    lateinit var similarCommentRepository: SimilarCommentRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    fun getAllCommentsCount(): Long {
        return commentRepository.getCommentCount()
    }

    fun getSimilarCommentCount(): Int {
        return similarCommentRepository.getSuspiciousCommentCount()
    }

    fun getSimilarCommentCountInBetween(lowerPercentage: Int, highestPercentage: Int): Int {
        return similarCommentRepository.getSuspiciousCommentCountBetween(lowerPercentage, highestPercentage)
    }
}