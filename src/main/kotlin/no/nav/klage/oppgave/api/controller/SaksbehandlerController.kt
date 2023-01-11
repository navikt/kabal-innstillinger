package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.mapper.SaksbehandlerMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
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
    private val innloggetAnsattRepository: InnloggetAnsattRepository,
    private val saksbehandlerMapper: SaksbehandlerMapper,
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
    fun getBrukerdata(): SaksbehandlerView {
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
        logger.debug("getBrukerdata is requested by $navIdent")

        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent))
    }

    @Operation(
        summary = "Hent innstillinger for innlogget ansatt",
        description = "Henter alle innstillinger for en saksbehandler"
    )
    @GetMapping("/me/innstillinger", produces = ["application/json"])
    fun getInnstillinger(): SaksbehandlerView.InnstillingerView {
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
        logger.debug("getBrukerdata is requested by $navIdent")
        return saksbehandlerMapper.mapToView(saksbehandlerService.getDataOmSaksbehandler(navIdent).saksbehandlerInnstillinger)
    }

    @Operation(
        summary = "Setter innstillinger for en ansatt",
        description = "Setter valgt tema, hjemmel og type som den ansatte jobber med"
    )
    @PutMapping("/me/innstillinger", produces = ["application/json"])
    fun setInnstillinger(
        @RequestBody input: SaksbehandlerView.InnstillingerView
    ): SaksbehandlerView.InnstillingerView {
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
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
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
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
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
        saksbehandlerService.storeShortName(
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
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
        saksbehandlerService.storeLongName(
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
        val navIdent = innloggetAnsattRepository.getInnloggetIdent()
        saksbehandlerService.storeJobTitle(
            navIdent,
            input.value.trimToNull(),
        )

        return input
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
        @RequestBody input: SaksbehandlerSearchInput
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = innloggetAnsattRepository.getInnloggetIdent()
        logger.debug("getSaksbehandlereForYtelseOgFnr is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerService.getSaksbehandlere(
            ytelse = Ytelse.of(input.ytelseId),
            fnr = input.fnr
        )
    }
}

