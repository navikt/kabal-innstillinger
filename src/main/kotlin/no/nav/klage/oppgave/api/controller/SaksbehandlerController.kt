package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.SaksbehandlerMapper
import no.nav.klage.oppgave.api.view.SaksbehandlerView
import no.nav.klage.oppgave.api.view.Signature
import no.nav.klage.oppgave.api.view.StringInputView
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.trimToNull
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Api(tags = ["kabal-innstillinger"])
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerMapper: SaksbehandlerMapper,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent brukerdata for innlogget ansatt",
        notes = "Henter alle brukerdata om en saksbehandler"
    )
    @GetMapping("/me/brukerdata", produces = ["application/json"])
    fun getBrukerdata(): SaksbehandlerView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug("getBrukerdata is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent))
    }

    @ApiOperation(
        value = "Hent innstillinger for innlogget ansatt",
        notes = "Henter alle innstillinger for en saksbehandler"
    )
    @GetMapping("/me/innstillinger", produces = ["application/json"])
    fun getInnstillinger(): SaksbehandlerView.InnstillingerView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug("getBrukerdata is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent).saksbehandlerInnstillinger)
    }

    @ApiOperation(
        value = "Setter innstillinger for en ansatt",
        notes = "Setter valgt tema, hjemmel og type som den ansatte jobber med"
    )
    @PutMapping("/me/innstillinger", produces = ["application/json"])
    fun setInnstillinger(
        @RequestBody input: SaksbehandlerView.InnstillingerView
    ): SaksbehandlerView.InnstillingerView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        return saksbehandlerMapper.mapToView(
            saksbehandlerService.storeInnstillinger(
                navIdent,
                saksbehandlerMapper.mapToDomain(input)
            )
        )
    }

    @ApiOperation(
        value = "Get signature for saksbehandler",
        notes = "Get signature for saksbehandler"
    )
    @GetMapping("/ansatte/{navIdent}/signature", produces = ["application/json"])
    fun getSignature(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
    ): Signature {
        validateNavIdent(navIdent)
        return saksbehandlerService.getSignature(navIdent)
    }

    @ApiOperation(
        value = "Get signature for saksbehandler",
        notes = "Get signature for saksbehandler"
    )
    @GetMapping("/me/signature", produces = ["application/json"])
    fun getSignature(): Signature {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        return saksbehandlerService.getSignature(navIdent)
    }

    @ApiOperation(
        value = "Set short name for saksbehandler",
        notes = "Set short name for saksbehandler"
    )
    @PutMapping("/me/customShortName", produces = ["application/json"])
    fun setShortName(
        @RequestBody input: StringInputView
    ): StringInputView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        saksbehandlerService.storeShortName(
            navIdent,
            input.value.trimToNull(),
        )

        return input
    }

    @ApiOperation(
        value = "Set long name for saksbehandler",
        notes = "Set long name for saksbehandler"
    )
    @PutMapping("/me/customLongName", produces = ["application/json"])
    fun setLongName(
        @RequestBody input: StringInputView
    ): StringInputView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        saksbehandlerService.storeLongName(
            navIdent,
            input.value.trimToNull(),
        )

        return input
    }

    @ApiOperation(
        value = "Set job title for saksbehandler",
        notes = "Set job title for saksbehandler"
    )
    @PutMapping("/me/customJobTitle", produces = ["application/json"])
    fun setJobTitle(
        @RequestBody input: StringInputView
    ): StringInputView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        saksbehandlerService.storeJobTitle(
            navIdent,
            input.value.trimToNull(),
        )

        return input
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

