package cz.f0lik.zoumi

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class ZoumiApplication

fun main(args: Array<String>) {
    SpringApplication.run(ZoumiApplication::class.java, *args)
}
