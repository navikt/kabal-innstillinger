package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.gateway.AzureGateway
import org.springframework.stereotype.Service

@Service
class EnhetRepository(
    private val azureGateway: AzureGateway
) {

    fun getAnsatteIEnhet(enhetId: String): List<String> {
        return azureGateway.getEnhetensAnsattesNavIdents(enhetId)
    }

}