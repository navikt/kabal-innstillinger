package no.nav.klage.oppgave.gateway

import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerIdent

interface AxsysGateway {

    fun getEnheterForSaksbehandler(ident: String): List<Enhet>
    fun getSaksbehandlereIEnhet(enhetId: String): List<SaksbehandlerIdent>
}