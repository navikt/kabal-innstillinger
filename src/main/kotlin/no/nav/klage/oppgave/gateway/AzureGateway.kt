package no.nav.klage.oppgave.gateway

import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerRolle

interface AzureGateway {
    fun getDataOmSaksbehandler(navIdent: String): SaksbehandlerPersonligInfo
    fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo
    fun getRollerForSaksbehandler(navIdent: String): List<SaksbehandlerRolle>
    fun getGroupMembersNavIdents(groupId: String): List<String>
    fun getRoleIds(navIdent: String): List<String>
    fun getEnhetensAnsattesNavIdents(enhetNr: String): List<String>
}