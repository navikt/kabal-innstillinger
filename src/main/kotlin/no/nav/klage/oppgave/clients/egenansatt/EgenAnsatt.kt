package no.nav.klage.oppgave.clients.egenansatt

import java.time.LocalDateTime

data class EgenAnsatt(val skjermetFra: LocalDateTime, val skjermetTil: LocalDateTime?) {

    fun erGyldig(): Boolean {
        val now = LocalDateTime.now()
        return skjermetFra.isBefore(now) && (skjermetTil ?: LocalDateTime.MAX).isAfter(now)
    }
}