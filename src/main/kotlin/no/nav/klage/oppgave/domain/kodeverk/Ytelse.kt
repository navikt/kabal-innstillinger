package no.nav.klage.oppgave.domain.kodeverk

enum class Ytelse(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {
    OMS_OMP("1", "Omsorgspenger", "Omsorgspenger"),
    OMS_OLP("2", "Opplæringspenger", "Opplæringspenger"),
    OMS_PSB("3", "Pleiepenger sykt barn", "Pleiepenger sykt barn"),
    OMS_PLS("4", "Pleiepenger i livets sluttfase", "Pleiepenger i livets sluttfase"),
    SYK_SYK("5", "Sykepenger", "Sykepenger"),
    //TODO: Koordiner disse med andre apper som bruker dette kodeverket.
    FOR_FOR("6", "Foreldrepenger", "Foreldrepenger"),
    FOR_ENG("7", "Engangsstønad", "Engangsstønad"),
    FOR_SVA("8", "Svangerskapspenger", "Svangerskapspenger"),
    ;

    companion object {
        fun of(id: String): Ytelse {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Ytelse with $id exists")
        }
    }
}

val ytelserPerEnhet = mapOf(
    "4291" to listOf(Ytelse.SYK_SYK),
    "4292" to listOf(Ytelse.FOR_FOR, Ytelse.FOR_ENG, Ytelse.FOR_SVA, Ytelse.SYK_SYK),
    "4293" to listOf(),
    "4294" to listOf(Ytelse.SYK_SYK),
    "4295" to listOf(Ytelse.OMS_OMP, Ytelse.OMS_PLS, Ytelse.OMS_PSB, Ytelse.OMS_OLP),
    "4250" to listOf(),
)

val enheterPerYtelse = mapOf(
    Ytelse.SYK_SYK to listOf("4291", "4292", "4294"),
    Ytelse.FOR_SVA to listOf("4292"),
    Ytelse.FOR_ENG to listOf("4292"),
    Ytelse.FOR_FOR to listOf("4292"),
    Ytelse.OMS_OMP to listOf("4295"),
    Ytelse.OMS_PLS to listOf("4295"),
    Ytelse.OMS_PSB to listOf("4295"),
    Ytelse.OMS_OLP to listOf("4295"),
)