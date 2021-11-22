package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.gateway.AxsysGateway
import org.springframework.stereotype.Service

@Service
class EnhetRepository(
    private val axsysGateway: AxsysGateway
) {

    fun getAnsatteIEnhet(enhetId: String): List<String> {
        return axsysGateway.getSaksbehandlereIEnhet(enhetId).map { it.navIdent }
    }

}