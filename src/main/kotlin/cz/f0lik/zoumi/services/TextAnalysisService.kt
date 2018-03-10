package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.model.SimilarComment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.CommentRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import info.debatty.java.stringsimilarity.Jaccard
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.HashMap

@Service("textAnalysisService")
class TextAnalysisService {
    val SIMILARITY_LIMIT = 0.6

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var similarCommentRepository: SimilarCommentRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    var similarComments: List<SimilarComment>? = null
    val jaccardSimilarityAlg = Jaccard()

    fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean {
        val firstArticle = articleRepository.findOne(firstArticleId)
        val secondArticle = articleRepository.findOne(secondArticleId)
        var somethingSimilar = false

        val newerComments = firstArticle.comments!!.filter { comment ->
            comment.isNew == true
        }

        if (newerComments.isEmpty()) return somethingSimilar

        newerComments.forEach { comment -> comment.isNew = false }
        commentRepository.save(newerComments)

        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        similarComments = similarCommentRepository.findAll()

        val processedPairIds = HashSet<String>()

        newerComments.forEach { firstComment ->
            run {
                val callableSimilarityTasks = ArrayList<Callable<Boolean>>()
                secondArticle.comments!!.forEach inner@{ secondComment ->
                    run {
                        val pairKey = Math.min(firstComment.id!!, secondComment.id!!).toString() + "_" + Math.max(firstComment.id!!, secondComment.id!!)
                        if (processedPairIds.contains(pairKey)) {
                            return@inner
                        }
                        if (firstComment.id == secondComment.id) {
                            return@inner
                        }
                        if (firstComment.commentText.equals(secondComment.commentText)) {
                            return@inner
                        }
                        if (doSimilarCommentAlreadyExist(firstComment.id!!, secondComment.id!!)) {
                            return@inner
                        }
                        processedPairIds.add(pairKey)
                        val callableSimilarityTask = Callable {
                            checkSimilarity(firstComment, secondComment)
                        }
                        callableSimilarityTasks.add(callableSimilarityTask)
                    }
                }
                val similarTasks = newFixedThreadPool.invokeAll(callableSimilarityTasks).filter { task -> task.get() == true }
                somethingSimilar = similarTasks.isNotEmpty()
            }
        }
        newFixedThreadPool.shutdown()
        return somethingSimilar
    }

    private fun checkSimilarity(firstComment: Comment, secondComment: Comment): Boolean {
        val similarity = jaccardSimilarityAlg.similarity(firstComment.commentText, secondComment.commentText)
        if (similarity > SIMILARITY_LIMIT) {
            createSimilarComment(firstComment, secondComment, similarity)
            return true
        }
        return false
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

    fun getSuspiciousCommentsCount(articleId: Long): Int {
        return similarCommentRepository.getSuspiciousCommentCount(articleId)
    }

    fun getSuspiciousComments(articleId: Long): HashMap<Pair<Comment, Comment>, Int> {
        val suspiciousComments = similarCommentRepository.getSuspiciousComments(articleId)
        val similarityCommentMap = HashMap<Pair<Comment, Comment>, Int>()
        if (!suspiciousComments.isPresent) {
            return similarityCommentMap
        }

       suspiciousComments.get().forEach { similarComment ->
            run {
                val first = commentRepository.findOne(similarComment.firstCommentId)
                val second = commentRepository.findOne(similarComment.secondCommentId)
                similarityCommentMap.put(Pair(first, second), similarComment.similarity!!)
            }
        }
        return similarityCommentMap
    }

    fun checkAllArticles() {
        articleRepository.findAll().forEach { article ->
            compareArticles(article.id!!, article.id!!)
        }
    }

    fun updateCommentCount() {
        articleRepository.findAll().forEach { article ->
            article.commentCount = commentRepository.getCommentCount(article.id!!)
            article.similarCommentCount = similarCommentRepository.getSuspiciousCommentCount(article.id!!)
            articleRepository.save(article)
        }
    }
}