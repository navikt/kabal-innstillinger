package no.nav.klage.oppgave.api.view

data class Medunderskrivere(val ytelse: String, val medunderskrivere: List<Medunderskriver>)

data class Medunderskriver(val navIdent: String, val navn: String)

data class MedunderskrivereInput(
    val ytelse: String,
    val fnr: String,
    val enhet: String,
    val navIdent: String,
)