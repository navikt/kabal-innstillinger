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
        notes = "Henter alle brukerdata om en saksbehandler, inklusive innstillingene hen har gjort."
    )
    @GetMapping("/me/brukerdata", produces = ["application/json"])
    fun getBrukerdata(): SaksbehandlerView {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug("getBrukerdata is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent))
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
        value = "Set short name for saksbehandler",
        notes = "Set short name for saksbehandler"
    )
    @PutMapping("/ansatte/{navIdent}/brukerdata/shortname", produces = ["application/json"])
    fun setShortName(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: StringInputView
    ): StringInputView {
        validateNavIdent(navIdent)
        saksbehandlerService.storeShortName(
            navIdent,
            input.value,
        )

        return input
    }

    @ApiOperation(
        value = "Set long name for saksbehandler",
        notes = "Set long name for saksbehandler"
    )
    @PutMapping("/ansatte/{navIdent}/brukerdata/longname", produces = ["application/json"])
    fun setLongName(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: StringInputView
    ): StringInputView {
        validateNavIdent(navIdent)
        saksbehandlerService.storeLongName(
            navIdent,
            input.value,
        )

        return input
    }

    @ApiOperation(
        value = "Set job title for saksbehandler",
        notes = "Set job title for saksbehandler"
    )
    @PutMapping("/ansatte/{navIdent}/brukerdata/jobtitle", produces = ["application/json"])
    fun setJobTitle(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: StringInputView
    ): StringInputView {
        validateNavIdent(navIdent)
        saksbehandlerService.storeJobTitle(
            navIdent,
            input.value,
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

