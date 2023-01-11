package no.nav.klage.oppgave.domain.saksbehandler.entities

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "innstillinger", schema = "innstillinger")
class Innstillinger(

    @Id
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String,
    @Column(name = "hjemler")
    val hjemler: String = "",
    @Column(name = "ytelser")
    val ytelser: String = "",
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

        private val separator = ","
    }

    fun toSaksbehandlerInnstillinger(ansattEnhetForInnloggetSaksbehandler: EnhetMedLovligeYtelser): SaksbehandlerInnstillinger {
        return SaksbehandlerInnstillinger(
            hjemler = hjemler.split(separator).filterNot { it.isBlank() }.map { Hjemmel.of(it) },
            ytelser = ytelser.split(separator).filterNot { it.isBlank() }.map { Ytelse.of(it) }
                .filter { it in ansattEnhetForInnloggetSaksbehandler.ytelser },
            typer = typer.split(separator).filterNot { it.isBlank() }.map { Type.of(it) },
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