package no.nav.klage.oppgave.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class Norg2ClientConfiguration(private val webClientBuilder: WebClient.Builder) {

    @Value("\${NORG2_API_V1_URL}")
    private lateinit var url: String

    @Value("\${NORG2_APIKEY}")
    private lateinit var apiKey: String

    @Bean
    fun norg2WebClient(): WebClient =
        webClientBuilder
            .baseUrl(url)
            .defaultHeader("x-nav-apiKey", apiKey)
            .clientConnector(ReactorClientHttpConnector(HttpClient.newConnection()))
            .build()
}
