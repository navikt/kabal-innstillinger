package no.nav.klage.oppgave.api.view

data class SearchROLInput(
    val fnr: String,
    val sak: SakInput?,
)

data class SearchMedunderskrivereInput(
    val ytelseId: String,
    val fnr: String,
    val enhet: String,
    val navIdent: String,
    val sak: SakInput?,
)

data class SearchSaksbehandlerInput(
    val ytelseId: String,
    val fnr: String,
    val sak: SakInput?,
)

data class SakInput(
    val fnr: String,
    val sakId: String,
    val ytelseId: String,
    val fagsystemId: String,
)