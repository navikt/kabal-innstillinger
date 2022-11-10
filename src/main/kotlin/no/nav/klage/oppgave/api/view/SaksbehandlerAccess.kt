package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class SaksbehandlerAccessResponse(
    val accessRights: List<SaksbehandlerAccess>
)

data class SaksbehandlerAccess(
    val saksbehandlerIdent: String,
    val saksbehandlerName: String,
    val ytelseIdList: List<String>,
    val created: LocalDateTime?,
    val accessRightsModified: LocalDateTime?,
)

data class YtelseInput(
    val accessRights: List<AccessRightInput>
) {
    data class AccessRightInput(
        val saksbehandlerIdent: String,
        val ytelseIdList: List<String>
    )
}