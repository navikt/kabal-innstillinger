package no.nav.klage.oppgave.api.controller


import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.api.view.MedunderskrivereInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Api(tags = ["kabal-api"])
class MedunderskriverOppslagController(
    private val saksbehandlerService: SaksbehandlerService,
    private val environment: Environment,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }


    @ApiOperation(
        value = "Hent medunderskriver for en ansatt",
        notes = "Henter alle medunderskrivere som saksbehandler er knyttet til for en gitt ytelse og fnr."
    )
    @PostMapping(
        "/search/medunderskrivere",
        produces = ["application/json"]
    )
    fun getMedunderskrivereForYtelseOgFnr(
        @RequestBody input: MedunderskrivereInput
    ): Medunderskrivere {
        val innloggetSaksbehandlerNavIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug("getMedunderskrivereForYtelseOgFnr is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerService.getMedunderskrivere(input.navIdent, input.enhet, Ytelse.of(input.ytelseId), input.fnr)
    }

}

