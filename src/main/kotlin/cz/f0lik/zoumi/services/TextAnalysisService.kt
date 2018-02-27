package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.model.SimilarComment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import info.debatty.java.stringsimilarity.Jaccard
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.HashMap

@Service("textAnalysisService")
class TextAnalysisService {
    val SIMILARITY_LIMIT = 0.6

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    var similarCommentRepository: SimilarCommentRepository? = null

    var similarComments: List<SimilarComment>? = null
    val jaccard = Jaccard()

    fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean {
        val firstArticle = articleRepository.findOne(firstArticleId)
        val secondArticle = articleRepository.findOne(secondArticleId)
        var somethingSimilar = false

        val newerComments = firstArticle.comments!!.filter {
            comment -> comment.created!! > firstArticle.lastFetchedDate
        }

        if (newerComments.isEmpty()) return somethingSimilar

        val newFixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)

        similarComments = similarCommentRepository!!.findAll()

        newerComments.forEach { firstComment ->
            run {
                val callableSimilarityTasks = ArrayList<Callable<Boolean>>()
                secondArticle.comments!!.forEach inner@ { secondComment ->
                    run {
                        if (firstComment.id == secondComment.id) {
                            return@inner
                        }
                        if (firstComment.commentText.equals(secondComment.commentText)) {
                            return@inner
                        }
                        if (doSimilarCommentAlreadyExist(firstComment.id!!, secondComment.id!!)) {
                            return@inner
                        }
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
        val similarity = jaccard.similarity(firstComment.commentText, secondComment.commentText)
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
        similarCommentRepository!!.save(similarComment)
    }

    private fun doSimilarCommentAlreadyExist(id1: Long, id2: Long): Boolean {
        val similarComment = similarComments!!.find { similarComment ->
            (similarComment.firstCommentId == id1 && similarComment.secondCommentId == id2)
                    || (similarComment.firstCommentId == id2 || similarComment.secondCommentId == id1)
        }

        return similarComment != null
    }

    fun getSuspiciousCommentsCount(articleId: Long): Int {
        return similarCommentRepository!!.getSuspiciousCommentCount(articleId)
    }

    fun getSuspiciousComments(articleId: Long): HashMap<Comment, Int> {
        val similarComments = similarCommentRepository!!.findAll()
        var similarityCommentMap = HashMap<Comment, Int>()
        similarComments.forEach { comment ->
            getSimilarComment(comment, articleId, similarityCommentMap)
        }
        return similarityCommentMap
    }

    private fun getSimilarComment(comment: SimilarComment, articleId: Long, commentSimMap: HashMap<Comment, Int>) {
        if (comment.firstCommentArticleId == articleId) {
            findComment(articleId, comment.firstCommentId!!, commentSimMap, comment.similarity!!)
        }
        if (comment.secondCommentArticleId == articleId) {
            findComment(articleId, comment.secondCommentId!!, commentSimMap, comment.similarity!!)
        }
    }

    private fun findComment(articleId: Long, commentId: Long, commentSimMap: HashMap<Comment, Int>, similarity: Int) {
        val comments = articleRepository.findOne(articleId).comments
        val foundComment = comments!!.find { comment -> comment.id == commentId }
        commentSimMap[foundComment!!] = similarity
    }

    fun checkAllArticles() {
        articleRepository.findAll().forEach { article ->
            compareArticles(article.id!!, article.id!!)
        }
    }
}