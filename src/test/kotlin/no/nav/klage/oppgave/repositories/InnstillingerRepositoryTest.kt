package no.nav.klage.oppgave.repositories


import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.Innstillinger
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InnstillingerRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance

        val ansattEnhetForInnloggetSaksbehandler = EnhetMedLovligeYtelser(
            enhet = Enhet("4291", "Nav Oslo"),
            ytelser = listOf(Ytelse.OMS_OLP, Ytelse.SYK_SYK)
        )
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var innstillingerRepository: InnstillingerRepository

    @Test
    fun `roundtrip in domain works`() {
        val navIdent = "AB12345"
        val saksbehandlersInnstillinger = SaksbehandlerInnstillinger(
            hjemler = listOf(Hjemmel.FTL, Hjemmel.FTRL_22_3),
            ytelser = emptyList(),
            typer = listOf(Type.KLAGE)
        )

        val roundtripValue = Innstillinger.fromSaksbehandlersInnstillinger(
            navIdent,
            ansattEnhetForInnloggetSaksbehandler,
            saksbehandlersInnstillinger
        )
            .toSaksbehandlerInnstillinger(ansattEnhetForInnloggetSaksbehandler)

        assertThat(roundtripValue).isEqualTo(saksbehandlersInnstillinger)
    }

    @Test
    fun `persist innstillinger works`() {
        val navIdent = "AB12345"
        val saksbehandlersInnstillinger = SaksbehandlerInnstillinger(
            hjemler = listOf(Hjemmel.FTL, Hjemmel.FTRL_22_3),
            ytelser = emptyList(),
            typer = listOf(Type.KLAGE)
        )
        val innstillinger = Innstillinger.fromSaksbehandlersInnstillinger(
            navIdent,
            ansattEnhetForInnloggetSaksbehandler,
            saksbehandlersInnstillinger
        )

        innstillingerRepository.save(innstillinger)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(innstillingerRepository.findById(navIdent).get()).isEqualTo(innstillinger)
    }

    @Test
    fun `updating valgtEnhet works`() {
        val navIdent = "AB12345"
        val saksbehandlersInnstillinger1 = SaksbehandlerInnstillinger(
            hjemler = listOf(Hjemmel.FTL, Hjemmel.FTRL_22_3),
            ytelser = emptyList(),
            typer = listOf(Type.KLAGE)
        )
        val innstillinger1 = Innstillinger.fromSaksbehandlersInnstillinger(
            navIdent,
            ansattEnhetForInnloggetSaksbehandler,
            saksbehandlersInnstillinger1
        )

        val saksbehandlersInnstillinger2 = SaksbehandlerInnstillinger(
            hjemler = listOf(Hjemmel.FTL),
            ytelser = listOf(Ytelse.OMS_OLP, Ytelse.SYK_SYK),
            typer = listOf(Type.ANKE)
        )
        val innstillinger2 = Innstillinger.fromSaksbehandlersInnstillinger(
            navIdent,
            ansattEnhetForInnloggetSaksbehandler,
            saksbehandlersInnstillinger2
        )

        innstillingerRepository.save(innstillinger1)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(innstillingerRepository.findById(navIdent).get()).isEqualTo(innstillinger1)

        testEntityManager.flush()
        testEntityManager.clear()

        innstillingerRepository.save(innstillinger2)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(innstillingerRepository.findById(navIdent).get()).isEqualTo(innstillinger2)
    }

}
