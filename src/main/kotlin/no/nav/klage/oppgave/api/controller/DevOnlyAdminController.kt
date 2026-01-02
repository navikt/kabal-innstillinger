package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.clients.nom.GetAnsattResponse
import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Profile("dev")
@RestController
class DevOnlyAdminController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService
) {
    @Unprotected
    @GetMapping("/internal/ansatte/{navident}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun getAnsattInfo(@PathVariable("navident") navIdent: String): GetAnsattResponse {
        return saksbehandlerAccessService.getAnsattInfoFromNom(navIdent)
    }

    @Unprotected
    @GetMapping("/internal/ansatte", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun logAllAnsatteInfo() {
        return saksbehandlerAccessService.logAnsattStatusInNom()
    }
}