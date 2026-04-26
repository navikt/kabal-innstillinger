package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val klageLookupGateway: KlageLookupGateway,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun hasSaksbehandlerAccessToPerson(
        navIdent: String,
        fnr: String,
    ): Boolean {
        return getSaksbehandlerAccessToPerson(
            navIdent = navIdent,
            fnr = fnr,
        ).access
    }

    private fun getSaksbehandlerAccessToPerson(
        navIdent: String,
        fnr: String,
    ): Access {
        return klageLookupGateway.getAccess(
            brukerId = fnr,
            navIdent = navIdent,
        )
    }

    data class Access(
        val access: Boolean,
        val reason: String,
    )
}