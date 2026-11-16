package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val klageLookupGateway: KlageLookupGateway,
) {
    /**
     * Returns the navIdents in [navIdentList] that have access to the person.
     * An ident missing from the response counts as no access.
     */
    fun getNavIdentsWithAccessToPerson(
        fnr: String,
        navIdentList: List<String>,
    ): Set<String> {
        if (navIdentList.isEmpty()) {
            return emptySet()
        }
        return klageLookupGateway
            .getAccessBatched(
                brukerId = fnr,
                navIdentList = navIdentList,
            ).filter { it.access }
            .map { it.navIdent }
            .toSet()
    }
}
