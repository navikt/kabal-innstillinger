package no.nav.klage.oppgave.api.view

import java.util.UUID

data class AbbreviationResponse(
    val id: UUID,
    val short: String,
    val long: String,
)

data class AbbreviationInput(
    val short: String,
    val long: String,
)
