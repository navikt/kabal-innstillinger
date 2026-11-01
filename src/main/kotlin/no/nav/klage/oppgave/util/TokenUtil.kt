package no.nav.klage.oppgave.util

import no.nav.klage.auth.TexasClient
import no.nav.klage.auth.currentAzureToken
import no.nav.klage.auth.navIdent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TokenUtil(
    private val texasClient: TexasClient,
    @Value($$"api://${NAIS_CLUSTER_NAME}.${NAIS_NAMESPACE}.${KLAGE_LOOKUP_APP_NAME}/.default")
    private val klageLookupScope: String,
) {
    fun getOnBehalfOfTokenWithKlageLookupScope(): String = texasClient.onBehalfOfCurrentUser(klageLookupScope)

    fun getAppAccessTokenWithKlageLookupScope(): String = texasClient.machineToMachineToken(klageLookupScope)

    fun getCurrentIdent(): String = currentAzureToken()?.navIdent ?: throw RuntimeException("Ident not found in token")
}
