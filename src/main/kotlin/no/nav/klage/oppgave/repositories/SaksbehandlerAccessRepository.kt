package no.nav.klage.oppgave.repositories

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SaksbehandlerAccessRepository : JpaRepository<SaksbehandlerAccess, String> {
    fun findAllByYtelserContaining(ytelse: Ytelse): List<SaksbehandlerAccess>
}
