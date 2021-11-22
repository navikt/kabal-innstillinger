package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class AxsysClientConfiguration(
    private val webClientBuilder: WebClient.Builder
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${AXSYS_APIKEY}")
    private lateinit var apiKey: String

    @Value("\${AXSYS_URL}")
    private lateinit var axsysServiceURL: String

    @Bean
    fun axsysWebClient(): WebClient {
        return webClientBuilder
            .defaultHeader("x-nav-apiKey", apiKey)
            .baseUrl(axsysServiceURL)
            .clientConnector(ReactorClientHttpConnector(HttpClient.newConnection()))
            .build()
    }
}