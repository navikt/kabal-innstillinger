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

    val SAKSBEHANDLER_IDENT_1 = "SAKSBEHANDLER_IDENT_1"
    val SAKSBEHANDLER_IDENT_2 = "SAKSBEHANDLER_IDENT_2"
    val SAKSBEHANDLER_IDENT_3 = "SAKSBEHANDLER_IDENT_3"

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
        val saksbehandlerident = SAKSBEHANDLER_IDENT_1
        val innloggetIdent = SAKSBEHANDLER_IDENT_2
        val ytelser = setOf(Ytelse.AAP_AAP, Ytelse.SYK_SYK)
        val saksbehandlerAccess = SaksbehandlerAccess(
            saksbehandlerIdent = saksbehandlerident,
            modifiedBy = innloggetIdent,
            ytelser = ytelser,
        )

        saksbehandlerAccessRepository.save(saksbehandlerAccess)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(saksbehandlerAccessRepository.findById(saksbehandlerident).get().ytelser).isEqualTo(ytelser)
    }

    @Test
    fun `findByYtelser functionality`() {
        val ytelser1 = setOf(Ytelse.AAP_AAP, Ytelse.SYK_SYK)
        val saksbehandlerAccess1 = SaksbehandlerAccess(
            saksbehandlerIdent = SAKSBEHANDLER_IDENT_1,
            modifiedBy = SAKSBEHANDLER_IDENT_3,
            ytelser = ytelser1,
        )

        val ytelser2 = setOf(Ytelse.SYK_SYK, Ytelse.BAR_BAR)
        val saksbehandlerAccess2 = SaksbehandlerAccess(
            saksbehandlerIdent = SAKSBEHANDLER_IDENT_2,
            modifiedBy = SAKSBEHANDLER_IDENT_3,
            ytelser = ytelser2,
        )

        val ytelser3 = setOf(Ytelse.OMS_OLP)
        val saksbehandlerAccess3 = SaksbehandlerAccess(
            saksbehandlerIdent = SAKSBEHANDLER_IDENT_3,
            modifiedBy = SAKSBEHANDLER_IDENT_3,
            ytelser = ytelser3,
        )

        saksbehandlerAccessRepository.save(saksbehandlerAccess1)
        saksbehandlerAccessRepository.save(saksbehandlerAccess2)
        saksbehandlerAccessRepository.save(saksbehandlerAccess3)
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(saksbehandlerAccessRepository.findAllByYtelserContaining(Ytelse.SYK_SYK).size).isEqualTo(2)
        assertThat(saksbehandlerAccessRepository.findAllByYtelserContaining(Ytelse.OMS_OLP).size).isEqualTo(1)
        assertThat(saksbehandlerAccessRepository.findAllByYtelserContaining(Ytelse.OMS_PLS).size).isEqualTo(0)
    }


}