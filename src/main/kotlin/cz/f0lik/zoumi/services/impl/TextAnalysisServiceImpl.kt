package cz.f0lik.zoumi.services.impl

import cz.f0lik.zoumi.model.Comment
import cz.f0lik.zoumi.model.SimilarComment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import cz.f0lik.zoumi.services.TextAnalysisService
import info.debatty.java.stringsimilarity.Jaccard
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.collections.HashMap

@Service("textAnalysisService")
class TextAnalysisServiceImpl : TextAnalysisService {
    val SIMILARITY_LIMIT = 0.7

    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    var similarCommentRepository: SimilarCommentRepository? = null

    override fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean {
        val firstArticle = articleRepository.findOne(firstArticleId)
        val secondArticle = articleRepository.findOne(secondArticleId)

        var somethingSimilar = false

        val jaccard = Jaccard()
        firstArticle.comments!!.forEach { first ->
            run {
                secondArticle.comments!!.forEach { second ->
                    run {
                        if (first.commentText.equals(second.commentText)) {
                            return@forEach
                        }
                        if (doSimilarCommentAlreadyExist(first.id!!, second.id!!)) {
                            return@forEach
                        }
                        val similarity = jaccard.similarity(first.commentText, second.commentText)
                        if (similarity > SIMILARITY_LIMIT) {
                            somethingSimilar = true
                            val similarComment = SimilarComment()
                            similarComment.firstCommentId = first.id
                            similarComment.firstCommentArticleId = first.article!!.id
                            similarComment.secondCommentId = second.id
                            similarComment.secondCommentArticleId = second.article!!.id
                            similarComment.similarity = (similarity * 100).toInt()
                            similarCommentRepository!!.save(similarComment)
                        }
                    }
                }
            }
        }
        return somethingSimilar
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

    override fun getSuspiciousCommentsCount(articleId: Long): Int {
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

    override fun getSuspiciousComments(articleId: Long): HashMap<Comment, Int> {
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

    override fun checkAllArticles() {
        articleRepository.findAll().forEach { article ->
            compareArticles(article.id!!, article.id!!)
        }
    }
}