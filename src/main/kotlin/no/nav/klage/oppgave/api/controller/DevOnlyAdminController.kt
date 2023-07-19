package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.clients.nom.GetAnsattResponse
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Profile("dev-gcp")
@RestController
class DevOnlyAdminController(
    private val saksbehandlerService: SaksbehandlerService
) {
    @Unprotected
    @GetMapping("/internal/ansatte/{navident}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun getAnsattInfo(@PathVariable("navident") navIdent: String): GetAnsattResponse {
        return saksbehandlerService.getAnsattInfoFromNom(navIdent)
    }

    @Unprotected
    @GetMapping("/internal/ansatte", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun logAllAnsatteInfo() {
        return saksbehandlerService.logAnsattStatusInNom()
    }
}