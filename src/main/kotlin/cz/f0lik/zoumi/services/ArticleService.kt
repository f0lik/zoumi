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

    fun listAllByPage(searchWord: String, pageable: Pageable) : Page<Article> {
        return articleRepository.findByKeyWordsContaining(searchWord, pageable)
    }
}