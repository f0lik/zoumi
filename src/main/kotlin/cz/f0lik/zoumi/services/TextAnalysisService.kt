package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.model.SimilarComment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import info.debatty.java.stringsimilarity.Jaccard
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.collections.HashMap

@Service("textAnalysisService")
class TextAnalysisService {
    val SIMILARITY_LIMIT = 0.6

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    var similarCommentRepository: SimilarCommentRepository? = null

    fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean {
        val firstArticle = articleRepository.findOne(firstArticleId)
        val secondArticle = articleRepository.findOne(secondArticleId)

        var somethingSimilar = false

        val jaccard = Jaccard()
        firstArticle.comments!!.forEach { firstComment ->
            run {
                secondArticle.comments!!.forEach { secondComment ->
                    run {
                        if (doSimilarCommentAlreadyExist(firstComment.id!!, secondComment.id!!)) {
                            return@forEach
                        }
                        if (firstComment.commentText.equals(secondComment.commentText)) {
                            somethingSimilar = true
                            createSimilarComment(firstComment, secondComment, 100.0)
                            return@forEach
                        }
                        val similarity = jaccard.similarity(firstComment.commentText, secondComment.commentText)
                        if (similarity > SIMILARITY_LIMIT) {
                            somethingSimilar = true
                            createSimilarComment(firstComment, secondComment, similarity)
                        }
                    }
                }
            }
        }
        return somethingSimilar
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
        val similarComments = similarCommentRepository!!.findAll()

        similarComments.forEach { similarComment ->
            run {
                if (similarComment.firstCommentId == id1 && similarComment.secondCommentId == id2) {
                    return true
                } else if (similarComment.firstCommentId == id2 && similarComment.secondCommentId == id1) {
                    return true
                }
            }
        }
        return false
    }

    fun getSuspiciousCommentsCount(articleId: Long): Int {
        val articleComments = articleRepository.findOne(articleId).comments
        val similarComments = similarCommentRepository!!.findAll()
        var count = 0
        articleComments!!.forEach { comment ->
            run {
                similarComments.forEach { similarComment ->
                    run {
                        if (comment.id == similarComment.firstCommentId || comment.id == similarComment.secondCommentId) {
                            count++
                        }
                    }
                }
            }
        }
        return count
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