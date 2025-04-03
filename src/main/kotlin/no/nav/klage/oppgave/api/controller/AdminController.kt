package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.InnstillingerService
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
    private val innstillingerService: InnstillingerService,
) {
    @GetMapping("/logsaksbehandlerstatus", produces = ["application/json"])
    fun logSaksbehandlerStatus() {
        verifyIsAdmin()
        saksbehandlerAccessService.logAnsattStatusInNom()
    }

    @GetMapping("/deleteexpiredsaksbehandlersettings", produces = ["application/json"])
    fun deleteExpiredSaksbehandlerSettings() {
        verifyIsAdmin()
        saksbehandlerAccessService.deleteInnstillingerAndAccessForExpiredSaksbehandlers()
    }

    @PostMapping("/inserthjemlerinsettings", produces = ["application/json"])
    fun insertHjemlerInSettings(
        @RequestBody ytelseAndHjemler: YtelseAndHjemler
    ) {
        verifyIsAdmin()
        innstillingerService.addHjemlerForYtelse(ytelse = ytelseAndHjemler.ytelse, hjemmelList = ytelseAndHjemler.hjemmelList)
    }

    private fun verifyIsAdmin() {
        if (!roleUtils.isAdmin()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke admin")
        }
    }

    data class YtelseAndHjemler(
        val ytelse: Ytelse,
        val hjemmelList: List<Hjemmel>,
    )
}