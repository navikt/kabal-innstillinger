package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess
import no.nav.klage.oppgave.api.view.Saksbehandlere
import no.nav.klage.oppgave.api.view.YtelseInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "kabal-innstillinger")
class AdministerAccessController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val saksbehandlerService: SaksbehandlerService,
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
        summary = "Hent saksbehandlere for en enhet",
        description = "Hent saksbehandlere for en enhet"
    )
    @GetMapping("/enhet/{enhet}/saksbehandlere", produces = ["application/json"])
    fun getSaksbehandlereForEnhet(@PathVariable enhet: String): Saksbehandlere {
        verifyIsLeder()

        val innloggetSaksbehandlerNavIdent = innloggetAnsattRepository.getInnloggetIdent()
        logger.debug("getSaksbehandlereForEnhet is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerAccessService.getSaksbehandlere(enhet = enhet)
    }

    @Operation(
        summary = "Setter hvilke ytelser som de ansatte får lov til å jobbe med",
        description = "Setter hvilke ytelser som de ansatte får lov til å jobbe med"
    )
    @PutMapping("/ansatte/addytelser", produces = ["application/json"])
    fun setYtelserForSaksbehandlere(
        @RequestBody input: YtelseInput
    ): List<SaksbehandlerAccess> {
        verifyIsLeder()

        return saksbehandlerAccessService.addYtelser(
            saksbehandleridentList = input.saksbehandleridentList,
            ytelseIdList = input.ytelseIdList,
            innloggetAnsattIdent = innloggetAnsattRepository.getInnloggetIdent()
        )
    }

    @Operation(
        summary = "Fjerner ytelser som de ansatte får lov til å jobbe med",
        description = "Fjerner ytelser som de ansatte får lov til å jobbe med"
    )
    @PutMapping("/ansatte/removeytelser", produces = ["application/json"])
    fun removeYtelserForSaksbehandlere(
        @RequestBody input: YtelseInput
    ): List<SaksbehandlerAccess> {
        verifyIsLeder()

        return saksbehandlerAccessService.removeYtelser(
            saksbehandleridentList = input.saksbehandleridentList,
            ytelseIdList = input.ytelseIdList,
            innloggetAnsattIdent = innloggetAnsattRepository.getInnloggetIdent())

    }

    private fun verifyIsLeder() {
        if (!innloggetAnsattRepository.isLeder()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke lederrolle")
        }
    }

}