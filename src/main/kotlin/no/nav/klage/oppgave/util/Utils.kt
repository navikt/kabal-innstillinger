package no.nav.klage.oppgave.util

import org.apache.commons.lang3.StringUtils

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

fun String?.trimToNull(): String? {
    return StringUtils.trimToNull(this)
//    return if (this.isNullOrBlank()) {
//        null
//    } else {
//        this.trim().replace(Regex(pattern = "/\\s+/"), " ")
//    }
}