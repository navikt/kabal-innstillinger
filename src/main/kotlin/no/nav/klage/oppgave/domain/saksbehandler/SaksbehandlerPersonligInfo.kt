package no.nav.klage.oppgave.domain.saksbehandler

data class SaksbehandlerPersonligInfo(
    val fornavn: String,
    val etternavn: String,
    val sammensattNavn: String,
    val enhet: Enhet,
)