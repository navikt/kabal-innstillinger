package no.nav.klage.oppgave.domain.kodeverk

enum class Ytelse(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {
    OMS_OMP("1", "Omsorgspenger", "Omsorgspenger"),
    OMS_OLP("2", "Opplæringspenger", "Opplæringspenger"),
    OMS_PSB("3", "Pleiepenger sykt barn", "Pleiepenger sykt barn"),
    OMS_PLS("4", "Pleiepenger i livets sluttfase", "Pleiepenger i livets sluttfase"),
    FOR_FOR("5", "Foreldrepenger", "Foreldrepenger"),
    FOR_ENG("5", "Engangsstønad", "Engangsstønad"),
    FOR_SVA("5", "Svangerskapspenger", "Svangerskapspenger"),
    SYK_SYK("6", "Sykepenger", "Sykepenger")
    ;

    companion object {
        fun of(id: String): Ytelse {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Ytelse with $id exists")
        }
    }
}

val ytelserPerEnhet = mapOf(
    "4203" to listOf(Ytelse.SYK_SYK),
    "4205" to listOf(Ytelse.FOR_FOR, Ytelse.FOR_ENG, Ytelse.FOR_SVA, Ytelse.SYK_SYK),
    "4207" to listOf(),
    "4214" to listOf(Ytelse.SYK_SYK),
    "4219" to listOf(Ytelse.OMS_OMP, Ytelse.OMS_PLS, Ytelse.OMS_PSB, Ytelse.OMS_OLP),
    "4250" to listOf(),
)

val enheterPerYtelse = mapOf(
    Ytelse.SYK_SYK to listOf("4203", "4205", "4214"),
    Ytelse.FOR_SVA to listOf("4205"),
    Ytelse.FOR_ENG to listOf("4205"),
    Ytelse.FOR_FOR to listOf("4205"),
    Ytelse.OMS_OMP to listOf("4219"),
    Ytelse.OMS_PLS to listOf("4219"),
    Ytelse.OMS_PSB to listOf("4219"),
    Ytelse.OMS_OLP to listOf("4219"),
)