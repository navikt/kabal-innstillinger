package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess
import no.nav.klage.oppgave.api.view.SaksbehandlerAccessResponse
import no.nav.klage.oppgave.api.view.TildelteYtelserResponse
import no.nav.klage.oppgave.api.view.YtelseInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Administer access")
class SaksbehandlerAccessController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val tokenUtil: TokenUtil,
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
        logger.debug("${::getSaksbehandlerAccess.name} is requested for $navIdent")
        return saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = navIdent)
    }

    @Operation(
        summary = "Hent saksbehandlere for en enhet, inkludert tildelte ytelser",
        description = "Hent saksbehandlere for en enhet, inkludert tildelte ytelser"
    )
    @GetMapping("/enhet/{enhet}/saksbehandlere", produces = ["application/json"])
    fun getSaksbehandlerAccessesForEnhet(@PathVariable enhet: String): SaksbehandlerAccessResponse {
        verifyIsTilgangsstyringEgenEnhet()
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::getSaksbehandlerAccessesForEnhet.name} is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerAccessService.getSaksbehandlerAccessesInEnhet(enhet = enhet)
    }

    @Operation(
        summary = "Hent alle tildelte ytelser i en enhet",
        description = "Hent alle tildelte ytelser i en enhet"
    )
    @GetMapping("/enhet/{enhet}/tildelteytelser", produces = ["application/json"])
    fun getTildelteYtelserForEnhet(@PathVariable enhet: String): TildelteYtelserResponse {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::getTildelteYtelserForEnhet.name} is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerAccessService.getTildelteYtelserForEnhet(enhet = enhet)
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
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::setYtelserForSaksbehandlere.name} is requested by $innloggetSaksbehandlerNavIdent")

        return saksbehandlerAccessService.setYtelserForAnsatt(
            ytelseInput = input,
            innloggetAnsattIdent = innloggetSaksbehandlerNavIdent
        )
    }

    private fun verifyIsTilgangsstyringEgenEnhet() {
        if (!roleUtils.isKabalTilgangsstyringEgenEnhet()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke tilgangsstyringrolle")
        }
    }
}