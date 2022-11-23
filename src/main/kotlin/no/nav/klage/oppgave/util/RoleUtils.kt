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
            kabalOppgavestyringAlleEnheterRoleId -> listOf("ROLE_KLAGE_OPPGAVESTYRING_ALLE_ENHETER", "KABAL_OPPGAVESTYRING_ALLE_ENHETER")
            kabalMaltekstredigeringRoleId -> listOf("ROLE_KLAGE_MALTEKSTREDIGERING", "KABAL_MALTEKSTREDIGERING")
            kabalSaksbehandlingRoleId -> listOf("ROLE_KLAGE_SAKSBEHANDLER", "KABAL_SAKSBEHANDLING")
            kabalFagtekstredigeringRoleId -> listOf("ROLE_KLAGE_FAGANSVARLIG", "KABAL_FAGTEKSTREDIGERING")
            kabalInnsynEgenEnhetRoleId -> listOf("ROLE_KLAGE_LEDER", "KABAL_INNSYN_EGEN_ENHET")
            fortroligRoleId -> listOf("ROLE_KLAGE_FORTROLIG", "FORTROLIG")
            strengtFortroligRoleId -> listOf("ROLE_KLAGE_STRENGT_FORTROLIG", "STRENGT_FORTROLIG")
            egenAnsattRoleId -> listOf("ROLE_KLAGE_EGEN_ANSATT", "EGEN_ANSATT")
            kabalAdminRoleId -> listOf("ROLE_ADMIN", "KABAL_ADMIN")
            else -> emptyList()
        }
    }

    fun isKabalInnsynEgenEnhet() = tokenUtil.getRoleIdsFromToken().contains(kabalInnsynEgenEnhetRoleId)

    fun isSaksbehandler() = tokenUtil.getRoleIdsFromToken().contains(kabalSaksbehandlingRoleId)

    fun isSaksbehandler(ident: String) = azureGateway.getRoleIds(ident).contains(kabalSaksbehandlingRoleId)

    fun getKanBehandleFortroligRoleId(): String = fortroligRoleId

    fun kanBehandleStrengtFortrolig(ident: String) = azureGateway.getRoleIds(ident).contains(strengtFortroligRoleId)

    fun kanBehandleFortrolig(ident: String) = azureGateway.getRoleIds(ident).contains(fortroligRoleId)

    fun kanBehandleEgenAnsatt(ident: String) = azureGateway.getRoleIds(ident).contains(egenAnsattRoleId)
}