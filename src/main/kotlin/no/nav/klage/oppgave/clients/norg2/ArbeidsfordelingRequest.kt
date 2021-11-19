package no.nav.klage.oppgave.clients.norg2

data class ArbeidsfordelingRequest(
    val behandlingstema: String,
    val behandlingstype: String,
    val diskresjonskode: String,
    val enhetNummer: String,
    val geografiskOmraade: String,
    val oppgavetype: String,
    val skjermet: Boolean,
    val tema: String,
    val temagruppe: String
)
