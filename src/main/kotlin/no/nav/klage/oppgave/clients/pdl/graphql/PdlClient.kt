package no.nav.klage.oppgave.clients.pdl.graphql

import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getTeamLogger
import no.nav.klage.oppgave.util.logErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.lang.System.currentTimeMillis

@Component
class PdlClient(
    private val pdlWebClient: WebClient,
    private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    fun <T> runWithTiming(block: () -> T): T {
        val start = currentTimeMillis()
        try {
            return block.invoke()
        } finally {
            val end = currentTimeMillis()
            logger.debug("Time it took to call pdl: ${end - start} millis")
        }
    }

    @Retryable
    fun getPersonInfo(fnr: String): HentPersonResponse {
        return runWithTiming {
            val token = tokenUtil.getSaksbehandlerTokenWithPdlScope()
            try {
                pdlWebClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .bodyValue(hentPersonQuery(fnr))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError) { response ->
                        logErrorResponse(response, ::getPersonInfo.name, teamLogger)
                    }
                    .bodyToMono<HentPersonResponse>()
                    .block() ?: throw RuntimeException("Person not found")
            } catch (e: Exception) {
                teamLogger.error("Could not get personinfo for fnr $fnr", e)
                throw RuntimeException("Could not get personinfo. See more in team-logs.")
            }
        }
    }
}
