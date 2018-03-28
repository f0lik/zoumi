package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.model.Comment
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
        val SIMILARITY_LIMIT = 0.6
    }

    private val logger = LogManager.getLogger(TextAnalysisService::class.java)

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var similarCommentRepository: SimilarCommentRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    var similarComments: List<SimilarComment>? = null
    val jaccardSimilarityAlg = Jaccard()

    fun compareArticles(articleId: Long) {
        val article = articleRepository.findOne(articleId)
        compareArticles(article, article)
    }

    fun compareArticles(firstArticle: Article, secondArticle: Article) {
        val newerComments = commentRepository.getNewComments(firstArticle.id!!)

        if (newerComments.isPresent.not()) return

        newerComments.get().forEach { comment -> comment.isNew = false }
        commentRepository.save(newerComments.get())

        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)

        similarComments = similarCommentRepository.findAll()
        val processedPairIds = HashSet<String>()

        newerComments.get().forEach { firstComment ->
            run {
                val callableSimilarityTasks = ArrayList<Callable<Double>>()
                secondArticle.comments!!.forEach inner@{ secondComment ->
                    run {
                        val pairKey = Math.min(firstComment.id!!, secondComment.id!!).toString() + "_" + Math.max(firstComment.id!!, secondComment.id!!)
                        if (processedPairIds.contains(pairKey)) {
                            return@inner
                        }
                        if (firstComment.id == secondComment.id) {
                            return@inner
                        }
                        if (firstComment.commentText == secondComment.commentText) {
                            return@inner
                        }
                        if (doSimilarCommentAlreadyExist(firstComment.id!!, secondComment.id!!)) {
                            return@inner
                        }
                        processedPairIds.add(pairKey)
                        val callableSimilarityTask = Callable<Double> {
                            checkCommentSimilarity(firstComment, secondComment)
                        }
                        callableSimilarityTasks.add(callableSimilarityTask)
                    }
                }
                newFixedThreadPool.invokeAll(callableSimilarityTasks)
            }
        }
        processedPairIds.clear()
        newFixedThreadPool.shutdown()
    }

    private fun checkCommentSimilarity(firstComment: Comment, secondComment: Comment): Double {
        val similarity = jaccardSimilarityAlg.similarity(firstComment.commentText, secondComment.commentText)
        if (similarity > SIMILARITY_LIMIT) {
            createSimilarComment(firstComment, secondComment, similarity)
            return similarity
        }
        return 0.0
    }

    private fun createSimilarComment(firstComment: Comment, secondComment: Comment, similarity: Double) {
        val similarComment = SimilarComment()
        similarComment.firstCommentId = firstComment.id
        similarComment.firstCommentArticleId = firstComment.article!!.id
        similarComment.secondCommentId = secondComment.id
        similarComment.secondCommentArticleId = secondComment.article!!.id
        similarComment.similarity = (similarity * 100).toInt()
        similarCommentRepository.save(similarComment)
    }

    private fun doSimilarCommentAlreadyExist(id1: Long, id2: Long): Boolean {
        val similarComment = similarComments!!.find { similarComment ->
            (similarComment.firstCommentId == id1 && similarComment.secondCommentId == id2)
                    || (similarComment.firstCommentId == id2 || similarComment.secondCommentId == id1)
        }
        return similarComment != null
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
            similarityCommentMap.put(Pair(first, second), similarComment.similarity!!)
        }
        return similarityCommentMap
    }

    fun checkAllArticles() {
        val updatedArticleIds = commentRepository.getArticleIdsOfNewComments()
        if (updatedArticleIds.isPresent) {
            logger.info("${updatedArticleIds.get().size} articles will be checked")
            updatedArticleIds.get().forEach { articleId ->
                compareArticles(articleId)
            }
        }
    }

    fun updateCommentCount() {
        val notRecountedArticleIDs = commentRepository.getArticleIdsOfNotCountedComments()
        if (notRecountedArticleIDs.isPresent) {
            notRecountedArticleIDs.get().forEach { articleId ->
                val updatedArticle = articleRepository.findOne(articleId)
                updatedArticle.commentCount = commentRepository.getCommentCount(articleId)
                updatedArticle.similarCommentCount = similarCommentRepository.getSuspiciousCommentCount(articleId)
                val commentsToRecount = commentRepository.getCommentsToRecount(articleId)
                if (commentsToRecount.isPresent) {
                    commentsToRecount.get().forEach { comment -> comment.isCounted = true }
                    commentRepository.save(commentsToRecount.get())
                }
                articleRepository.save(updatedArticle)
            }
        }
    }
}