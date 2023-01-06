package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess
import no.nav.klage.oppgave.api.view.SaksbehandlerAccessResponse
import no.nav.klage.oppgave.api.view.YtelseInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Administer access")
class AdministerAccessController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val saksbehandlerService: SaksbehandlerService,
    private val innloggetAnsattRepository: InnloggetAnsattRepository,
    private val roleUtils: RoleUtils,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Operation(
        summary = "Henter ytelser som den ansatte har blitt tildelt av leder",
        description = "Henter ytelser som den ansatte har blitt tildelt av leder"
    )
    @GetMapping("/ansatte/{navIdent}/tildelteytelser", produces = ["application/json"])
    fun getSaksbehandlerAccess(
        @PathVariable navIdent: String,
    ): SaksbehandlerAccess {
        return saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = navIdent)
    }

    @Operation(
        summary = "Hent saksbehandlere for en enhet",
        description = "Hent saksbehandlere for en enhet"
    )
    @GetMapping("/enhet/{enhet}/saksbehandlere", produces = ["application/json"])
    fun getSaksbehandlereForEnhet(@PathVariable enhet: String): SaksbehandlerAccessResponse {
        verifyIsTilgangsstyringEgenEnhet()

        val innloggetSaksbehandlerNavIdent = innloggetAnsattRepository.getInnloggetIdent()
        logger.debug("getSaksbehandlereForEnhet is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerAccessService.getSaksbehandlere(enhet = enhet)
    }

    @Operation(
        summary = "Setter hvilke ytelser som de ansatte får lov til å jobbe med",
        description = "Setter hvilke ytelser som de ansatte får lov til å jobbe med"
    )
    @PutMapping("/ansatte/setytelser", produces = ["application/json"])
    fun setYtelserForSaksbehandlere(
        @RequestBody input: YtelseInput
    ): SaksbehandlerAccessResponse {
        verifyIsTilgangsstyringEgenEnhet()

        return saksbehandlerAccessService.setYtelser(
            ytelseInput = input,
            innloggetAnsattIdent = innloggetAnsattRepository.getInnloggetIdent()
        )
    }

    @Operation(
        summary = "Admin-endepunkt. Fjerner ytelser i innstillinger som ikke er tillatt for saksbehandler.",
        description = "Admin-endepunkt. Fjerner ytelser i innstillinger som ikke er tillatt for saksbehandler."
    )
    @PostMapping("/admin/cleanupinnstillinger", produces = ["application/json"])
    fun cleanupInnstillinger() {
        verifyIsAdmin()
        saksbehandlerService.cleanupInnstillinger()
    }

    private fun verifyIsTilgangsstyringEgenEnhet() {
        if (!roleUtils.isKabalTilgangsstyringEgenEnhet()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke tilgangsstyringrolle")
        }
    }

    private fun verifyIsAdmin() {
        if (!roleUtils.isAdmin()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke admin")
        }
    }

}