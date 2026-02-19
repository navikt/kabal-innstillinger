package no.nav.klage.oppgave.api.view

data class SearchMedunderskrivereInput(
    val enhet: String,
    val navIdent: String,
    val sak: SakInput,
)

data class SakInput(
    val fnr: String,
    val sakId: String,
    val ytelseId: String,
    val fagsystemId: String,
)

data class SearchSaksbehandlereForBrukerInput(
    val fnr: String,
    val ytelseId: String,
)