package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupClient
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val klageLookupClient: KlageLookupClient,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun hasSaksbehandlerAccessToSak(
        navIdent: String,
        fnr: String,
        ytelse: Ytelse,
        sakId: String?,
        fagsystem: Fagsystem?,
    ): Boolean {
        return klageLookupClient.getAccess(
            brukerId = fnr,
            navIdent = navIdent,
            sakId = sakId,
            ytelse = ytelse,
            fagsystem = fagsystem,
        ).access
    }

    data class Access(
        val access: Boolean,
        val reason: String,
    )
}