package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.api.controller.SaksbehandlerController
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun apiInternal(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .packagesToScan(SaksbehandlerController::class.java.packageName)
            .group("internal")
            .pathsToMatch("/**")
            .build()
    }

}