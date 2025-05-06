package db.migration

import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

class V14__migrate_innstillinger : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val preparedHjemmelStatement = context.connection.prepareStatement(
            """
                INSERT INTO innstillinger.innstillinger_hjemmel (id, innstillinger_saksbehandlerident)
                    VALUES(?, ?)
            """.trimIndent()
        )
        val preparedYtelseStatement = context.connection.prepareStatement(
            """
                INSERT INTO innstillinger.innstillinger_ytelse (id, innstillinger_saksbehandlerident)
                    VALUES(?, ?)
            """.trimIndent()
        )

        context.connection.createStatement().use { select ->
            select.executeQuery(
                """
                    select innstillinger.saksbehandlerident, innstillinger.hjemler, innstillinger.ytelser
                    from innstillinger.innstillinger
                    """
            )
                .use { rows ->
                    while (rows.next()) {
                        val saksbehandlerIdent = rows.getString(1)
                        val hjemler = rows.getString(2).split(",").filterNot { it.isBlank() }.map { Hjemmel.of(it) }
                        val ytelser = rows.getString(3).split(",").filterNot { it.isBlank() }.map { Ytelse.of(it) }
                        hjemler.forEach { hjemmel ->
                            preparedHjemmelStatement.setString(1, hjemmel.id)
                            preparedHjemmelStatement.setString(2, saksbehandlerIdent)
                            preparedHjemmelStatement.executeUpdate()
                        }
                        ytelser.forEach { ytelse ->
                            preparedYtelseStatement.setString(1, ytelse.id)
                            preparedYtelseStatement.setString(2, saksbehandlerIdent)
                            preparedYtelseStatement.executeUpdate()
                        }
                    }
                }
        }
    }
}