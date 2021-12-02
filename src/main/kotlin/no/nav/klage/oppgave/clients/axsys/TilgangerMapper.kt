package no.nav.klage.oppgave.clients.axsys

import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component

@Component
class TilgangerMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        private val unwantedEnheter = listOf("0118", "9999") //NAV Aremark and ANDRE EKSTERNE
    }

    fun mapTilgangerToEnheter(tilganger: Tilganger): List<Enhet> =
        tilganger.enheter.map { enhet -> Enhet(enhet.enhetId, enhet.navn) }
            .filter { enhet -> enhet.enhetId !in unwantedEnheter }

}