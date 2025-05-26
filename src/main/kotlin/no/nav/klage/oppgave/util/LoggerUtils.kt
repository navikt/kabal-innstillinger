package no.nav.klage.oppgave.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono

fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)

fun getTeamLogger(): Logger = LoggerFactory.getLogger("team-logs")

fun logErrorResponse(response: ClientResponse, functionName: String, classLogger: Logger): Mono<RuntimeException> {
    return response.bodyToMono(String::class.java).map {
        val errorString = "Got ${response.statusCode()} when requesting $functionName"
        classLogger.error("$errorString. See team-logs for more details.")
        getTeamLogger().error("$errorString - response body: '$it'")
        RuntimeException(errorString)
    }
}