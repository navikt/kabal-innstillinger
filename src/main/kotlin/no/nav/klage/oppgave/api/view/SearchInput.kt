package no.nav.klage.oppgave.api.view

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class SearchMedunderskrivereInput(
    val enhet: String,
    val navIdent: String,
    val sak: SakInput,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SakInput(
    val fnr: String,
    val sakId: String,
    val ytelseId: String,
    val fagsystemId: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchSaksbehandlereForBrukerInput(
    val fnr: String,
    val ytelseId: String,
)
