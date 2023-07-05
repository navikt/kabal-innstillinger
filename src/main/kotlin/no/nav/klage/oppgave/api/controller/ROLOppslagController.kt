package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.Saksbehandlere
import no.nav.klage.oppgave.api.view.SearchROLInput
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RestController
@Tag(name = "ROL")
class ROLOppslagController(
    private val saksbehandlerService: SaksbehandlerService,
    private val innloggetAnsattRepository: InnloggetAnsattRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Hent ROLs for en ansatt",
        description = "Henter alle ROLs som kan brukes for en gitt ytelse og fnr."
    )
    @PostMapping(
        "/search/rol",
        produces = ["application/json"]
    )
    fun getROLForFnr(
        @RequestBody input: SearchROLInput,
    ): Saksbehandlere {
        val innloggetSaksbehandlerNavIdent = innloggetAnsattRepository.getInnloggetIdent()
        logger.debug("getROLForFnr is requested by $innloggetSaksbehandlerNavIdent")
        return saksbehandlerService.getROLList(
            fnr = input.fnr
        )
    }

}