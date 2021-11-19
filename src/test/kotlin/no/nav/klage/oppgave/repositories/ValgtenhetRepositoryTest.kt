package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.saksbehandler.ValgtEnhet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ValgtenhetRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var valgtEnhetRepository: ValgtEnhetRepository

    @Test
    fun `persist valgtEnhet works`() {
        val valgtEnhet = ValgtEnhet(
            saksbehandlerident = "AB12345",
            enhetId = "4250",
            enhetNavn = "NAV Klageinstans Sør",
            tidspunkt = LocalDateTime.now()
        )

        valgtEnhetRepository.save(valgtEnhet)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(valgtEnhetRepository.findById(valgtEnhet.saksbehandlerident).get().enhetId).isEqualTo("4250")
    }

    @Test
    fun `updating valgtEnhet works`() {
        val valgtEnhet = ValgtEnhet(
            saksbehandlerident = "AB12345",
            enhetId = "4250",
            enhetNavn = "NAV Klageinstans Sør",
            tidspunkt = LocalDateTime.now()
        )

        val valgtEnhet2 = ValgtEnhet(
            saksbehandlerident = "AB12345",
            enhetId = "4295",
            enhetNavn = "NAV Klageinstans Nord",
            tidspunkt = LocalDateTime.now()
        )

        valgtEnhetRepository.save(valgtEnhet)

        testEntityManager.flush()
        testEntityManager.clear()

        valgtEnhetRepository.save(valgtEnhet2)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(valgtEnhetRepository.findById(valgtEnhet.saksbehandlerident).get().enhetId).isEqualTo("4295")
    }

}
