package no.nav.klage.oppgave

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@EnableScheduling
class Application

fun main() {
    runApplication<Application>()
}