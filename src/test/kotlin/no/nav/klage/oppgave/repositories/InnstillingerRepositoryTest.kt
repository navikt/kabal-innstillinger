package no.nav.klage.oppgave.repositories


import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.saksbehandler.entities.Innstillinger
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
            hjemler = setOf(Hjemmel.of("FVL_16"), Hjemmel.of("FS_TILL_ST_15_2")),
            ytelser = setOf(Ytelse.of("3"), Ytelse.of("4")),
            shortName = "shortName",
            longName = "longName",
            jobTitle = "myTitle",
            anonymous = false,
        )

        innstillingerRepository.save(innstillinger)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(innstillingerRepository.findById(navIdent).get()).isEqualTo(innstillinger)
    }

}
