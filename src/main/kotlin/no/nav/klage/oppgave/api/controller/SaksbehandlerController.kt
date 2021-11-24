package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.SaksbehandlerMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.domain.kodeverk.Ytelse
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Api(tags = ["kabal-innstillinger"])
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
    private val environment: Environment,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerMapper: SaksbehandlerMapper,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent brukerdata for innlogget ansaatt",
        notes = "Henter alle brukerdata om en saksbehandler, inklusive innstillingene hen har gjort."
    )
    @GetMapping("/me/brukerdata", produces = ["application/json"])
    fun getBrukerdata(): SaksbehandlerView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug("getBrukerdata is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent))
    }

    @ApiOperation(
        value = "Hent brukerdata for en ansatt",
        notes = "Henter alle brukerdata om en saksbehandler, inklusive innstillingene hen har gjort."
    )
    @GetMapping("/ansatte/{navIdent}/brukerdata", produces = ["application/json"])
    fun getBrukerdata(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): SaksbehandlerView {
        logger.debug("getBrukerdata is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent))
    }

    @ApiOperation(
        value = "Setter valgt klageenhet for en ansatt",
        notes = "Setter valgt klageenhet som den ansatte jobber med. Må være en i lista over mulige enheter"
    )
    @PutMapping("/ansatte/{navIdent}/brukerdata/valgtenhet", produces = ["application/json"])
    fun setBrukerdataValgtEnhet(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: ValgtEnhetInput
    ): EnhetView {
        validateNavIdent(navIdent)
        return saksbehandlerMapper.mapToView(saksbehandlerService.storeValgtEnhetId(navIdent, input.enhetId))
    }

    @ApiOperation(
        value = "Setter innstillinger for en ansatt",
        notes = "Setter valgt tema, hjemmel og type som den ansatte jobber med"
    )
    @PutMapping("/ansatte/{navIdent}/brukerdata/innstillinger", produces = ["application/json"])
    fun setInnstillinger(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: SaksbehandlerView.InnstillingerView
    ): SaksbehandlerView.InnstillingerView {
        validateNavIdent(navIdent)
        return saksbehandlerMapper.mapToView(
            saksbehandlerService.storeInnstillinger(
                navIdent,
                saksbehandlerMapper.mapToDomain(input)
            )
        )
    }

    @ApiOperation(
        value = "Hent klageenheter for en ansatt",
        notes = "Henter alle klageenheter som saksbehandler er knyttet til."
    )
    @GetMapping("/ansatte/{navIdent}/enheter", produces = ["application/json"])
    fun getEnheter(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): List<EnhetView> {
        logger.debug("getEnheter is requested by $navIdent")
        val enheter = saksbehandlerMapper.mapToView(saksbehandlerService.getEnheterMedYtelserForSaksbehandler())
        logEnheter(enheter, navIdent)
        return enheter
    }

    @ApiOperation(
        value = "Setter valgt klageenhet for en ansatt",
        notes = "Setter valgt klageenhet som den ansatte jobber med. Må være en i lista over mulige enheter"
    )
    @PutMapping("/ansatte/{navIdent}/valgtenhet", produces = ["application/json"])
    fun setValgtEnhet(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: ValgtEnhetInput
    ): EnhetView {
        validateNavIdent(navIdent)
        return saksbehandlerMapper.mapToView(saksbehandlerService.storeValgtEnhetId(navIdent, input.enhetId))
    }

    @ApiOperation(
        value = "Henter valgt klageenhet for en ansatt",
        notes = "Henter valgt klageenhet som den ansatte jobber med. Er fra lista over mulige enheter"
    )
    @GetMapping("/ansatte/{navIdent}/valgtenhet", produces = ["application/json"])
    fun getValgtEnhet(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): EnhetView {
        return saksbehandlerMapper.mapToView(saksbehandlerService.findValgtEnhet(navIdent))
    }

    @ApiOperation(
        value = "Hent medunderskriver for en ansatt",
        notes = "Henter alle medunderskrivere som saksbehandler er knyttet til for et gitt tema."
    )
    @GetMapping("/ansatte/{navIdent}/medunderskrivere/{ytelse}", produces = ["application/json"])
    fun getMedunderskrivere(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Tema man trenger medunderskrivere for")
        @PathVariable ytelse: String
    ): Medunderskrivere {
        logger.debug("getMedunderskrivere is requested by $navIdent")
        return if (environment.activeProfiles.contains("prod-gcp")) {
            saksbehandlerService.getMedunderskrivere(navIdent, Ytelse.of(ytelse))
        } else Medunderskrivere(
            ytelse,
            listOf(
                Medunderskriver("Z994488", "F_Z994488, E_Z994488"),
                Medunderskriver("Z994330", "F_Z994330 E_Z994330"),
                Medunderskriver("Z994861", "F_Z994861 E_Z994861"),
                Medunderskriver("Z994864", "F_Z994864 E_Z994864"),
                Medunderskriver("Z994863", "F_Z994863 E_Z994863"),
                Medunderskriver("Z994862", "F_Z994862 E_Z994862"),
            ).filter { it.ident != navIdent }
        )
    }

    private fun logEnheter(enheter: List<EnhetView>, navIdent: String) {
        enheter.forEach { enhet ->
            logger.debug(
                "{} has access to {} ({}) with ytelser {}",
                navIdent,
                enhet.id,
                enhet.navn,
                enhet.lovligeYtelser.joinToString(separator = ",")
            )
        }
    }

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

}

