package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.service.InnstillingerService
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Admin functionality")
@RequestMapping("/admin")
class AdminController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val roleUtils: RoleUtils,
    private val innstillingerService: InnstillingerService,
    private val adminService: AdminService,
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

    @GetMapping("/admin/evictcaches")
    @ResponseStatus(HttpStatus.OK)
    fun evictCaches() {
        verifyIsAdmin()
        logger.debug("Evicting all caches")
        adminService.evictAllCaches()
        logger.debug("Evicted all caches")
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
        if (!roleUtils.isAdmin()) {
            throw MissingTilgangException(msg = "Innlogget ansatt har ikke admin")
        }
    }

    data class YtelseAndHjemler(
        val ytelseId: String,
        val hjemmelIdList: List<String>,
    )
}