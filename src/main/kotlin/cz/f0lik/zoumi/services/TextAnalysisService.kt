package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.model.CommentDTO
import cz.f0lik.zoumi.model.SimilarComment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import info.debatty.java.stringsimilarity.Jaccard
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.HashMap

@Service("textAnalysisService")
class TextAnalysisService {
    companion object {
        const val SIMILARITY_LIMIT = 0.6
    }

    private val logger = LogManager.getLogger(TextAnalysisService::class.java)

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var similarCommentRepository: SimilarCommentRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    var similarComments: List<SimilarComment>? = null

    fun compareArticleComments(articleId: Long) {
        val newerComments = commentRepository.getNewCommentDTO(articleId)

        val allComments = commentRepository.getCommentDTOList(articleId)
        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        similarComments = similarCommentRepository.findAll()
        val processedPairIds = HashSet<String>()

        markCommentAsNotNew(articleId)

        newerComments.forEach { newComment ->
            val callableSimilarityTasks = ArrayList<Callable<Double>>()
            allComments.forEach inner@{ comment ->
                val pairKey = Math.min(newComment.getCommentId(), comment.getCommentId()).toString() +
                        "_" + Math.max(newComment.getCommentId(), comment.getCommentId())
                if (processedPairIds.contains(pairKey)) {
                    return@inner
                }
                if (newComment.getCommentId() == comment.getCommentId()) {
                    return@inner
                }
                if (newComment.getCommentText() == comment.getCommentText()) {
                    return@inner
                }
                val callableSimilarityTask = Callable<Double> {
                    checkCommentSimilarity(newComment, comment)
                }
                callableSimilarityTasks.add(callableSimilarityTask)
                processedPairIds.add(pairKey)
            }
            newFixedThreadPool.invokeAll(callableSimilarityTasks)
        }
        processedPairIds.clear()
        newFixedThreadPool.shutdown()
    }

    private fun markCommentAsNotNew(articleId: Long) {
        val newComments = commentRepository.getNewComments(articleId)
        newComments.forEach {
            it.isNew = false
        }
        commentRepository.save(newComments)
    }

    private fun checkCommentSimilarity(firstComment: CommentDTO, secondComment: CommentDTO): Double {
        val similarity = Jaccard().similarity(firstComment.getCommentText(), secondComment.getCommentText())
        if (similarity > SIMILARITY_LIMIT) {
            createSimilarComment(firstComment, secondComment, similarity)
            return similarity
        }
        return 0.0
    }

    private fun createSimilarComment(firstComment: CommentDTO, secondComment: CommentDTO, similarity: Double) {
        val similarComment = SimilarComment()
        similarComment.firstCommentId = firstComment.getCommentId()
        similarComment.firstCommentArticleId = firstComment.getCommentArticleId()
        similarComment.secondCommentId = secondComment.getCommentId()
        similarComment.secondCommentArticleId = secondComment.getCommentArticleId()
        similarComment.similarity = (similarity * 100).toInt()
        similarCommentRepository.save(similarComment)
    }

    fun getSuspiciousComments(articleId: Long): HashMap<Pair<Comment, Comment>, Int> {
        val suspiciousComments = similarCommentRepository.getSuspiciousComments(articleId)
        val similarityCommentMap = HashMap<Pair<Comment, Comment>, Int>()
        if (!suspiciousComments.isPresent) {
            return similarityCommentMap
        }

        suspiciousComments.get().forEach { similarComment ->
            val first = commentRepository.findOne(similarComment.firstCommentId)
            val second = commentRepository.findOne(similarComment.secondCommentId)
            similarityCommentMap[Pair(first, second)] = similarComment.similarity!!
        }
        return similarityCommentMap
    }

    fun checkAllArticles() {
        val updatedArticleIds = commentRepository.getArticleIdsOfNewComments()
        if (updatedArticleIds.isPresent) {
            logger.info("${updatedArticleIds.get().size} articles will be checked")
            updatedArticleIds.get().forEach { articleId ->
                val startTime = System.currentTimeMillis()
                compareArticleComments(articleId)
                val stopTime = System.currentTimeMillis()
                val elapsedTime = stopTime - startTime
                logger.info("Similarity check on article id $articleId took $elapsedTime miliseconds")
            }
        }
    }

    fun updateCommentCount() {
        val notRecountedArticleIDs = commentRepository.getArticleIdsOfNotCountedComments()
        if (notRecountedArticleIDs.isPresent) {
            logger.info("${notRecountedArticleIDs.get().size} articles will be recounted")
            notRecountedArticleIDs.get().forEach { articleId ->
                val startTime = System.currentTimeMillis()
                val updatedArticle = articleRepository.findOne(articleId)
                updatedArticle.commentCount = commentRepository.getCommentCount(articleId)
                updatedArticle.similarCommentCount = similarCommentRepository.getSuspiciousCommentCount(articleId)
                val commentsToRecount = commentRepository.getCommentsToRecount(articleId)
                if (commentsToRecount.isPresent) {
                    commentsToRecount.get().forEach { comment -> comment.isCounted = true }
                    commentRepository.save(commentsToRecount.get())
                }
                articleRepository.save(updatedArticle)
                val stopTime = System.currentTimeMillis()
                val elapsedTime = stopTime - startTime
                logger.info("Comment recount on article id $articleId took $elapsedTime miliseconds")
            }
        }
    }
}