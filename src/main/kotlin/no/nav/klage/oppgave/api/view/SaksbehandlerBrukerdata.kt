package no.nav.klage.oppgave.api.view

data class SaksbehandlerView(
    val info: PersonligInfoView,
    val roller: List<String>,
    val enheter: List<EnhetView>,
    val valgtEnhetView: EnhetView,
    val innstillinger: InnstillingerView
) {
    data class PersonligInfoView(
        val navIdent: String,
        val azureId: String,
        val fornavn: String,
        val etternavn: String,
        val sammensattNavn: String,
        val epost: String
    )

    data class InnstillingerView(
        val hjemler: List<String>,
        val temaer: List<String>,
        val typer: List<String>
    )
}

data class SaksbehandlerRefView(
    val navIdent: String,
    val navn: String
)