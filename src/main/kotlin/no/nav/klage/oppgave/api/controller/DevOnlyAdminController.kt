package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.service.SaksbehandlerAccessService
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.RestController

@Profile("dev")
@RestController
class DevOnlyAdminController(
    private val saksbehandlerAccessService: SaksbehandlerAccessService
) {
    //Not currently in use
}