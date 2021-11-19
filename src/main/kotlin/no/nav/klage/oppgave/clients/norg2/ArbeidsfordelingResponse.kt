package no.nav.klage.oppgave.clients.norg2

data class ArbeidsfordelingResponse(
    val behandlingstema: String,
    val behandlingstype: String,
    val diskresjonskode: String,
    val enhetId: Int,
    val enhetNavn: String,
    val enhetNr: String,
    val geografiskOmraade: String,
    val oppgavetype: String,
    val skalTilLokalkontor: Boolean,
    val tema: String,
    val temagruppe: String
)
