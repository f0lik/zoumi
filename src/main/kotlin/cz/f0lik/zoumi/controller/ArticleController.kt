package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.services.DataDownloaderService
import cz.f0lik.zoumi.services.TextAnalysisService
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
class ArticleController {
    private val logger = LogManager.getLogger(ArticleController::class.java)

    @Autowired
    lateinit var textAnalysisService: TextAnalysisService

    @Autowired
    lateinit var dataDownloaderService: DataDownloaderService

    @GetMapping("/articles/recountArticles")
    fun recountArticles(){
        logger.info("Starting similarity check at " + LocalDateTime.now())
        textAnalysisService.checkAllArticles()
        logger.info("Ending similarity check at " + LocalDateTime.now())
    }

    @GetMapping("/articles/recountComments")
    fun recountComments(){
        logger.info("Starting comment recount at " + LocalDateTime.now())
        textAnalysisService.updateCommentCount()
        logger.info("Ending comment recount at " + LocalDateTime.now())
    }

    @GetMapping("/articles/fetchAll")
    fun fetchAllNew(){
        logger.info("Starting remote data fetch at " + LocalDateTime.now())
        dataDownloaderService.fetchData()
        logger.info("Ending remote data fetch at " + LocalDateTime.now())
    }
}