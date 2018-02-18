package cz.f0lik.zoumi.controller

import cz.f0lik.zoumi.repository.ArticleRepository
import cz.f0lik.zoumi.utils.ShutdownManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping

@Controller
class AppController {
    @Autowired
    var appContext: ApplicationContext? = null

    @Autowired
    var articleRepository: ArticleRepository? = null

    @GetMapping(value = ["/"])
    fun index(model: Model): String {
        val size = articleRepository!!.findAll().size
        model.addAttribute("artCount", size)
        return "index"
    }

    @PostMapping(value = ["/shtdw"])
    fun shutdown() {
        ShutdownManager.performShutdown(appContext)
    }
}