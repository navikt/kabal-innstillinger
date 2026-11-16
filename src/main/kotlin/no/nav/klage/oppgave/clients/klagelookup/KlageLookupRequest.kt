package no.nav.klage.oppgave.clients.klagelookup

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse

data class BatchedAccessRequest(
    val brukerId: String,
    val navIdentList: List<String>,
)

data class GetPersonRequest(
    val fnr: String,
)

data class Sak(
    val sakId: String,
    val ytelse: Ytelse,
    val fagsystem: Fagsystem,
)

data class BatchedUserRequest(
    val navIdentList: List<String>,
)
