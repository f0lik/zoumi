package cz.f0lik.zoumi.services.impl

import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.services.TextAnalysisService
import org.springframework.beans.factory.annotation.Autowired
import info.debatty.java.stringsimilarity.NGram
import org.springframework.stereotype.Service
import info.debatty.java.stringsimilarity.JaroWinkler

@Service("textAnalysisService")
class TextAnalysisServiceImpl : TextAnalysisService {
    val SIMILARITY_LIMIT = 0.15

    @Autowired
    var articleRepository: ArticleRepository? = null

    override fun compareArticles(firstArticleId: Long, secondArticleId: Long): Boolean {
        var firstArticle = articleRepository!!.findOne(firstArticleId)
        var secondArticle = articleRepository!!.findOne(secondArticleId)

        var somethingSimilar = false

        val twogram = NGram(4)
        val jw = JaroWinkler()
        firstArticle.comments!!.forEach { first ->
            run {

                secondArticle.comments!!.forEach { second ->
                    run {
                        println("Comparing \n->" + first.commentText + "\n with -> " + second.commentText)
                        var sim = twogram.distance(first.commentText, second.commentText)
                        var sim2 = jw.distance(first.commentText, second.commentText)
                        if (sim < SIMILARITY_LIMIT || sim2 < SIMILARITY_LIMIT) {
                            somethingSimilar = true
                        }
                    }
                }
            }
        }
        return somethingSimilar
    }
}