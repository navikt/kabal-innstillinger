package no.nav.klage.oppgave.api.controller;

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.InnstillingerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Hjemler")
class HjemmelController(private val innstillingerService: InnstillingerService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent alle hjemler lagret i innstillinger for en gitt ytelse",
        description = "Hent alle hjemler lagret i innstillinger for en gitt ytelse"
    )
    @GetMapping("/hjemler")
    fun getHjemlerForYtelse(
        @RequestParam(required = true, name = "ytelseId") ytelseId: String,
    ): Set<String> {
        return innstillingerService.getAllHjemlerForYtelse(Ytelse.of(ytelseId))
    }
}
