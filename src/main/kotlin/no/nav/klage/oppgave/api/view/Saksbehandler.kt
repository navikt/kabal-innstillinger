package no.nav.klage.oppgave.api.view

data class Saksbehandlere(val saksbehandlere: List<Saksbehandler>)

data class Saksbehandler(val navIdent: String, val navn: String)

data class SaksbehandlerSearchInput(
    val ytelseId: String,
    val fnr: String,
)
