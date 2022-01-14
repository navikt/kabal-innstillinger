package no.nav.klage.oppgave.clients.pdl.graphql

import java.time.LocalDate


data class HentPersonerResponse(val data: HentPersonBolk?, val errors: List<PdlError>? = null)

data class HentPersonResponse(val data: DataWrapper?, val errors: List<PdlError>? = null)

data class DataWrapper(val hentPerson: PdlPerson?)

data class HentPersonBolk(val hentPersonBolk: List<HentPersonBolkResult>?)

data class HentPersonBolkResult(
    val person: PdlPerson?,
    val ident: String
)

data class PdlPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>,
    val navn: List<Navn>,
    val kjoenn: List<Kjoenn>,
    val sivilstand: List<Sivilstand>
) {
    data class Adressebeskyttelse(val gradering: GraderingType) {
        enum class GraderingType { STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG, UGRADERT }
    }

    data class Sivilstand(
        val type: SivilstandType,
        val gyldigFraOgMed: LocalDate?,
        val relatertVedSivilstand: String?,
        val bekreftelsesdato: LocalDate?
    ) {

        fun dato(): LocalDate? = gyldigFraOgMed ?: bekreftelsesdato

        enum class SivilstandType {
            UOPPGITT,
            UGIFT,
            GIFT,
            ENKE_ELLER_ENKEMANN,
            SKILT,
            SEPARERT,
            REGISTRERT_PARTNER,
            SEPARERT_PARTNER,
            SKILT_PARTNER,
            GJENLEVENDE_PARTNER
        }
    }

    data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
    )

    data class Kjoenn(val kjoenn: KjoennType?) {
        enum class KjoennType { MANN, KVINNE, UKJENT }
    }
}
