package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.AbbreviationResponse
import no.nav.klage.oppgave.domain.abbreviation.Abbreviation
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.AbbreviationRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.NoSuchElementException

@Service
@Transactional

class AbbreviationService(
    private val abbreviationRepository: AbbreviationRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun getAbbreviationsForSaksbehandler(navIdent: String): List<AbbreviationResponse> {
        return abbreviationRepository.findByNavIdent(navIdent).map { it.toAbbreviationResponse() }
    }

    fun createAbbreviationForSaksbehandler(short: String, long: String, navIdent: String): AbbreviationResponse {
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