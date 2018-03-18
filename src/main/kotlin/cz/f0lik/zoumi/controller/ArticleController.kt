package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.services.DataDownloaderService
import cz.f0lik.zoumi.services.TextAnalysisService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api")
class ArticleController {
    @Autowired
    lateinit var textAnalysisService: TextAnalysisService

    @Autowired
    lateinit var dataDownloaderService: DataDownloaderService

    @GetMapping("/articles/recountArticles")
    fun recountArticles(){
        println("Starting check at " + LocalDateTime.now())
        textAnalysisService.checkAllArticles()
        println("Ending check at " + LocalDateTime.now())
    }

    @GetMapping("/articles/recountComments")
    fun recountComments(){
        println("Comment recount started at " + LocalDateTime.now())
        textAnalysisService.updateCommentCount()
        println("Comment recount ended at " + LocalDateTime.now())
    }

    @GetMapping("/articles/fetchAll")
    fun fetchAllNew(){
        println("Fetch initiated at " + LocalDateTime.now())
        dataDownloaderService.fetchData()
        println("Fetch completed at " + LocalDateTime.now())
    }

    @GetMapping("/articles/updateCurrent")
    fun updateCurrent() {
        dataDownloaderService.updateCurrentArticles()
    }
}