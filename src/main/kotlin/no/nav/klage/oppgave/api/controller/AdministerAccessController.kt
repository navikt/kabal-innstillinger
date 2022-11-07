package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess
import no.nav.klage.oppgave.api.view.YtelseInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "kabal-innstillinger")
class AdministerAccessController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val innloggetAnsattRepository: InnloggetAnsattRepository,
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
        verifyIsLeder()

        return saksbehandlerAccessService.getSaksbehandlerAccess(saksbehandlerIdent = navIdent)
    }

    @Operation(
        summary = "Setter hvilke ytelser som den ansatte får lov til å jobbe med",
        description = "Setter hvilke ytelser som den ansatte får lov til å jobbe med"
    )
    @PutMapping("/ansatte/{navIdent}/ytelser", produces = ["application/json"])
    fun setYtelserForSaksbehandler(
        @PathVariable navIdent: String,
        @RequestBody input: YtelseInput
    ): SaksbehandlerAccess {
        verifyIsLeder()

        return saksbehandlerAccessService.setYtelser(saksbehandlerIdent = navIdent, ytelseIdList = input.ytelseIdList)
    }

    private fun verifyIsLeder() {
        if (!innloggetAnsattRepository.isLeder()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke lederrolle")
        }
    }

}