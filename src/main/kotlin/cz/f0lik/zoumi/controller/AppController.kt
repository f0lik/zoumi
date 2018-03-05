package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.services.ArticleService
import cz.f0lik.zoumi.services.StatsService
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
    lateinit var statsService: StatsService

    @Autowired
    lateinit var articleService: ArticleService

    @GetMapping(value = ["/"])
    fun index(model: Model): ModelAndView {
        val modelAndView = ModelAndView()
        modelAndView.viewName = "index"

        val allCommentsCount = statsService.getAllCommentsCount()
        modelAndView.addObject("commentCount", allCommentsCount)
        modelAndView.addObject("operationCount", allCommentsCount * allCommentsCount)
        modelAndView.addObject("similarCommentCount", statsService.getSimilarCommentCount())
        modelAndView.addObject("similarCommentBetweenCount", statsService.getSimilarCommentCountInBetween(65, 95))
        return modelAndView
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
        return getArticleView(article, articleId, false)
    }

    @GetMapping("/article/{id}/all")
    fun getArticleWithComments(@PathVariable(value = "id") articleId: Long, model: Model): ModelAndView {
        val article: Article = articleRepository.findOne(articleId) ?: return ModelAndView()
        return getArticleView(article, articleId, true)
    }

    private fun getArticleView(article: Article, articleId: Long, showComments: Boolean): ModelAndView {
        val modelAndView = ModelAndView()
        modelAndView.viewName = "article"
        modelAndView.addObject("title", article.title)
        modelAndView.addObject("portal", article.portal)
        modelAndView.addObject("anotation", article.anotation)
        modelAndView.addObject("url", article.url)
        modelAndView.addObject("commentsCount", article.comments?.size)
        modelAndView.addObject("suspiciousCount", textService.getSuspiciousCommentsCount(articleId))
        if (showComments) {
            modelAndView.addObject("suspComments", textService.getSuspiciousComments(articleId))
        }
        return modelAndView
    }
}