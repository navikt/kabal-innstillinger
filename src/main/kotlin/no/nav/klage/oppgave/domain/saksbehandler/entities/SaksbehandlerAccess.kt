package no.nav.klage.oppgave.domain.saksbehandler.entities

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.YtelseConverter
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "saksbehandler_access", schema = "innstillinger")
class SaksbehandlerAccess(

    @Id
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String,
    @ElementCollection(targetClass = Ytelse::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "saksbehandler_access_ytelse",
        schema = "innstillinger",
        joinColumns = [JoinColumn(
            name = "saksbehandlerident",
            referencedColumnName = "saksbehandlerident",
            nullable = false
        )]
    )
    @Convert(converter = YtelseConverter::class)
    @Column(name = "ytelse_id")
    var ytelser: Set<Ytelse> = setOf(),
    @Column(name = "created")
    val created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "access_rights_modified")
    var accessRightsModified: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SaksbehandlerAccess

        if (saksbehandlerident != other.saksbehandlerident) return false
        if (ytelser != other.ytelser) return false
        if (created != other.created) return false
        if (accessRightsModified != other.accessRightsModified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = saksbehandlerident.hashCode()
        result = 31 * result + ytelser.hashCode()
        result = 31 * result + created.hashCode()
        result = 31 * result + accessRightsModified.hashCode()
        return result
    }

    override fun toString(): String {
        return "SaksbehandlerAccess(saksbehandlerident='$saksbehandlerident', ytelser=$ytelser, created=$created, accessRightsModified=$accessRightsModified)"
    }


}