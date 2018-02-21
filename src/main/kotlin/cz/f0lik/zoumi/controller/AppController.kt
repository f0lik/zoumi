package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.services.TextAnalysisService
import org.springframework.beans.factory.annotation.Autowired
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
    var textService: TextAnalysisService? = null

    @GetMapping(value = ["/"])
    fun index(model: Model): String {
        val size = articleRepository.findAll().size
        model.addAttribute("artCount", size)
        return "index"
    }

    @GetMapping("/articles")
    fun getArticles(model: Model): ModelAndView {
        val modelAndView = ModelAndView()
        modelAndView.viewName = "article_list"

        var articles = articleRepository.findAll()
        modelAndView.addObject("articles", articles)
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
        modelAndView.addObject("suspiciousCount", textService!!.getSuspiciousCommentsCount(articleId))
        modelAndView.addObject("suspComments", textService!!.getSuspiciousComments(articleId))

        return modelAndView
    }
}