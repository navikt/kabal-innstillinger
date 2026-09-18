package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.klage.kodeverk.Enhet
import no.nav.klage.kodeverk.klageenheter
import no.nav.klage.kodeverk.styringsenheter
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.view.AccessInput
import no.nav.klage.oppgave.api.view.AnketeamInput
import no.nav.klage.oppgave.api.view.AnketeamMember
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerEnhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerSluttdato
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SaksbehandlerAccessServiceTest {
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository = mockk()
    private val innstillingerService: InnstillingerService = mockk()
    private val klageLookupGateway: KlageLookupGateway = mockk()

    private val saksbehandlerAccessService =
        SaksbehandlerAccessService(
            saksbehandlerAccessRepository = saksbehandlerAccessRepository,
            innstillingerService = innstillingerService,
            klageLookupGateway = klageLookupGateway,
        )

    private val ident = "ident"
    private val sammensattNavn = "sammensattNavn"

    @Nested
    inner class GetSaksbehandlerAccessView {
        @Test
        fun `does not exists, verifies saksbehandlerName`() {
            every { saksbehandlerAccessRepository.existsById(any()) } returns false
            every { klageLookupGateway.getUserInfoForGivenNavIdent(any()) } returns
                SaksbehandlerPersonligInfo(
                    navIdent = "",
                    fornavn = "",
                    etternavn = "",
                    sammensattNavn = sammensattNavn,
                    enhet = SaksbehandlerEnhet(enhetId = "", navn = ""),
                )

            assertEquals(
                emptyList<String>(),
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).ytelseIdList,
            )
            assertEquals(
                ident,
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).saksbehandlerIdent,
            )
            assertEquals(
                sammensattNavn,
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).saksbehandlerName,
            )
        }
    }

    @Nested
    inner class DeleteInnstillingerAndAccessForExpiredSaksbehandlers {
        @Test
        fun `deletes access and innstillinger when ansatt is expired`() {
            every { saksbehandlerAccessRepository.findAll() } returns
                listOf(
                    SaksbehandlerAccess(
                        saksbehandlerIdent = ident,
                        modifiedBy = "admin",
                        ytelser = emptySet(),
                        created = LocalDateTime.now(),
                        accessRightsModified = LocalDateTime.now(),
                        anketeam = false,
                    ),
                )
            every { klageLookupGateway.getSluttdatoForNavIdentList(listOf(ident)) } returns
                listOf(
                    SaksbehandlerSluttdato(
                        navIdent = ident,
                        sluttdato = LocalDate.now().minusWeeks(2),
                    ),
                )
            every { klageLookupGateway.getUserInfoForNavIdentList(listOf(ident)) } returns emptyList()
            every { saksbehandlerAccessRepository.deleteById(ident) } returns Unit
            every { innstillingerService.deleteInnstillingerForSaksbehandler(ident) } returns "deleted\n"

            saksbehandlerAccessService.deleteInnstillingerAndAccessForExpiredSaksbehandlers()

            verify(exactly = 1) { klageLookupGateway.getSluttdatoForNavIdentList(listOf(ident)) }
            verify(exactly = 1) { klageLookupGateway.getUserInfoForNavIdentList(listOf(ident)) }
            verify(exactly = 1) { saksbehandlerAccessRepository.deleteById(ident) }
            verify(exactly = 1) { innstillingerService.deleteInnstillingerForSaksbehandler(ident) }
            verify(exactly = 0) { klageLookupGateway.getGroupsForGivenNavIdent(any()) }
            verify(exactly = 0) { klageLookupGateway.getSluttdatoForGivenNavIdent(any()) }
        }

        @Test
        fun `deletes access and innstillinger when ansatt is not in klageenhet`() {
            val enhetOutsideKlageAndStyring = Enhet.entries.first { it !in (klageenheter + styringsenheter) }

            every { saksbehandlerAccessRepository.findAll() } returns
                listOf(
                    SaksbehandlerAccess(
                        saksbehandlerIdent = ident,
                        modifiedBy = "admin",
                        ytelser = emptySet(),
                        created = LocalDateTime.now(),
                        accessRightsModified = LocalDateTime.now(),
                        anketeam = false,
                    ),
                )

            every { klageLookupGateway.getSluttdatoForNavIdentList(listOf(ident)) } returns
                listOf(
                    SaksbehandlerSluttdato(
                        navIdent = ident,
                        sluttdato = LocalDate.now().plusDays(1),
                    ),
                )

            every { klageLookupGateway.getUserInfoForNavIdentList(listOf(ident)) } returns
                listOf(
                    SaksbehandlerPersonligInfo(
                        navIdent = ident,
                        fornavn = "fornavn",
                        etternavn = "etternavn",
                        sammensattNavn = "fornavn etternavn",
                        enhet =
                            SaksbehandlerEnhet(
                                enhetId = enhetOutsideKlageAndStyring.navn,
                                navn = enhetOutsideKlageAndStyring.beskrivelse,
                            ),
                    ),
                )
            every { saksbehandlerAccessRepository.deleteById(ident) } returns Unit
            every { innstillingerService.deleteInnstillingerForSaksbehandler(ident) } returns "deleted\n"

            saksbehandlerAccessService.deleteInnstillingerAndAccessForExpiredSaksbehandlers()

            verify(exactly = 1) { klageLookupGateway.getSluttdatoForNavIdentList(listOf(ident)) }
            verify(exactly = 1) { klageLookupGateway.getUserInfoForNavIdentList(listOf(ident)) }
            verify(exactly = 1) { saksbehandlerAccessRepository.deleteById(ident) }
            verify(exactly = 1) { innstillingerService.deleteInnstillingerForSaksbehandler(ident) }
            verify(exactly = 0) { klageLookupGateway.getUserInfoForGivenNavIdent(any()) }
        }
    }

    @Nested
    inner class SetAnketeamForAnsatt {
        @Test
        fun `oppretter access med anketeam naar saksbehandler mangler access fra foer`() {
            val savedAccess = slot<SaksbehandlerAccess>()
            every { saksbehandlerAccessRepository.existsById(ident) } returns false
            every { saksbehandlerAccessRepository.save(capture(savedAccess)) } answers { savedAccess.captured }

            val result =
                saksbehandlerAccessService.setAnketeamForAnsatt(
                    anketeamInput =
                        AnketeamInput(
                            anketeam = listOf(AnketeamInput.AnketeamMemberInput(saksbehandlerIdent = ident, anketeam = true)),
                        ),
                    innloggetAnsattIdent = "leder",
                )

            assertEquals(listOf(AnketeamMember(saksbehandlerIdent = ident, anketeam = true)), result.anketeam)
            assertEquals(emptySet<Ytelse>(), savedAccess.captured.ytelser)
            assertEquals("leder", savedAccess.captured.modifiedBy)
        }

        @Test
        fun `oppdaterer anketeam og sporingsfelter paa eksisterende access`() {
            val existingModified = LocalDateTime.now().minusDays(1)
            val existingAccess =
                SaksbehandlerAccess(
                    saksbehandlerIdent = ident,
                    modifiedBy = "tidligere leder",
                    ytelser = setOf(Ytelse.AAP_AAP),
                    anketeam = false,
                    created = existingModified,
                    accessRightsModified = existingModified,
                )
            every { saksbehandlerAccessRepository.existsById(ident) } returns true
            every { saksbehandlerAccessRepository.getReferenceById(ident) } returns existingAccess

            val result =
                saksbehandlerAccessService.setAnketeamForAnsatt(
                    anketeamInput =
                        AnketeamInput(
                            anketeam = listOf(AnketeamInput.AnketeamMemberInput(saksbehandlerIdent = ident, anketeam = true)),
                        ),
                    innloggetAnsattIdent = "leder",
                )

            assertEquals(listOf(AnketeamMember(saksbehandlerIdent = ident, anketeam = true)), result.anketeam)
            assertTrue(existingAccess.anketeam)
            assertEquals("leder", existingAccess.modifiedBy)
            assertTrue(existingAccess.accessRightsModified.isAfter(existingModified))
            assertEquals(setOf(Ytelse.AAP_AAP), existingAccess.ytelser)
            verify(exactly = 0) { saksbehandlerAccessRepository.save(any()) }
        }

        @Test
        fun `lar sporingsfelter staa naar anketeam er uendret`() {
            val existingModified = LocalDateTime.now().minusDays(1)
            val existingAccess =
                SaksbehandlerAccess(
                    saksbehandlerIdent = ident,
                    modifiedBy = "tidligere leder",
                    ytelser = setOf(Ytelse.AAP_AAP),
                    anketeam = true,
                    created = existingModified,
                    accessRightsModified = existingModified,
                )
            every { saksbehandlerAccessRepository.existsById(ident) } returns true
            every { saksbehandlerAccessRepository.getReferenceById(ident) } returns existingAccess

            saksbehandlerAccessService.setAnketeamForAnsatt(
                anketeamInput =
                    AnketeamInput(
                        anketeam = listOf(AnketeamInput.AnketeamMemberInput(saksbehandlerIdent = ident, anketeam = true)),
                    ),
                innloggetAnsattIdent = "leder",
            )

            assertEquals("tidligere leder", existingAccess.modifiedBy)
            assertEquals(existingModified, existingAccess.accessRightsModified)
        }
    }

    @Nested
    inner class SetYtelserForAnsatt {
        @Test
        fun `endrer ikke anketeam paa eksisterende access`() {
            val existingAccess =
                SaksbehandlerAccess(
                    saksbehandlerIdent = ident,
                    modifiedBy = "tidligere leder",
                    ytelser = emptySet(),
                    anketeam = true,
                    created = LocalDateTime.now().minusDays(1),
                    accessRightsModified = LocalDateTime.now().minusDays(1),
                )
            every { saksbehandlerAccessRepository.existsById(ident) } returns true
            every { saksbehandlerAccessRepository.getReferenceById(ident) } returns existingAccess
            every {
                innstillingerService.updateYtelseAndHjemmelInnstillinger(
                    navIdent = ident,
                    inputYtelseSet = any(),
                    assignedYtelseSet = any(),
                )
            } returns Unit
            every { klageLookupGateway.getUserInfoForGivenNavIdent(ident) } returns
                SaksbehandlerPersonligInfo(
                    navIdent = ident,
                    fornavn = "",
                    etternavn = "",
                    sammensattNavn = sammensattNavn,
                    enhet = SaksbehandlerEnhet(enhetId = "", navn = ""),
                )

            val result =
                saksbehandlerAccessService.setYtelserForAnsatt(
                    accessInput =
                        AccessInput(
                            accessRights =
                                listOf(
                                    AccessInput.AccessRightInput(
                                        saksbehandlerIdent = ident,
                                        ytelseIdList = listOf(Ytelse.AAP_AAP.id),
                                    ),
                                ),
                        ),
                    innloggetAnsattIdent = "leder",
                )

            assertTrue(existingAccess.anketeam)
            assertEquals(setOf(Ytelse.AAP_AAP), existingAccess.ytelser)
            assertTrue(result.accessRights.single().anketeam)
            assertEquals(listOf(Ytelse.AAP_AAP.id), result.accessRights.single().ytelseIdList)
        }
    }
}
