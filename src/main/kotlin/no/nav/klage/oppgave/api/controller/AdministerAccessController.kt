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
    private val innloggetAnsattRepository: InnloggetAnsattRepository,
    private val roleUtils: RoleUtils,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Operation(
        summary = "Henter hvilke ytelser som den ansatte fått spesiell tilgang til å jobbe med",
        description = "Henter hvilke ytelser som den ansatte fått spesiell tilgang til å jobbe med"
    )
    @GetMapping("/ansatte/{navIdent}", produces = ["application/json"])
    fun getSaksbehandlerAccess(
        @PathVariable navIdent: String,
    ): SaksbehandlerAccess {
        verifyIsTilgangsstyringEgenEnhet()

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

    private fun verifyIsTilgangsstyringEgenEnhet() {
        if (!roleUtils.isKabalTilgangsstyringEgenEnhet()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke tilgangsstyringrolle")
        }
    }

}