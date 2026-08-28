package no.nav.klage.oppgave.repositories

import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.db.PostgresIntegrationTestBase
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
@DataJpaTest
class SaksbehandlerAccessRepositoryTest : PostgresIntegrationTestBase() {
    val saksbehandlerIdent1 = "SAKSBEHANDLER_IDENT_1"
    val saksbehandlerIdent2 = "SAKSBEHANDLER_IDENT_2"
    val saksbehandlerIdent3 = "SAKSBEHANDLER_IDENT_3"

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var saksbehandlerAccessRepository: SaksbehandlerAccessRepository

    @Test
    fun `persist SaksbehandlerAccess works`() {
        val saksbehandlerident = saksbehandlerIdent1
        val innloggetIdent = saksbehandlerIdent2
        val ytelser = setOf(Ytelse.AAP_AAP, Ytelse.SYK_SYK)
        val saksbehandlerAccess =
            SaksbehandlerAccess(
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
        val saksbehandlerAccess1 =
            SaksbehandlerAccess(
                saksbehandlerIdent = saksbehandlerIdent1,
                modifiedBy = saksbehandlerIdent3,
                ytelser = ytelser1,
            )

        val ytelser2 = setOf(Ytelse.SYK_SYK, Ytelse.BAR_BAR)
        val saksbehandlerAccess2 =
            SaksbehandlerAccess(
                saksbehandlerIdent = saksbehandlerIdent2,
                modifiedBy = saksbehandlerIdent3,
                ytelser = ytelser2,
            )

        val ytelser3 = setOf(Ytelse.OMS_OLP)
        val saksbehandlerAccess3 =
            SaksbehandlerAccess(
                saksbehandlerIdent = saksbehandlerIdent3,
                modifiedBy = saksbehandlerIdent3,
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
