package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.AbbreviationService
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.UUID

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "Abbreviations")
@RequestMapping("/me/abbreviations")
class AbbreviationController(
    private val abbreviationService: AbbreviationService,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @GetMapping(produces = ["application/json"])
    fun getAbbreviationsForInnloggetSaksbehandler(): List<AbbreviationResponse> {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::getAbbreviationsForInnloggetSaksbehandler.name} is requested by $navIdent")
        return abbreviationService.getAbbreviationsForSaksbehandler(navIdent = navIdent)
    }

    @PostMapping(produces = ["application/json"])
    fun createAbbreviationForInnloggetSaksbehandler(
        @RequestBody input: AbbreviationInput
    ): AbbreviationResponse {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::createAbbreviationForInnloggetSaksbehandler.name} is requested by $navIdent")
        return abbreviationService.createAbbreviationForSaksbehandler(
            short = input.short,
            long = input.long,
            navIdent = navIdent,
        )
    }

    @PutMapping("/{abbreviationId}", produces = ["application/json"])
    fun updateAbbreviation(
        @RequestBody input: AbbreviationInput,
        @PathVariable abbreviationId: UUID
    ): AbbreviationResponse {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::updateAbbreviation.name} is requested by $navIdent")
        return abbreviationService.updateAbbreviation(
            abbreviationId = abbreviationId,
            short = input.short,
            long = input.long,
            navIdent = navIdent
        )
    }

    @DeleteMapping("/{abbreviationId}")
    fun deleteAbbreviation(
        @PathVariable abbreviationId: UUID
    ) {
        val navIdent = tokenUtil.getCurrentIdent()
        logger.debug("${::deleteAbbreviation.name} is requested by $navIdent")
        abbreviationService.deleteAbbreviation(
            abbreviationId = abbreviationId,
            navIdent = navIdent
        )
    }
}