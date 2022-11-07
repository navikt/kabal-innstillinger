package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class EnhetAccess(
    val enhet: String,
    val ytelseIdList: List<String>,
    val created: LocalDateTime,
    val accessRightsModified: LocalDateTime,
)

data class SaksbehandlerAccess(
    val saksbehandlerIdent: String,
    val ytelseIdList: List<String>,
    val created: LocalDateTime,
    val accessRightsModified: LocalDateTime,
)

data class YtelseInput(
    val ytelseIdList: List<String>,
)