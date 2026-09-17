package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class TildelteYtelserResponse(
    val ytelseIdList: List<String>,
)

data class SaksbehandlerAccessResponse(
    val accessRights: List<SaksbehandlerAccess>,
)

data class SaksbehandlerAccess(
    val saksbehandlerIdent: String,
    val saksbehandlerName: String,
    val ytelseIdList: List<String>,
    val anketeam: Boolean,
    val created: LocalDateTime?,
    val accessRightsModified: LocalDateTime?,
)

data class AccessInput(
    val accessRights: List<AccessRightInput>,
) {
    data class AccessRightInput(
        val saksbehandlerIdent: String,
        val ytelseIdList: List<String>,
        val anketeam: Boolean = false,
    )
}
