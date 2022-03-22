package no.nav.klage.oppgave.util

fun generateShortNameOrNull(fornavn: String, etternavn: String): String? {
    val fornavnList = fornavn.split(" ")
    if (fornavnList.any { it.first().isLowerCase() }) {
        return null
    }

    var fornavnFirstLetters = ""
    fornavnList.forEach { fn ->
        fornavnFirstLetters += fn.first() + "."
    }

    return "$fornavnFirstLetters $etternavn"
}