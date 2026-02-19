package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Search")
class SearchController(
    private val saksbehandlerService: SaksbehandlerService,
    private val tokenUtil: TokenUtil,
)  {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent medunderskrivere for en gitt sak",
        description = "Henter alle medunderskrivere som saksbehandler er knyttet til for en gitt sak."
    )
    @PostMapping(
        "/search/medunderskrivere",
        produces = ["application/json"]
    )
    fun getMedunderskrivereForSak(
        @RequestBody input: SearchMedunderskrivereInput
    ): MedunderskrivereForYtelse {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getMedunderskrivereForSak.name)

        return saksbehandlerService.getMedunderskrivere(
            ident = input.navIdent,
            ytelse = Ytelse.of(input.sak.ytelseId),
            fnr = input.sak.fnr,
            sakId = input.sak.sakId,
            fagsystem = Fagsystem.of(input.sak.fagsystemId),
        )
    }

    @Operation(
        summary = "Hent mulige ROLs for en gitt sak",
        description = "Henter alle ROLs som kan brukes for en gitt sak."
    )
    @PostMapping(
        "/search/rol",
        produces = ["application/json"]
    )
    fun getROLsForSak(
        @RequestBody input: SakInput,
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getROLsForSak.name)

        return saksbehandlerService.getROLList(
            fnr = input.fnr,
            ytelse = Ytelse.of(input.ytelseId),
            sakId = input.sakId,
            fagsystem = Fagsystem.of(input.fagsystemId),
        )
    }

    @Operation(
        summary = "Hent potensielle saksbehandlere for en gitt sak",
        description = "Hent potensielle saksbehandlere for en gitt sak"
    )
    @PostMapping(
        "/search/saksbehandlere",
        produces = ["application/json"]
    )
    fun getSaksbehandlereForSak(
        @RequestBody input: SakInput
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getSaksbehandlereForSak.name)

        return saksbehandlerService.getSaksbehandlere(
            ytelse = Ytelse.of(input.ytelseId),
            fnr = input.fnr,
            sakId = input.sakId,
            fagsystem = Fagsystem.of(input.fagsystemId),
        )
    }

    @Operation(
        summary = "Hent potensielle saksbehandlere for en gitt bruker",
        description = "Hent potensielle saksbehandlere for en gitt bruker"
    )
    @PostMapping(
        "/search/saksbehandlere-for-bruker",
        produces = ["application/json"]
    )
    fun getSaksbehandlereForBruker(
        @RequestBody input: SearchSaksbehandlereForBrukerInput
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getSaksbehandlereForBruker.name)

        return saksbehandlerService.getSaksbehandlereForBruker(
            ytelse = Ytelse.of(input.ytelseId),
            fnr = input.fnr,
        )
    }

    private fun logMethodCall(navIdent: String, methodName: String) {
        logger.debug("$methodName is requested by $navIdent")
    }
}