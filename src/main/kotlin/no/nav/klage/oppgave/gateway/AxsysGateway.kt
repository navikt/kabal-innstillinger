package no.nav.klage.oppgave.gateway

import no.nav.klage.oppgave.domain.saksbehandler.Enhet

//TODO remove?
interface AxsysGateway {
    fun getEnheterForSaksbehandler(ident: String): List<Enhet>
}