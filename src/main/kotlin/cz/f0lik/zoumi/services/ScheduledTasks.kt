package cz.f0lik.zoumi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Component
class ScheduledTasks {
    @Autowired
    lateinit var textAnalysisService: TextAnalysisService

    private val dateFormat = SimpleDateFormat("HH:mm:ss")

    @Scheduled(cron = "*/30 * * * * *")
    fun checkArticleRoutine() {
        println("Starting check at " + dateFormat.format(Date()))
        textAnalysisService.checkAllArticles()
    }
}