package no.nav.klage.oppgave.domain.saksbehandler

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel

data class SaksbehandlerInfo(
    val navIdent: String,
    val navn: String,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeYtelser,
    val ansattEnhet: EnhetMedLovligeYtelser,
    val saksbehandlerInnstillinger: SaksbehandlerInnstillinger,
    val tildelteYtelser: List<Ytelse>,
)

data class SaksbehandlerInnstillinger(
    val hjemler: List<Hjemmel> = emptyList(),
    val ytelser: List<Ytelse> = emptyList(),
    val typer: List<Type> = emptyList(),
    val shortName: String? = null,
    val longName: String? = null,
    val jobTitle: String? = null,
)

data class SaksbehandlerName(
    val fornavn: String,
    val etternavn: String,
    val sammensattNavn: String,
)

data class SaksbehandlerPersonligInfo(
    val fornavn: String,
    val etternavn: String,
    val sammensattNavn: String,
    val enhet: Enhet,
)

data class SaksbehandlerRolle(
    val id: String,
    val navn: String
)