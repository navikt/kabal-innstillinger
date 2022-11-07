package no.nav.klage.oppgave.repositories

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
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
class SaksbehandlerAccessRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var saksbehandlerAccessRepository: SaksbehandlerAccessRepository

    @Test
    fun `persist SaksbehandlerAccess works`() {
        val saksbehandlerident = "AB12345"
        val ytelser = setOf(Ytelse.AAP_AAP, Ytelse.SYK_SYK)
        val saksbehandlerAccess = SaksbehandlerAccess(
            saksbehandlerident = saksbehandlerident,
            ytelser = ytelser,
        )

        saksbehandlerAccessRepository.save(saksbehandlerAccess)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(saksbehandlerAccessRepository.findById(saksbehandlerident).get().ytelser).isEqualTo(ytelser)
    }
}