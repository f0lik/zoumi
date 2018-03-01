package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.services.ArticleService
import cz.f0lik.zoumi.services.TextAnalysisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView

@Controller
class AppController {
    @Autowired
    lateinit var articleRepository: ArticleRepository

    @Autowired
    lateinit var textService: TextAnalysisService

    @Autowired
    lateinit var articleService: ArticleService

    @GetMapping(value = ["/"])
    fun index(model: Model): String {
        return "index"
    }

    @GetMapping("/articles")
    fun getArticles(pageable: Pageable): ModelAndView {
        val commentsPerPage: Page<Article> = articleService.listAllByPage(pageable)
        val modelAndView = ModelAndView()
        modelAndView.viewName = "article_list"

        modelAndView.addObject("articles", commentsPerPage.content)
        return modelAndView
    }

    @GetMapping("/article/{id}")
    fun getArticle(@PathVariable(value = "id") articleId: Long, model: Model): ModelAndView {
        val article: Article = articleRepository.findOne(articleId) ?: return ModelAndView()
        val modelAndView = ModelAndView()
        modelAndView.viewName = "article"
        modelAndView.addObject("title", article.title)
        modelAndView.addObject("portal", article.portal)
        modelAndView.addObject("anotation", article.anotation)
        modelAndView.addObject("url", article.url)
        modelAndView.addObject("commentsCount", article.comments?.size)
        modelAndView.addObject("suspiciousCount", textService.getSuspiciousCommentsCount(articleId))
        modelAndView.addObject("suspComments", textService.getSuspiciousComments(articleId))

        return modelAndView
    }
}