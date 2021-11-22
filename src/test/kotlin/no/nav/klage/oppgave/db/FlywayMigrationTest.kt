package no.nav.klage.oppgave.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.ResultSet
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Id


@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    data class MiniValgtEnhet(val saksbehandlerident: String, val enhetId: String)

    @Test
    fun flyway_should_run() {
        val valgteEnheter: List<MiniValgtEnhet> = jdbcTemplate.query(
            "SELECT * FROM innstillinger.valgt_enhet"
        ) { rs: ResultSet, _: Int ->
            MiniValgtEnhet(
                saksbehandlerident = rs.getString("saksbehandlerident"),
                enhetId = rs.getString("enhet_id")
            )
        }

        assertThat(valgteEnheter).hasSize(0)
    }

}
