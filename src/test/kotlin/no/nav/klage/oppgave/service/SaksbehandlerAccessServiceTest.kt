package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.kodeverk.Enhet
import no.nav.klage.kodeverk.klageenheter
import no.nav.klage.kodeverk.styringsenheter
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.clients.nom.Ansatt
import no.nav.klage.oppgave.clients.nom.DataWrapper
import no.nav.klage.oppgave.clients.nom.GetAnsattResponse
import no.nav.klage.oppgave.clients.nom.NomClient
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerEnhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime


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

    @Nested
    inner class DeleteInnstillingerAndAccessForExpiredSaksbehandlers {
        @Test
        fun `deletes access and innstillinger when ansatt is expired`() {
            every { saksbehandlerAccessRepository.findAll() } returns listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = ident,
                    modifiedBy = "admin",
                    ytelser = emptySet(),
                    created = LocalDateTime.now(),
                    accessRightsModified = LocalDateTime.now(),
                )
            )
            every { nomClient.getAnsatt(ident) } returns GetAnsattResponse(
                data = DataWrapper(
                    ressurs = Ansatt(
                        navident = ident,
                        sluttdato = LocalDate.now().minusWeeks(2),
                    )
                )
            )
            every { saksbehandlerAccessRepository.deleteById(ident) } returns Unit
            every { innstillingerService.deleteInnstillingerForSaksbehandler(ident) } returns "deleted\n"

            saksbehandlerAccessService.deleteInnstillingerAndAccessForExpiredSaksbehandlers()

            verify(exactly = 1) { nomClient.getAnsatt(ident) }
            verify(exactly = 1) { saksbehandlerAccessRepository.deleteById(ident) }
            verify(exactly = 1) { innstillingerService.deleteInnstillingerForSaksbehandler(ident) }
            verify(exactly = 0) { klageLookupGateway.getGroupsForGivenNavIdent(any()) }
        }

        @Test
        fun `deletes access and innstillinger when ansatt is not in klageenhet`() {
            val enhetOutsideKlageAndStyring = Enhet.entries.first { it !in (klageenheter + styringsenheter) }

            every { saksbehandlerAccessRepository.findAll() } returns listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = ident,
                    modifiedBy = "admin",
                    ytelser = emptySet(),
                    created = LocalDateTime.now(),
                    accessRightsModified = LocalDateTime.now(),
                )
            )
            every { nomClient.getAnsatt(ident) } returns GetAnsattResponse(
                data = DataWrapper(
                    ressurs = Ansatt(
                        navident = ident,
                        sluttdato = LocalDate.now().plusDays(1),
                    )
                )
            )
            every { klageLookupGateway.getUserInfoForGivenNavIdent(ident) } returns SaksbehandlerPersonligInfo(
                navIdent = ident,
                fornavn = "fornavn",
                etternavn = "etternavn",
                sammensattNavn = "fornavn etternavn",
                enhet = SaksbehandlerEnhet(
                    enhetId = enhetOutsideKlageAndStyring.name,
                    navn = "enhet",
                )
            )
            every { saksbehandlerAccessRepository.deleteById(ident) } returns Unit
            every { innstillingerService.deleteInnstillingerForSaksbehandler(ident) } returns "deleted\n"

            saksbehandlerAccessService.deleteInnstillingerAndAccessForExpiredSaksbehandlers()

            verify(exactly = 1) { nomClient.getAnsatt(ident) }
            verify(exactly = 1) { klageLookupGateway.getUserInfoForGivenNavIdent(ident) }
            verify(exactly = 1) { saksbehandlerAccessRepository.deleteById(ident) }
            verify(exactly = 1) { innstillingerService.deleteInnstillingerForSaksbehandler(ident) }
        }
    }
}