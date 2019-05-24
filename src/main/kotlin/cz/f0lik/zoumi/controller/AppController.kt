package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.model.Article
import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.services.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.RequestParam
import java.time.Year
import java.util.*

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

    @Autowired
    lateinit var portalService: PortalService

    private val INITIAL_PAGE = 0
    private val INITIAL_PAGE_SIZE = 5
    private val PAGE_SIZES = intArrayOf(5, 10, 20)
    val sortAttributesMap = mapOf("Počet komentářů" to "commentCount", "Počet podobných komentářů" to "similarCommentCount")
    val sortDirectionMap = mapOf("Vzestupně" to "ASC", "Sestupně" to "DESC")

    @Value("\${cz.f0lik.zoumi.appVersion}")
    private val applicationVersion: String? = null

    @GetMapping(value = ["/"])
    fun index(model: Model): ModelAndView {
        val modelAndView = ModelAndView()
        modelAndView.viewName = "index"

        val allCommentsCount = statsService.getAllCommentsCount()
        modelAndView.addObject("commentCount", allCommentsCount)
        modelAndView.addObject("operationCount", allCommentsCount * allCommentsCount)
        modelAndView.addObject("similarCommentCount", statsService.getSimilarCommentCount())
        modelAndView.addObject("similarCommentBetweenCount",
                statsService.getSimilarCommentCountInBetween(60, 90))
        modelAndView.addObject("version", applicationVersion)
        modelAndView.addObject("currentYear", Year.now().value)
        return modelAndView
    }

    @GetMapping("/articles")
    fun getArticles(@RequestParam("pageSize") pageSize: Optional<Int>,
                    @RequestParam("page")page: Optional<Int>,
                    @RequestParam("sortAttribute") sortAttribute: Optional<String>,
                    @RequestParam("sortDirection") sortDirection: Optional<String>,
                    @RequestParam("search") search: Optional<String>,
                    @RequestParam("portal")  choosedPortal: Optional<Int>): ModelAndView {
        val modelAndView = ModelAndView("article_list")
        val evaluatedPageSize = when {
            pageSize.orElse(INITIAL_PAGE_SIZE) > 20 -> INITIAL_PAGE_SIZE
            else -> pageSize.orElse(INITIAL_PAGE_SIZE)
        }
        val evaluatedPage = if (page.orElse(0) < 1) INITIAL_PAGE else page.get() - 1
        val evaluatedSortAttribute = sortAttribute.orElse("similarCommentCount")
        val evaluatedSortDirection = sortDirection.orElse(Sort.Direction.DESC.toString())
        val evaluatedPortal = choosedPortal.orElse(-1)

        val articles = when {
            search.isPresent -> articleService.listAllByPage(search.get().toLowerCase(),
                    PageRequest(evaluatedPage, evaluatedPageSize,
                    Sort.Direction.fromString(evaluatedSortDirection), evaluatedSortAttribute))
            choosedPortal.isPresent -> articleService.listAllByPortal(choosedPortal.get().toLong(),
                    PageRequest(evaluatedPage, evaluatedPageSize,
                    Sort.Direction.fromString(evaluatedSortDirection), evaluatedSortAttribute))
            else -> articleService.listAllByPage(PageRequest(evaluatedPage, evaluatedPageSize,
                    Sort.Direction.fromString(evaluatedSortDirection), evaluatedSortAttribute))
        }
        val pager = Pager(articles.totalPages, articles.number, 5)

        val portalIdNameMap = portalService.getPortalIdNameMap()
        portalIdNameMap!![-1] = "--Všechny--"
        modelAndView.addObject("articles", articles)
        modelAndView.addObject("portalMap", portalIdNameMap)
        modelAndView.addObject("selectedPortal", evaluatedPortal)
        modelAndView.addObject("selectedPageSize", evaluatedPageSize)
        modelAndView.addObject("pageSizes", PAGE_SIZES)
        modelAndView.addObject("sortAttributes", sortAttributesMap)
        modelAndView.addObject("selectedSortAttribute", evaluatedSortAttribute)
        modelAndView.addObject("sortDirections", sortDirectionMap)
        modelAndView.addObject("selectedSortDirection", evaluatedSortDirection)
        modelAndView.addObject("pager", pager)
        modelAndView.addObject("version", applicationVersion)
        modelAndView.addObject("currentYear", Year.now().value)
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
        modelAndView.addObject("article", article)
        modelAndView.addObject("suspiciousCount", statsService.getSuspiciousCommentsCount(articleId))
        modelAndView.addObject("version", applicationVersion)
        modelAndView.addObject("currentYear", Year.now().value)
        if (showComments) {
            modelAndView.addObject("suspComments", textService.getSuspiciousComments(articleId))
        }
        return modelAndView
    }
}