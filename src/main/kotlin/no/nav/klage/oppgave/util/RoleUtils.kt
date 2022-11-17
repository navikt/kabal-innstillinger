package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.gateway.AzureGateway
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoleUtils(
    private val tokenUtil: TokenUtil,
    private val azureGateway: AzureGateway,
    @Value("\${ROLE_KLAGE_OPPGAVESTYRING_ALLE_ENHETER}") private val oppgavestyringAlleEnheterRole: String,
    @Value("\${ROLE_KLAGE_MALTEKSTREDIGERING}") private val maltekstredigering: String,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String,
    @Value("\${ROLE_ADMIN}") private val adminRole: String,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getRoleNameFromId(roleId: String): String {
        return when (roleId) {
            oppgavestyringAlleEnheterRole -> "ROLE_KLAGE_OPPGAVESTYRING_ALLE_ENHETER"
            maltekstredigering -> "ROLE_KLAGE_MALTEKSTREDIGERING"
            saksbehandlerRole -> "ROLE_KLAGE_SAKSBEHANDLER"
            fagansvarligRole -> "ROLE_KLAGE_FAGANSVARLIG"
            lederRole -> "ROLE_KLAGE_LEDER"
            kanBehandleFortroligRole -> "ROLE_KLAGE_FORTROLIG"
            kanBehandleStrengtFortroligRole -> "ROLE_KLAGE_STRENGT_FORTROLIG"
            kanBehandleEgenAnsattRole -> "ROLE_KLAGE_EGEN_ANSATT"
            adminRole -> "ROLE_ADMIN"
            else -> {
                logger.error("Could not find role name for role id {}", roleId)
                "UNKNOWN"
            }
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