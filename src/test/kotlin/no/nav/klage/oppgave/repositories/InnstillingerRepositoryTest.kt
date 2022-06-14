package no.nav.klage.oppgave.repositories


import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.Innstillinger
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
    fun `persist innstillinger works`() {
        val navIdent = "AB12345"
        val innstillinger = Innstillinger(
            saksbehandlerident = navIdent,
            hjemler = "1,12",
            ytelser = "3,4",
            typer = "1",
            shortName = "shortName",
            longName = "longName",
            jobTitle = "myTitle",
        )

        innstillingerRepository.save(innstillinger)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(innstillingerRepository.findById(navIdent).get()).isEqualTo(innstillinger)
    }

}
