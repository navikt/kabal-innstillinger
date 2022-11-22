package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.gateway.AzureGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoleUtils(
    private val tokenUtil: TokenUtil,
    private val azureGateway: AzureGateway,
    @Value("\${KABAL_OPPGAVESTYRING_ALLE_ENHETER}") private val oppgavestyringAlleEnheterRole: String,
    @Value("\${KABAL_MALTEKSTREDIGERING}") private val maltekstredigering: String,
    @Value("\${KABAL_SAKSBEHANDLING}") private val saksbehandlerRole: String,
    @Value("\${KABAL_FAGTEKSTREDIGERING}") private val fagansvarligRole: String,
    @Value("\${KABAL_OPPGAVESTYRING_EGEN_ENHET}") private val lederRole: String,
    @Value("\${FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String,
    @Value("\${KABAL_ADMIN}") private val adminRole: String,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getRoleNamesFromId(roleId: String): List<String> {
        return when (roleId) {
            oppgavestyringAlleEnheterRole -> listOf("ROLE_KLAGE_OPPGAVESTYRING_ALLE_ENHETER", "KABAL_OPPGAVESTYRING_ALLE_ENHETER")
            maltekstredigering -> listOf("ROLE_KLAGE_MALTEKSTREDIGERING", "KABAL_MALTEKSTREDIGERING")
            saksbehandlerRole -> listOf("ROLE_KLAGE_SAKSBEHANDLER", "KABAL_SAKSBEHANDLING")
            fagansvarligRole -> listOf("ROLE_KLAGE_FAGANSVARLIG", "KABAL_FAGTEKSTREDIGERING")
            lederRole -> listOf("ROLE_KLAGE_LEDER", "KABAL_OPPGAVESTYRING_EGEN_ENHET")
            kanBehandleFortroligRole -> listOf("ROLE_KLAGE_FORTROLIG", "FORTROLIG")
            kanBehandleStrengtFortroligRole -> listOf("ROLE_KLAGE_STRENGT_FORTROLIG", "STRENGT_FORTROLIG")
            kanBehandleEgenAnsattRole -> listOf("ROLE_KLAGE_EGEN_ANSATT", "EGEN_ANSATT")
            adminRole -> listOf("ROLE_ADMIN", "KABAL_ADMIN")
            else -> emptyList()
        }
    }

    fun isLeder() = tokenUtil.getRollerFromToken().hasRole(lederRole)

    fun isSaksbehandler() = tokenUtil.getRollerFromToken().hasRole(saksbehandlerRole)

    fun isSaksbehandler(ident: String) = azureGateway.getRolleIder(ident).hasRole(saksbehandlerRole)

    fun getKanBehandleFortroligRoleId(): String = kanBehandleFortroligRole

    fun kanBehandleStrengtFortrolig(ident: String) = azureGateway.getRolleIder(ident).hasRole(kanBehandleStrengtFortroligRole)

    fun kanBehandleFortrolig(ident: String) = azureGateway.getRolleIder(ident).hasRole(kanBehandleFortroligRole)

    fun kanBehandleEgenAnsatt(ident: String) = azureGateway.getRolleIder(ident).hasRole(kanBehandleEgenAnsattRole)

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }

}