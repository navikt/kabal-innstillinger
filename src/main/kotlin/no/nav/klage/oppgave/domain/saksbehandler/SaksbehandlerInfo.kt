package no.nav.klage.oppgave.domain.saksbehandler

data class SaksbehandlerInfo(
    val navIdent: String,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeYtelser,
    val ansattEnhet: EnhetMedLovligeYtelser,
    val valgtEnhet: EnhetMedLovligeYtelser,
    val saksbehandlerInnstillinger: SaksbehandlerInnstillinger,
)