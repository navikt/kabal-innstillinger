package no.nav.klage.oppgave.api.view

data class SaksbehandlerView(
    val info: PersonligInfoView,
    val roller: List<String>,
    val enheter: List<EnhetView>,
    val ansattEnhet: EnhetView,
    val valgtEnhetView: EnhetView,
    val innstillinger: InnstillingerView
) {
    data class PersonligInfoView(
        val navIdent: String,
        val azureId: String,
        val sammensattNavn: String,
        val epost: String,
        //For signature and maybe more?
        val shortName: String?,
        val longName: String?,
        val jobTitle: String?,
    )

    data class InnstillingerView(
        val hjemler: List<String>,
        val ytelser: List<String>,
        val typer: List<String>
    )
}

data class SaksbehandlerRefView(
    val navIdent: String,
    val navn: String
)

data class StringInputView(val value: String)