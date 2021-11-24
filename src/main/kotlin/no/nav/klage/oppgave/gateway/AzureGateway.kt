package no.nav.klage.oppgave.gateway

import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerRolle

interface AzureGateway {
    fun getPersonligDataOmSaksbehandlerMedIdent(navIdent: String): SaksbehandlerPersonligInfo
    fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo
    fun getRollerForInnloggetSaksbehandler(): List<SaksbehandlerRolle>
}