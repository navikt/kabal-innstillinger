package no.nav.klage.oppgave.api.view

data class SaksbehandlerView(
    val navIdent: String,
    val roller: List<String>,
    val enheter: List<EnhetView>,
    val ansattEnhet: EnhetView,
    val tildelteYtelser: List<String>,
)

data class InnstillingerView(
    val hjemler: List<String>,
    val ytelser: List<String>,
    val typer: List<String>
)

data class EnhetView(
    val id: String,
    val navn: String,
    val lovligeYtelser: List<String>
)

data class StringInputView(val value: String?)

data class Signature(
    val longName: String,
    val generatedShortName: String?,
    val customLongName: String?,
    val customShortName: String?,
    val customJobTitle: String?,
)