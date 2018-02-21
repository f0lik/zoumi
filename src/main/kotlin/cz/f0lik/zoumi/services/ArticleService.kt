package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.repository.ArticleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service("articleService")
class ArticleService {
    @Autowired
    lateinit var articleRepository: ArticleRepository

    fun listAllByPage(pageable: Pageable) : Page<Article> {
        return articleRepository.findAll(pageable)
    }

    fun recountCommentSize() {
        val articles = articleRepository.findAll()
        articles.forEach { article ->
            run {
                article.commentCount = article.comments!!.size
                articleRepository.save(article)
            }
        }
    }
}