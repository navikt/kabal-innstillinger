package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.AbbreviationResponse
import no.nav.klage.oppgave.domain.abbreviation.Abbreviation
import no.nav.klage.oppgave.exceptions.AbbreviationAlreadyExistsException
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.AbbreviationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AbbreviationService(
    private val abbreviationRepository: AbbreviationRepository
) {
    fun getAbbreviationsForSaksbehandler(navIdent: String): List<AbbreviationResponse> {
        return abbreviationRepository.findByNavIdent(navIdent).map { it.toAbbreviationResponse() }
    }

    fun createAbbreviationForSaksbehandler(short: String, long: String, navIdent: String): AbbreviationResponse {
        val existingAbbreviationsForSaksbehandler = abbreviationRepository.findByNavIdent(navIdent)
        if (existingAbbreviationsForSaksbehandler.any { it.short == short }) {
            throw AbbreviationAlreadyExistsException("Bruker har allerede en forkortelse lagret med kortform $short")
        }

        return abbreviationRepository.save(
            Abbreviation(
                navIdent = navIdent,
                short = short,
                long = long,
            )
        ).toAbbreviationResponse()
    }

    fun updateAbbreviation(abbreviationId: UUID, short: String, long: String, navIdent: String): AbbreviationResponse {
        if (abbreviationRepository.existsById(abbreviationId)) {
            val abbreviation = abbreviationRepository.getReferenceById(abbreviationId)
            if (abbreviation.navIdent != navIdent) {
                throw MissingTilgangException(msg = "Forkortelse er ikke opprettet av innlogget bruker")
            }

            abbreviation.short = short
            abbreviation.long = long

            return abbreviation.toAbbreviationResponse()

        } else {
            throw NoSuchElementException("Finner ingen forkortelse med denne id-en")
        }
    }

    fun deleteAbbreviation(abbreviationId: UUID, navIdent: String) {
        if (abbreviationRepository.existsById(abbreviationId)) {
            val abbreviation = abbreviationRepository.getReferenceById(abbreviationId)
            if (abbreviation.navIdent != navIdent) {
                throw MissingTilgangException(msg = "Forkortelse er ikke opprettet av innlogget bruker")
            }

            abbreviationRepository.deleteById(abbreviationId)
        } else {
            throw NoSuchElementException("Finner ingen forkortelse med denne id-en")
        }
    }

    fun Abbreviation.toAbbreviationResponse(): AbbreviationResponse {
        return AbbreviationResponse(
            id = id,
            short = short,
            long = long,
        )
    }
}