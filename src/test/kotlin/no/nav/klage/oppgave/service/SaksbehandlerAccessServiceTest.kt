package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.clients.nom.NomClient
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerEnhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class SaksbehandlerAccessServiceTest {
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository = mockk()
    private val innstillingerService: InnstillingerService = mockk()
    private val klageLookupGateway: KlageLookupGateway = mockk()
    private val nomClient: NomClient = mockk()

    private val saksbehandlerAccessService = SaksbehandlerAccessService(
        saksbehandlerAccessRepository = saksbehandlerAccessRepository,
        innstillingerService = innstillingerService,
        klageLookupGateway = klageLookupGateway,
        nomClient = nomClient,
        tokenUtil = mockk(),
    )

    private val ident = "ident"
    private val sammensattNavn = "sammensattNavn"

    @Nested
    inner class GetSaksbehandlerAccessView {
        @Test
        fun `does not exists, verifies saksbehandlerName`() {
            every { saksbehandlerAccessRepository.existsById(any()) } returns false
            every { klageLookupGateway.getUserInfoForGivenNavIdent(any()) } returns SaksbehandlerPersonligInfo(
                navIdent = "",
                fornavn = "",
                etternavn = "",
                sammensattNavn = sammensattNavn,
                enhet = SaksbehandlerEnhet(enhetId = "", navn = "")
            )

            assertEquals(
                emptyList<String>(),
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).ytelseIdList
            )
            assertEquals(
                ident,
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).saksbehandlerIdent
            )
            assertEquals(
                sammensattNavn,
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).saksbehandlerName
            )
        }
    }
}