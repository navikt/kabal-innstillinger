package no.nav.klage.oppgave.domain.saksbehandler

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel

data class SaksbehandlerInnstillinger(
    val hjemler: List<Hjemmel> = emptyList(),
    val ytelser: List<Ytelse> = emptyList(),
    val typer: List<Type> = emptyList(),
    val shortName: String? = null,
    val longName: String? = null,
    val jobTitle: String? = null,
)