package no.nav.klage.oppgave.domain.saksbehandler.entities

import jakarta.persistence.*
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.HjemmelConverter
import no.nav.klage.kodeverk.ytelse.Ytelse
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
    @ElementCollection(targetClass = Hjemmel::class, fetch = FetchType.LAZY)
    @CollectionTable(
        name = "innstillinger_hjemmel",
        schema = "innstillinger",
        joinColumns = [JoinColumn(name = "innstillinger_saksbehandlerident", referencedColumnName = "saksbehandlerident", nullable = false)]
    )
    @Convert(converter = HjemmelConverter::class)
    @Column(name = "id")
    var hjemler: Set<Hjemmel> = emptySet(),
    @ElementCollection(targetClass = Ytelse::class, fetch = FetchType.LAZY)
    @CollectionTable(
        name = "innstillinger_ytelse",
        schema = "innstillinger",
        joinColumns = [JoinColumn(name = "innstillinger_saksbehandlerident", referencedColumnName = "saksbehandlerident", nullable = false)]
    )
    @Convert(converter = YtelseConverter::class)
    @Column(name = "id")
    var ytelser: Set<Ytelse> = emptySet(),
    @Column(name = "short_name")
    var shortName: String? = null,
    @Column(name = "long_name")
    var longName: String? = null,
    @Column(name = "job_title")
    var jobTitle: String? = null,
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "anonymous")
    var anonymous: Boolean
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()

        const val SEPARATOR = ","
    }

    fun toSaksbehandlerInnstillinger(): SaksbehandlerInnstillinger {
        return SaksbehandlerInnstillinger(
            hjemler = hjemler,
            ytelser = ytelser,
            shortName = shortName,
            longName = longName,
            jobTitle = jobTitle,
            anonymous = anonymous
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
        return "Innstillinger(saksbehandlerident='$saksbehandlerident', hjemler=$hjemler, ytelser=$ytelser, shortName=$shortName, longName=$longName, jobTitle=$jobTitle, modified=$modified, anonymous=$anonymous)"
    }
}