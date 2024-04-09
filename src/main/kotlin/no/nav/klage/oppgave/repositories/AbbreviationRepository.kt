package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.abbreviation.Abbreviation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AbbreviationRepository: JpaRepository<Abbreviation, UUID> {
    fun findByNavIdent(navIdent: String): List<Abbreviation>
}