package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.AbbreviationInput
import no.nav.klage.oppgave.api.view.AbbreviationResponse
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.AbbreviationService
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
    }

    @GetMapping(produces = ["application/json"])
    fun getAbbreviationsForInnloggetSaksbehandler(): List<AbbreviationResponse> {
        val navIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = navIdent, methodName = ::getAbbreviationsForInnloggetSaksbehandler.name)
        return abbreviationService.getAbbreviationsForSaksbehandler(navIdent = navIdent)
    }

    @PostMapping(produces = ["application/json"])
    fun createAbbreviationForInnloggetSaksbehandler(
        @RequestBody input: AbbreviationInput,
    ): AbbreviationResponse {
        val navIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = navIdent, methodName = ::createAbbreviationForInnloggetSaksbehandler.name)
        return abbreviationService.createAbbreviationForSaksbehandler(
            short = input.short,
            long = input.long,
            navIdent = navIdent,
        )
    }

    @PutMapping("/{abbreviationId}", produces = ["application/json"])
    fun updateAbbreviation(
        @RequestBody input: AbbreviationInput,
        @PathVariable abbreviationId: UUID,
    ): AbbreviationResponse {
        val navIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = navIdent, methodName = ::updateAbbreviation.name)
        return abbreviationService.updateAbbreviation(
            abbreviationId = abbreviationId,
            short = input.short,
            long = input.long,
            navIdent = navIdent,
        )
    }

    @DeleteMapping("/{abbreviationId}")
    fun deleteAbbreviation(
        @PathVariable abbreviationId: UUID,
    ) {
        val navIdent = tokenUtil.getCurrentIdent()
        logMethodCall(navIdent = navIdent, methodName = ::deleteAbbreviation.name)
        abbreviationService.deleteAbbreviation(
            abbreviationId = abbreviationId,
            navIdent = navIdent,
        )
    }

    private fun logMethodCall(
        navIdent: String,
        methodName: String,
    ) {
        logger.debug("$methodName is requested by $navIdent")
    }
}
