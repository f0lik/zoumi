package cz.f0lik.zoumi.utils

import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext

class ShutdownManager {
    companion object {
        @JvmStatic
        fun performShutdown(appContext: ApplicationContext?) {
            SpringApplication.exit(appContext)
        }
    }
}
