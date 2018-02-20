package cz.f0lik.zoumi.services.impl

import cz.f0lik.zoumi.model.SimilarComment
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.SimilarCommentRepository
import cz.f0lik.zoumi.services.TextAnalysisService
import info.debatty.java.stringsimilarity.Jaccard
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("textAnalysisService")
class TextAnalysisServiceImpl : TextAnalysisService {
    val SIMILARITY_LIMIT = 0.7

    @Autowired
    var articleRepository: ArticleRepository? = null

    @Autowired
    var similarCommentRepository: SimilarCommentRepository? = null

    override fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean {
        val firstArticle = articleRepository!!.findOne(firstArticleId)
        val secondArticle = articleRepository!!.findOne(secondArticleId)

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
                            similarComment.secondCommentId = second.id
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
}