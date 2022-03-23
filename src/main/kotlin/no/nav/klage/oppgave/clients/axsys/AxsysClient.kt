package no.nav.klage.oppgave.clients.axsys

import brave.Tracer
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.TILGANGER_CACHE
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class AxsysClient(
    private val axsysWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    private val tracer: Tracer
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val IT_ENHET = "2990"
        const val KLAGEENHET_PREFIX = "42"
    }

    @Value("\${spring.application.name}")
    lateinit var applicationName: String

    @Retryable
    @Cacheable(TILGANGER_CACHE)
    fun getTilgangerForSaksbehandler(navIdent: String): Tilganger {
        logger.debug("Fetching tilganger for saksbehandler with Nav-Ident {}", navIdent)

        return try {
            val tilganger = axsysWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/tilgang/{navIdent}")
                        .queryParam("inkluderAlleEnheter", "true")
                        .build(navIdent)
                }
                .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithAxsysScope()}")
                .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)

                .retrieve()
                .bodyToMono<Tilganger>()
                .block() ?: throw RuntimeException("Tilganger could not be fetched")

            Tilganger(
                enheter = tilganger.enheter.filter { enhet ->
                    enhet.enhetId.startsWith(KLAGEENHET_PREFIX) ||
                            enhet.enhetId == IT_ENHET
                }
            )
            tilganger
        } catch (notFound: WebClientResponseException.NotFound) {
            logger.warn("Got a 404 fetching tilganger for saksbehandler {}, throwing exception", navIdent, notFound)
            throw RuntimeException("Tilganger could not be fetched")
        }
    }
}



