package no.nav.klage.oppgave.api.view

data class MedunderskrivereForYtelse(val ytelse: String, val medunderskrivere: List<Saksbehandler>)

data class MedunderskrivereInput(
    val ytelseId: String,
    val fnr: String?,
    val enhet: String,
    val navIdent: String,
)