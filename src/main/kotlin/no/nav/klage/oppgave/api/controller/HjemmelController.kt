package no.nav.klage.oppgave.api.controller;

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.InnstillingerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Hjemler")
class HjemmelController(private val innstillingerService: InnstillingerService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Operation(
        summary = "Hent alle registrerte hjemler for en gitt ytelse",
        description = "Hent alle registrerte hjemler for en gitt ytelse"
    )
    @GetMapping("/hjemler/{ytelseId}")
    fun getHjemlerForYtelse(
        @Parameter(name = "Id på etterspurt ytelse.")
        @PathVariable ytelseId: String,
    ): Set<Hjemmel> {
        return innstillingerService.getAllRegisteredHjemlerForYtelse(Ytelse.of(ytelseId))
    }
}
