package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.InnstillingerService
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Admin functionality")
@RequestMapping("/admin")
class AdminController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val klageLookupGateway: KlageLookupGateway,
    private val innstillingerService: InnstillingerService,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

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
        innstillingerService.addHjemlerForYtelse(
            ytelse = Ytelse.of(ytelseAndHjemler.ytelseId),
            hjemmelList = ytelseAndHjemler.hjemmelIdList.map { Hjemmel.of(it) })
    }

    private fun verifyIsAdmin() {
        if (!klageLookupGateway.getGroupsForGivenNavIdent(tokenUtil.getCurrentIdent()).groups.any { it == AzureGroup.KABAL_ADMIN }) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke admin")
        }
    }

    data class YtelseAndHjemler(
        val ytelseId: String,
        val hjemmelIdList: List<String>,
    )
}