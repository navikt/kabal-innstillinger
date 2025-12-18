package no.nav.klage.oppgave.repositories


import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.db.PostgresIntegrationTestBase
import no.nav.klage.oppgave.domain.saksbehandler.entities.Innstillinger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
@DataJpaTest
class InnstillingerRepositoryTest: PostgresIntegrationTestBase() {
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
