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
        summary = "Hent medunderskrivere for et gitt fnr og ytelse",
        description = "Henter alle medunderskrivere som saksbehandler er knyttet til for en gitt ytelse og fnr."
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
        return saksbehandlerService.getMedunderskrivere(
            ident = input.navIdent,
            ytelse = Ytelse.of(input.ytelseId),
            fnr = input.fnr
        )
    }

    @Operation(
        summary = "Hent ROLs for en ansatt",
        description = "Henter alle ROLs som kan brukes for en gitt person."
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
        return saksbehandlerService.getROLList(
            fnr = input.fnr
        )
    }

    @Operation(
        summary = "Hent potensielle saksbehandlere for en gitt ytelse og person",
        description = "Hent potensielle saksbehandlere for en gitt ytelse og person"
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
        return saksbehandlerService.getSaksbehandlere(
            ytelse = Ytelse.of(input.ytelseId),
            fnr = input.fnr
        )
    }

    private fun logMethodCall(navIdent: String, methodName: String) {
        logger.debug("$methodName is requested by $navIdent")
    }
}