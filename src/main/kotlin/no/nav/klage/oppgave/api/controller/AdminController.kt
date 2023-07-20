package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Admin functionality")
@RequestMapping("/admin")
class AdminController(
    private val saksbehandlerService: SaksbehandlerService,
    private val roleUtils: RoleUtils,
) {
    @Operation(
        summary = "Legger til FTRL 22-12 og FTRL 22-13 i innstillinger for saksbehandlere som har valgt ytelse ENF, dersom de ikke har disse fra før.",
        description = "Legger til FTRL 22-12 og FTRL 22-13 i innstillinger for saksbehandlere som har valgt ytelse ENF, dersom de ikke har disse fra før."
    )
    @GetMapping("/completeenf", produces = ["application/json"])
    fun completeENFHjemler() {
        verifyIsAdmin()
        saksbehandlerService.addMissingENFHjemler()
    }

    @GetMapping("/logsaksbehandlerstatus", produces = ["application/json"])
    fun logSaksbehandlerStatus() {
        verifyIsAdmin()
        saksbehandlerService.logAnsattStatusInNom()
    }

    private fun verifyIsAdmin() {
        if (!roleUtils.isAdmin()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke admin")
        }
    }
}