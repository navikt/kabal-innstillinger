package no.nav.klage.oppgave.domain.abbreviation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "Abbreviation", schema = "innstillinger")
class Abbreviation(
    @Id
    @Column(name = "id")
    val id: UUID = UUID.randomUUID(),
    @Column(name = "nav_ident")
    val navIdent: String,
    @Column(name = "short")
    var short: String,
    @Column(name = "long")
    var long: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Abbreviation

        if (id != other.id) return false
        if (navIdent != other.navIdent) return false
        if (short != other.short) return false
        if (long != other.long) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + navIdent.hashCode()
        result = 31 * result + short.hashCode()
        result = 31 * result + long.hashCode()
        return result
    }

    override fun toString(): String {
        return "Abbreviation(id=$id, navIdent='$navIdent', short='$short', long='$long')"
    }

}