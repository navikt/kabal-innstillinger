package no.nav.klage.oppgave.api.view

data class Saksbehandlere(val saksbehandlere: List<Saksbehandler>)

data class Saksbehandler(val navIdent: String, val navn: String)

data class MedunderskrivereForYtelse(
    //TODO: Dette er ytelseId, bør få nytt navn.
    val ytelse: String,
    val medunderskrivere: List<Saksbehandler>
)

