package cz.f0lik.zoumi.services

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.repository.PortalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service("articleService")
class ArticleService {
    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var portalRepository: PortalRepository

    fun listAllByPage(pageable: Pageable) : Page<Article> {
        return articleRepository.findAll(pageable)
    }

    fun listAllByPage(searchWord: String, pageable: Pageable) : Page<Article> {
        return articleRepository.findByKeyWordsContaining(searchWord, pageable)
    }

    fun listAllByPortal(portalId: Long, pageable: Pageable) : Page<Article> {
        val portal = portalRepository.findOne(portalId)
        return articleRepository.findByPortal(portal, pageable)
    }
}