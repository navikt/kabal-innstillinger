package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.gateway.AzureGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoleUtils(
    private val tokenUtil: TokenUtil,
    private val azureGateway: AzureGateway,
    @Value("\${KABAL_OPPGAVESTYRING_ALLE_ENHETER_ROLE_ID}") private val kabalOppgavestyringAlleEnheterRoleId: String,
    @Value("\${KABAL_MALTEKSTREDIGERING_ROLE_ID}") private val kabalMaltekstredigeringRoleId: String,
    @Value("\${KABAL_SAKSBEHANDLING_ROLE_ID}") private val kabalSaksbehandlingRoleId: String,
    @Value("\${KABAL_FAGTEKSTREDIGERING_ROLE_ID}") private val kabalFagtekstredigeringRoleId: String,
    @Value("\${KABAL_INNSYN_EGEN_ENHET_ROLE_ID}") private val kabalInnsynEgenEnhetRoleId: String,
    @Value("\${KABAL_TILGANGSSTYRING_EGEN_ENHET_ROLE_ID}") private val kabalTilgangsstyringEgenEnhetRoleId: String,
    @Value("\${FORTROLIG_ROLE_ID}") private val fortroligRoleId: String,
    @Value("\${STRENGT_FORTROLIG_ROLE_ID}") private val strengtFortroligRoleId: String,
    @Value("\${EGEN_ANSATT_ROLE_ID}") private val egenAnsattRoleId: String,
    @Value("\${KABAL_ADMIN_ROLE_ID}") private val kabalAdminRoleId: String,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getRoleNamesFromId(roleId: String): List<String> {
        return when (roleId) {
            kabalOppgavestyringAlleEnheterRoleId -> listOf(
                "KABAL_OPPGAVESTYRING_ALLE_ENHETER"
            )

            kabalMaltekstredigeringRoleId -> listOf("KABAL_MALTEKSTREDIGERING")
            kabalSaksbehandlingRoleId -> listOf("KABAL_SAKSBEHANDLING")
            kabalFagtekstredigeringRoleId -> listOf("KABAL_FAGTEKSTREDIGERING")
            kabalInnsynEgenEnhetRoleId -> listOf(
                "KABAL_INNSYN_EGEN_ENHET"
            )

            fortroligRoleId -> listOf("FORTROLIG")
            strengtFortroligRoleId -> listOf("STRENGT_FORTROLIG")
            egenAnsattRoleId -> listOf("EGEN_ANSATT")
            kabalAdminRoleId -> listOf("KABAL_ADMIN")
            kabalTilgangsstyringEgenEnhetRoleId -> listOf("KABAL_TILGANGSSTYRING_EGEN_ENHET")
            else -> emptyList()
        }
    }

    fun isKabalTilgangsstyringEgenEnhet() =
        tokenUtil.getRoleIdsFromToken().contains(kabalTilgangsstyringEgenEnhetRoleId)

    fun isSaksbehandler() = tokenUtil.getRoleIdsFromToken().contains(kabalSaksbehandlingRoleId)

    fun isSaksbehandler(ident: String) = azureGateway.getRoleIds(ident).contains(kabalSaksbehandlingRoleId)

    fun kanBehandleStrengtFortrolig(ident: String) = azureGateway.getRoleIds(ident).contains(strengtFortroligRoleId)

    fun kanBehandleFortrolig(ident: String) = azureGateway.getRoleIds(ident).contains(fortroligRoleId)

    fun kanBehandleEgenAnsatt(ident: String) = azureGateway.getRoleIds(ident).contains(egenAnsattRoleId)

    fun isAdmin() = tokenUtil.getRoleIdsFromToken().contains(kabalAdminRoleId)
}