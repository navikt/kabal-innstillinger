package no.nav.klage.oppgave.service

import io.mockk.*
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseToHjemler
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
    private val hjemmel3 = Hjemmel.FTRL_10_3
    private val hjemmel4 = Hjemmel.FTRL_10_4
    private val ytelse1 = Ytelse.HEL_HEL
    private val ytelse2 = Ytelse.BIL_BIL
    private val ytelse3 = Ytelse.SYK_SYK
    private val shortName = "shortName"
    private val longName = "longName"
    private val jobTitle = "jobTitle"

    private val saksbehandlerInnstillingerInput = SaksbehandlerInnstillinger(
        hjemler = setOf(hjemmel1, hjemmel2),
        ytelser = setOf(ytelse1, ytelse2),
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
                ytelser = setOf(Ytelse.of("1")),
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
                    assignedYtelseSet = setOf(ytelse1, ytelse2)
                )
            )
        }

        @Test
        fun `illegal ytelser in input are ignored`() {
            every { innstillingerRepository.findBySaksbehandlerident(ident1) }.returns(null)

            assertEquals(
                /* expected = */ listOf(ytelse1).map { it.id },
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseSet = setOf(ytelse1)
                ).ytelser.map { it.id }
            )
        }

        @Test
        fun `signature input ignored`() {

            val oldInnstillinger = Innstillinger(
                saksbehandlerident = ident1,
                hjemler = setOf(hjemmel1, hjemmel2),
                ytelser = setOf(ytelse1, ytelse2),
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
                    assignedYtelseSet = setOf(ytelse1, ytelse2)
                ).shortName
            )

            assertEquals(
                /* expected = */ longName,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseSet = setOf(ytelse1, ytelse2)
                ).longName
            )

            assertEquals(
                /* expected = */ jobTitle,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseSet = setOf(ytelse1, ytelse2)
                ).jobTitle
            )

            assertEquals(
                /* expected = */ true,
                /* actual = */ innstillingerService.storeInnstillingerButKeepSignature(
                    navIdent = ident1,
                    newSaksbehandlerInnstillinger = saksbehandlerInnstillingerInput,
                    assignedYtelseSet = setOf(ytelse1, ytelse2)
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
                assignedYtelseSet = setOf(ytelse1),
            )

            verify {
                innstillingerRepository.save(
                    Innstillinger(
                        saksbehandlerident = ident1,
                        hjemler = ytelseToHjemler[ytelse1]!!.toSet(),
                        ytelser = setOf(ytelse1),
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
            val existingYtelse2Hjemler = ytelseToHjemler[ytelse2]!!.subList(0, 3)
            val extraExistingHjemler = setOf(ytelseToHjemler[ytelse3]!![0])

            val existingHjemler = existingYtelse2Hjemler + extraExistingHjemler

            val mockInnstillinger = spyk(
                Innstillinger(
                    saksbehandlerident = ident1,
                    hjemler = existingHjemler.toSet(),
                    ytelser = setOf(ytelse2, ytelse3),
                    shortName = null,
                    longName = null,
                    jobTitle = null,
                    modified = now,
                    anonymous = false,
                ),
                recordPrivateCalls = true,
            )

            every { mockInnstillinger.ytelser = any() } returnsArgument 0
            every { mockInnstillinger.hjemler = any() } returnsArgument 0
            every { mockInnstillinger.modified = now } returnsArgument 0

            every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(ident1) }.returns(
                setOf(
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
                assignedYtelseSet = setOf(ytelse1, ytelse2),

                )

            verify {
                mockInnstillinger setProperty "ytelser" value setOf(
                    ytelse1,
                    ytelse2,
                )

                mockInnstillinger setProperty "hjemler" value (ytelseToHjemler[ytelse1]!! + existingYtelse2Hjemler).toSet()
            }
        }
    }

    @Test
    fun `add hjemler for ytelse`() {
        val mockInnstillinger1 = spyk(
            Innstillinger(
                saksbehandlerident = ident1,
                hjemler = setOf(hjemmel1),
                ytelser = setOf(ytelse2, ytelse3),
                shortName = null,
                longName = null,
                jobTitle = null,
                modified = now,
                anonymous = false,
            ),
            recordPrivateCalls = true,
        )

        val mockInnstillinger2 = spyk(
            Innstillinger(
                saksbehandlerident = ident1,
                hjemler = setOf(hjemmel2),
                ytelser = setOf(ytelse3),
                shortName = null,
                longName = null,
                jobTitle = null,
                modified = now,
                anonymous = false,
            ),
            recordPrivateCalls = true,
        )

        every { innstillingerRepository.findAll() }.returns(
            listOf(mockInnstillinger1, mockInnstillinger2)
        )

        innstillingerService.addHjemlerForYtelse(
            ytelse = ytelse2,
            hjemmelList = listOf(hjemmel3, hjemmel4)
        )

        verify {
            mockInnstillinger1 setProperty "hjemler" value setOf(hjemmel1, hjemmel3, hjemmel4)
        }

        //mockInnstillinger2 does not have ytelse in input, skipped in update.
        verify (exactly = 0) {
            mockInnstillinger2 setProperty "hjemler" value setOf(hjemmel1, hjemmel3, hjemmel4)
        }
    }

    @Test
    fun `attempt at adding existing hjemler, no change`() {
        val mockInnstillinger = spyk(
            Innstillinger(
                saksbehandlerident = ident1,
                hjemler = setOf(hjemmel1, hjemmel3),
                ytelser = setOf(ytelse2, ytelse3),
                shortName = null,
                longName = null,
                jobTitle = null,
                modified = now,
                anonymous = false,
            ),
            recordPrivateCalls = true,
        )

        every { innstillingerRepository.findAll() }.returns(
            listOf(mockInnstillinger)
        )

        innstillingerService.addHjemlerForYtelse(
            ytelse = ytelse2,
            hjemmelList = listOf(hjemmel3)
        )

        verify(exactly = 0) {
            mockInnstillinger setProperty "hjemler" value setOf(hjemmel1, hjemmel3)
        }
    }
}