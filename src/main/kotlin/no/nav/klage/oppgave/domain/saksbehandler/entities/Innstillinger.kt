package no.nav.klage.oppgave.domain.saksbehandler.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import java.time.LocalDateTime

@Entity
@Table(name = "innstillinger", schema = "innstillinger")
class Innstillinger(

    @Id
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String,
    @Column(name = "hjemler")
    var hjemler: String = "",
    @Column(name = "ytelser")
    var ytelser: String = "",
    @Column(name = "typer")
    val typer: String = "",
    @Column(name = "short_name")
    var shortName: String? = null,
    @Column(name = "long_name")
    var longName: String? = null,
    @Column(name = "job_title")
    var jobTitle: String? = null,
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()

        const val SEPARATOR = ","
    }

    fun toSaksbehandlerInnstillinger(): SaksbehandlerInnstillinger {
        return SaksbehandlerInnstillinger(
            hjemler = hjemler.split(SEPARATOR).filterNot { it.isBlank() }.map { Hjemmel.of(it) },
            ytelser = ytelser.split(SEPARATOR).filterNot { it.isBlank() }.map { Ytelse.of(it) },
            typer = typer.split(SEPARATOR).filterNot { it.isBlank() }.map { Type.of(it) },
            shortName = shortName,
            longName = longName,
            jobTitle = jobTitle,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Innstillinger

        if (saksbehandlerident != other.saksbehandlerident) return false

        return true
    }

    override fun hashCode(): Int {
        return saksbehandlerident.hashCode()
    }

    override fun toString(): String {
        return "Innstillinger(saksbehandlerident='$saksbehandlerident', hjemler='$hjemler', ytelser='$ytelser', typer='$typer', shortName='$shortName', longName='$longName', jobTitle='$jobTitle', modified=$modified)"
    }

}