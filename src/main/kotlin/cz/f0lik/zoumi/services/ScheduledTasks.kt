package cz.f0lik.zoumi.services

import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduledTasks {
    private val logger = LogManager.getLogger(ScheduledTasks::class.java)

    @Autowired
    lateinit var textAnalysisService: TextAnalysisService

    @Autowired
    lateinit var dataDownloaderService: DataDownloaderService

    @Scheduled(cron = "0 0 1 * * ?")
    fun fetchArticlesComments() {
        logger.info("Starting remote data fetch at " + LocalDateTime.now())
        dataDownloaderService.fetchData()
        logger.info("Ending remote data fetch at " + LocalDateTime.now())
    }

    @Scheduled(cron = "0 0 3 * * ?")
    fun checkArticleRoutine() {
        logger.info("Starting similarity check at " + LocalDateTime.now())
        textAnalysisService.checkAllArticles()
        logger.info("Ending similarity check at " + LocalDateTime.now())
    }

    @Scheduled(cron = "0 0 6 * * ?")
    fun checkCommentCountRoutine() {
        logger.info("Starting comment recount at " + LocalDateTime.now())
        textAnalysisService.updateCommentCount()
        logger.info("Ending comment recount at " + LocalDateTime.now())
    }
}