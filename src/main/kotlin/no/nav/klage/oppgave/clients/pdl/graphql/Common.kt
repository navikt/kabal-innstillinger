package no.nav.klage.oppgave.clients.pdl.graphql

import java.net.URL

fun URL.cleanForGraphql() = readText().replace("[\n\r]", "")

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)
