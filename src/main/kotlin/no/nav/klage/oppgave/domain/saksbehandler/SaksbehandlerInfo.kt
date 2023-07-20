package no.nav.klage.oppgave.domain.saksbehandler

import no.nav.klage.kodeverk.Ytelse

data class SaksbehandlerInfo(
    val navIdent: String,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeYtelser,
    val ansattEnhet: EnhetMedLovligeYtelser,
    val saksbehandlerInnstillinger: SaksbehandlerInnstillinger,
    val tildelteYtelser: List<Ytelse>,
)