package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
    fun getMedunderskrivereForYtelseOgFnr(
        @RequestBody input: SearchMedunderskrivereInput
    ): MedunderskrivereForYtelse {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getMedunderskrivereForYtelseOgFnr.name)

        val fnr = input.sak?.fnr ?: input.fnr
        val ytelseId = input.sak?.ytelseId ?: input.ytelseId

        return saksbehandlerService.getMedunderskrivere(
            ident = input.navIdent,
            ytelse = Ytelse.of(ytelseId),
            fnr = fnr,
            sakId = input.sak?.sakId,
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
    fun getROLForFnr(
        @RequestBody input: SearchROLInput,
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getROLForFnr.name)

        val fnr = input.sak?.fnr ?: input.fnr

        return saksbehandlerService.getROLList(
            fnr = fnr,
            ytelse = input.sak?.let { Ytelse.of(it.ytelseId) },
            sakId = input.sak?.sakId,
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
    fun getSaksbehandlereForYtelseOgFnr(
        @RequestBody input: SearchSaksbehandlerInput
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = innloggetSaksbehandlerNavIdent, methodName = ::getSaksbehandlereForYtelseOgFnr.name)

        val fnr = input.sak?.fnr ?: input.fnr
        val ytelseId = input.sak?.ytelseId ?: input.ytelseId

        return saksbehandlerService.getSaksbehandlere(
            ytelse = Ytelse.of(ytelseId),
            fnr = fnr,
            sakId = input.sak?.sakId,
        )
    }

    private fun logMethodCall(navIdent: String, methodName: String) {
        logger.debug("$methodName is requested by $navIdent")
    }
}