package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.mapper.SaksbehandlerMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.InnstillingerService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.klage.oppgave.util.trimToNull
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Saksbehandler")
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
    private val innstillingerService: InnstillingerService,
    private val saksbehandlerMapper: SaksbehandlerMapper,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Operation(
        summary = "Hent brukerdata for innlogget ansatt",
        description = "Henter alle brukerdata om en saksbehandler"
    )
    @GetMapping("/me/brukerdata", produces = ["application/json"])
    fun getBrukerdataForInnloggetSaksbehandler(): SaksbehandlerView {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::getBrukerdataForInnloggetSaksbehandler.name} is requested by $navIdent")

        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent))
    }

    @Operation(
        summary = "Hent innstillinger for innlogget ansatt",
        description = "Henter alle innstillinger for en saksbehandler"
    )
    @GetMapping("/me/innstillinger", produces = ["application/json"])
    fun getInnstillingerForInnloggetSaksbehandler(): InnstillingerView {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::getInnstillingerForInnloggetSaksbehandler.name} is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent).saksbehandlerInnstillinger)
    }

    @Operation(
        summary = "Setter innstillinger for en ansatt",
        description = "Setter valgte ytelser og hjemler som den ansatte jobber med"
    )
    @PutMapping("/me/innstillinger", produces = ["application/json"])
    fun setInnstillinger(
        @RequestBody input: InnstillingerView
    ): InnstillingerView {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::setInnstillinger.name} is requested by $navIdent")
        return saksbehandlerMapper.mapToView(
            saksbehandlerService.storeInnstillingerButKeepSignature(
                navIdent,
                saksbehandlerMapper.mapToDomain(input)
            )
        )
    }

    @Operation(
        summary = "Get signature for saksbehandler",
        description = "Get signature for saksbehandler"
    )
    @GetMapping("/ansatte/{navIdent}/signature", produces = ["application/json"])
    fun getSignature(
        @Parameter(name = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
    ): Signature {
        return saksbehandlerService.getSignature(navIdent)
    }

    @Operation(
        summary = "Get signature for saksbehandler",
        description = "Get signature for saksbehandler"
    )
    @GetMapping("/me/signature", produces = ["application/json"])
    fun getSignature(): Signature {
        val navIdent = tokenUtil.getCurrentIdent()
        return saksbehandlerService.getSignature(navIdent)
    }

    @Operation(
        summary = "Set short name for saksbehandler",
        description = "Set short name for saksbehandler"
    )
    @PutMapping("/me/customShortName", produces = ["application/json"])
    fun setShortName(
        @RequestBody input: StringInputView
    ): StringInputView {
        val navIdent = tokenUtil.getCurrentIdent()
        innstillingerService.storeShortName(
            navIdent,
            input.value.trimToNull(),
        )

        return input
    }

    @Operation(
        summary = "Set long name for saksbehandler",
        description = "Set long name for saksbehandler"
    )
    @PutMapping("/me/customLongName", produces = ["application/json"])
    fun setLongName(
        @RequestBody input: StringInputView
    ): StringInputView {
        val navIdent = tokenUtil.getCurrentIdent()
        innstillingerService.storeLongName(
            navIdent,
            input.value.trimToNull(),
        )

        return input
    }

    @Operation(
        summary = "Set job title for saksbehandler",
        description = "Set job title for saksbehandler"
    )
    @PutMapping("/me/customJobTitle", produces = ["application/json"])
    fun setJobTitle(
        @RequestBody input: StringInputView
    ): StringInputView {
        val navIdent = tokenUtil.getCurrentIdent()
        innstillingerService.storeJobTitle(
            navIdent,
            input.value.trimToNull(),
        )

        return input
    }

    @Operation(
        summary = "Set anonymous toggle in signature for saksbehandler",
        description = "Set anonymous toggle in signature for saksbehandler"
    )
    @PutMapping("/me/anonymous", produces = ["application/json"])
    fun setAnonymous(
        @RequestBody input: BooleanInputView
    ): BooleanInputView {
        val navIdent = tokenUtil.getCurrentIdent()
        innstillingerService.storeAnonymous(
            navIdent,
            input.value,
        )

        return input
    }
}

