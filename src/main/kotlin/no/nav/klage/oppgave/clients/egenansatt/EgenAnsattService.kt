package no.nav.klage.oppgave.clients.egenansatt

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class EgenAnsattService {

    private val egenAnsattMap: ConcurrentMap<String, EgenAnsatt> = ConcurrentHashMap()

    fun erEgenAnsatt(foedselsnr: String): Boolean =
        egenAnsattMap[foedselsnr]?.erGyldig() ?: false

    fun oppdaterEgenAnsatt(foedselsnr: String, egenAnsatt: EgenAnsatt) {
        egenAnsattMap[foedselsnr] = egenAnsatt
    }
}