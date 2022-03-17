package no.nav.klage.oppgave.domain.saksbehandler

data class SaksbehandlerInfo(
    val info: SaksbehandlerPersonligInfo,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeYtelser,
    val ansattEnhet: EnhetMedLovligeYtelser,
    val valgtEnhet: EnhetMedLovligeYtelser,
    val innstillinger: SaksbehandlerInnstillinger,
)