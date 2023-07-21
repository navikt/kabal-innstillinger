package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Admin functionality")
@RequestMapping("/admin")
class AdminController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val roleUtils: RoleUtils,
) {
    @GetMapping("/logsaksbehandlerstatus", produces = ["application/json"])
    fun logSaksbehandlerStatus() {
        verifyIsAdmin()
        saksbehandlerAccessService.logAnsattStatusInNom()
    }

    private fun verifyIsAdmin() {
        if (!roleUtils.isAdmin()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke admin")
        }
    }
}