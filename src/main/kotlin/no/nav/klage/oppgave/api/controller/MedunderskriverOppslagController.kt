package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.MedunderskrivereForYtelse
import no.nav.klage.oppgave.api.view.MedunderskrivereInput
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
@Tag(name = "Medunderskriver")
class MedunderskriverOppslagController(
    private val saksbehandlerService: SaksbehandlerService,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent medunderskriver for en ansatt",
        description = "Henter alle medunderskrivere som saksbehandler er knyttet til for en gitt ytelse og fnr."
    )
    @PostMapping(
        "/search/medunderskrivere",
        produces = ["application/json"]
    )
    fun getMedunderskrivereForYtelseOgFnr(
        @RequestBody input: MedunderskrivereInput
    ): MedunderskrivereForYtelse {
        val innloggetSaksbehandlerNavIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::getMedunderskrivereForYtelseOgFnr.name} is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerService.getMedunderskrivere(
            ident = input.navIdent,
            ytelse = Ytelse.of(input.ytelseId),
            fnr = input.fnr
        )
    }
}