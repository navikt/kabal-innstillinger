package no.nav.klage.oppgave.domain.saksbehandler

import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse

data class SaksbehandlerInfo(
    val navIdent: String,
    val navn: String,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeYtelser,
    val ansattEnhet: EnhetMedLovligeYtelser,
    val saksbehandlerInnstillinger: SaksbehandlerInnstillinger,
    val tildelteYtelser: Set<Ytelse>,
)

data class SaksbehandlerInnstillinger(
    val hjemler: Set<Hjemmel> = mutableSetOf(),
    val ytelser: Set<Ytelse> = mutableSetOf(),
    val shortName: String? = null,
    val longName: String? = null,
    val jobTitle: String? = null,
    val anonymous: Boolean,
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