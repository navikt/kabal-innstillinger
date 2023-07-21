package no.nav.klage.oppgave.api.view

data class SearchROLInput(
    val fnr: String,
)

data class SearchMedunderskrivereInput(
    val ytelseId: String,
    val fnr: String?,
    val enhet: String,
    val navIdent: String,
)

data class SearchSaksbehandlerInput(
    val ytelseId: String,
    val fnr: String,
)