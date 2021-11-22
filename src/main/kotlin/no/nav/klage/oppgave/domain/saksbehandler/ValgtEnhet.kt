package no.nav.klage.oppgave.domain.saksbehandler

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "valgt_enhet", schema = "innstillinger")
class ValgtEnhet(
    @Id
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String,
    @Column(name = "enhet_id")
    val enhetId: String,
    @Column(name = "enhet_navn")
    val enhetNavn: String,
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ValgtEnhet

        if (saksbehandlerident != other.saksbehandlerident) return false

        return true
    }

    override fun hashCode(): Int {
        return saksbehandlerident.hashCode()
    }

    override fun toString(): String {
        return "ValgtEnhet(saksbehandlerident='$saksbehandlerident', enhetId='$enhetId', enhetNavn='$enhetNavn', tidspunkt=$tidspunkt)"
    }
}
