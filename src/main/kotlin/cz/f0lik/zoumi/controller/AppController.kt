package cz.f0lik.zoumi.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AppController {

    @GetMapping(value = ["/"])
    fun index(): String {
        return "index"
    }
}