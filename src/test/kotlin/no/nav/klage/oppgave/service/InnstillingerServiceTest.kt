package no.nav.klage.oppgave.service

import io.mockk.*
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseTilHjemler
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import no.nav.klage.oppgave.domain.saksbehandler.entities.Innstillinger
import no.nav.klage.oppgave.repositories.InnstillingerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class InnstillingerServiceTest {
    private val innstillingerRepository: InnstillingerRepository = spyk()
    private val saksbehandlerAccessService: SaksbehandlerAccessService = mockk()
    private val innstillingerService = InnstillingerService(
        innstillingerRepository = innstillingerRepository,
    )

    private val ident1 = "ident1"
    private val ident2 = "ident2"
    private val hjemmel1 = Hjemmel.FTRL_8_1
    private val hjemmel2 = Hjemmel.FTRL_8_2
    private val ytelse1 = Ytelse.HEL_HEL
    private val ytelse2 = Ytelse.BIL_BIL
    private val ytelse3 = Ytelse.SYK_SYK
    private val shortName = "shortName"
    private val longName = "longName"
    private val jobTitle = "jobTitle"

    private val saksbehandlerInnstillingerInput = SaksbehandlerInnstillinger(
        hjemler = listOf(hjemmel1, hjemmel2),
        ytelser = listOf(ytelse1, ytelse2),
        shortName = null,
        longName = null,
        jobTitle = null,
        anonymous = false,
    )

    private val SEPARATOR = ","
    private val now = LocalDateTime.now()

    @BeforeEach
    fun before() {

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns now
        every { innstillingerRepository.save(any()) }.returnsArgument(0)
    }


    @Test
    fun findSaksbehandlerInnstillinger() {
        every { innstillingerRepository.findBySaksbehandlerident(ident1) }.returns(
            Innstillinger(
                saksbehandlerident = ident1,
                ytelser = "1",
                anonymous = false,
            )
        )
        every { innstillingerRepository.findBySaksbehandlerident(ident2) }.returns(null)
        assertNotEquals(
            /* unexpected = */ innstillingerService.findSaksbehandlerInnstillinger(ident = ident1),
            /* actual = */ innstillingerService.findSaksbehandlerInnstillinger(ident = ident2)
        )
    }

    @Nested
    inner class StoreInnstillingerButKeepSignature {

        @Test
        fun `happy path`() {
            every { innstillingerRepository.findBySaksbehandlerident(ident1) }.returns(null)

            assertEquals(
                /* expected = */ saksbehandlerInnstillingerInput,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseList = listOf(ytelse1, ytelse2)
                )
            )
        }

        @Test
        fun `illegal ytelser in input are ignored`() {
            every { innstillingerRepository.findBySaksbehandlerident(ident1) }.returns(null)

            assertEquals(
                /* expected = */ listOf(ytelse1),
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseList = listOf(ytelse1)
                ).ytelser
            )
        }

        @Test
        fun `signature input ignored`() {

            val oldInnstillinger = Innstillinger(
                saksbehandlerident = ident1,
                hjemler = listOf(hjemmel1, hjemmel2).joinToString(SEPARATOR) { it.id },
                ytelser = listOf(ytelse1, ytelse2).joinToString(SEPARATOR) { it.id },
                shortName = shortName,
                longName = longName,
                jobTitle = jobTitle,
                modified = now,
                anonymous = true,
            )

            every { innstillingerRepository.findBySaksbehandlerident(ident1) }.returns(
                oldInnstillinger
            )

            assertEquals(
                /* expected = */ shortName,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseList = listOf(ytelse1, ytelse2)
                ).shortName
            )

            assertEquals(
                /* expected = */ longName,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseList = listOf(ytelse1, ytelse2)
                ).longName
            )

            assertEquals(
                /* expected = */ jobTitle,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseList = listOf(ytelse1, ytelse2)
                ).jobTitle
            )

            assertEquals(
                /* expected = */ true,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseList = listOf(ytelse1, ytelse2)
                ).anonymous
            )
        }
    }

    @Nested
    inner class UpdateYtelseAndHjemmelInnstillinger {
        @Test
        fun `new Innstillinger, saves all hjemler from ytelse, ignores illegal ytelse in input`() {
            every { innstillingerRepository.existsById(ident1) }.returns(false)

            innstillingerService.updateYtelseAndHjemmelInnstillinger(
                navIdent = ident1,
                inputYtelseSet = setOf(ytelse1, ytelse2),
                assignedYtelseList = listOf(ytelse1),
            )

            verify {
                innstillingerRepository.save(
                    Innstillinger(
                        saksbehandlerident = ident1,
                        hjemler = ytelseTilHjemler[ytelse1]!!.joinToString(SEPARATOR) { it.id },
                        ytelser = listOf(ytelse1).joinToString(SEPARATOR) { it.id },
                        shortName = null,
                        longName = null,
                        jobTitle = null,
                        modified = now,
                        anonymous = false,
                    )
                )
            }
        }

        @Test
        fun `existing Innstillinger, saves all hjemler from new ytelse, keeps existing ytelse and hjemler, removes existing hjemler and ytelser no longer legal`() {
            val existingYtelse2Hjemler = ytelseTilHjemler[ytelse2]!!.subList(0, 3)
            val extraExistingHjemler = listOf(ytelseTilHjemler[ytelse3]!![0])

            val existingHjemler = existingYtelse2Hjemler + extraExistingHjemler

            val mockInnstillinger = spyk(
                Innstillinger(
                    saksbehandlerident = ident1,
                    hjemler = existingHjemler.joinToString(SEPARATOR) { it.id },
                    ytelser = listOf(ytelse2, ytelse3).joinToString(SEPARATOR) { it.id },
                    shortName = null,
                    longName = null,
                    jobTitle = null,
                    modified = now,
                    anonymous = false,
                ), recordPrivateCalls = true,
            )

            every { mockInnstillinger.ytelser = any() } returnsArgument 0
            every { mockInnstillinger.hjemler = any() } returnsArgument 0
            every { mockInnstillinger.modified = now } returnsArgument 0

            every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseList(ident1) }.returns(
                listOf(
                    ytelse1,
                    ytelse2,
                )
            )
            every { innstillingerRepository.existsById(ident1) }.returns(true)
            every { innstillingerRepository.findBySaksbehandlerident(ident1) }.returns(
                mockInnstillinger
            )
            every { innstillingerRepository.getReferenceById(ident1) }.returns(
                mockInnstillinger
            )

            innstillingerService.updateYtelseAndHjemmelInnstillinger(
                navIdent = ident1,
                inputYtelseSet = setOf(ytelse1, ytelse2),
                assignedYtelseList = listOf(ytelse1, ytelse2),

            )

            verify {
                mockInnstillinger setProperty "ytelser" value listOf(
                        ytelse1,
                        ytelse2,
                    ).joinToString(SEPARATOR) { it.id }

                mockInnstillinger setProperty "hjemler" value (ytelseTilHjemler[ytelse1]!! + existingYtelse2Hjemler).toSet()
                    .joinToString(SEPARATOR) { it.id }
            }
        }
    }
}