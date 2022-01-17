package no.nav.klage.oppgave.domain.saksbehandler

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
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
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()

        private val separator = ","

        fun fromSaksbehandlersInnstillinger(
            navIdent: String,
            ansattEnhetForInnloggetSaksbehandler: EnhetMedLovligeYtelser,
            saksbehandlerInnstillinger: SaksbehandlerInnstillinger
        ): Innstillinger {
            return Innstillinger(
                navIdent,
                saksbehandlerInnstillinger.hjemler.map { it.id }.joinToString(separator),
                saksbehandlerInnstillinger.ytelser.filter { it in ansattEnhetForInnloggetSaksbehandler.ytelser }
                    .map { it.id }.joinToString(separator),
                saksbehandlerInnstillinger.typer.map { it.id }.joinToString(separator),
                LocalDateTime.now()
            )
        }
    }

    fun toSaksbehandlerInnstillinger(ansattEnhetForInnloggetSaksbehandler: EnhetMedLovligeYtelser): SaksbehandlerInnstillinger {
        return SaksbehandlerInnstillinger(
            hjemler.split(separator).filterNot { it.isBlank() }.map { Hjemmel.of(it) },
            ytelser.split(separator).filterNot { it.isBlank() }.map { Ytelse.of(it) }
                .filter { it in ansattEnhetForInnloggetSaksbehandler.ytelser },
            typer.split(separator).filterNot { it.isBlank() }.map { Type.of(it) })
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
        return "Innstillinger(saksbehandlerident='$saksbehandlerident', hjemler='$hjemler', ytelser='$ytelser', typer='$typer', tidspunkt=$tidspunkt)"
    }

}

data class SaksbehandlerInnstillinger(
    val hjemler: List<Hjemmel> = emptyList(),
    val ytelser: List<Ytelse> = emptyList(),
    val typer: List<Type> = emptyList()
)