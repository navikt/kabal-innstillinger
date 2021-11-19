package no.nav.klage.oppgave.clients.axsys

data class Tilganger(val enheter: List<Enhet>)

data class Enhet(val enhetId: String, val navn: String, val temaer: List<String>?)

data class Bruker(val appIdent: String, val historiskIdent: Long)