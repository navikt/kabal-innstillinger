package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.api.view.EnhetView
import no.nav.klage.oppgave.api.view.SaksbehandlerView
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Type
import no.nav.klage.oppgave.domain.kodeverk.Ytelse
import no.nav.klage.oppgave.domain.saksbehandler.*
import org.springframework.beans.factory.annotation.Value

class SaksbehandlerMapper(
    @Value("\${ROLE_GOSYS_OPPGAVE_BEHANDLER}") private val gosysSaksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_MERKANTIL}") private val merkantilRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String,
    @Value("\${ROLE_ADMIN}") private val adminRole: String
) {

    val rolleMapper = mapOf(
        gosysSaksbehandlerRole to "ROLE_GOSYS_OPPGAVE_BEHANDLER",
        saksbehandlerRole to "ROLE_KLAGE_SAKSBEHANDLER",
        fagansvarligRole to "ROLE_KLAGE_FAGANSVARLIG",
        lederRole to "ROLE_KLAGE_LEDER",
        merkantilRole to "ROLE_KLAGE_MERKANTIL",
        kanBehandleFortroligRole to "ROLE_KLAGE_FORTROLIG",
        kanBehandleStrengtFortroligRole to "ROLE_KLAGE_STRENGT_FORTROLIG",
        kanBehandleEgenAnsattRole to "ROLE_KLAGE_EGEN_ANSATT",
        adminRole to "ROLE_ADMIN",
    )


    fun SaksbehandlerInfo.mapToView() =
        SaksbehandlerView(
            info = info.mapToView(),
            roller = roller.mapNotNull { rolleMapper[it.id] },
            enheter = enheter.mapToView(),
            valgtEnhetView = valgtEnhet.mapToView(),
            innstillinger = innstillinger.mapToView()
        )

    fun SaksbehandlerPersonligInfo.mapToView() = SaksbehandlerView.PersonligInfoView(
        navIdent = navIdent,
        azureId = azureId,
        fornavn = fornavn,
        etternavn = etternavn,
        sammensattNavn = sammensattNavn,
        epost = epost
    )

    fun SaksbehandlerInnstillinger.mapToView() = SaksbehandlerView.InnstillingerView(
        hjemler = hjemler.map { it.id },
        ytelser = ytelser.map { it.id },
        typer = typer.map { it.id }
    )

    fun SaksbehandlerView.InnstillingerView.mapToDomain() = SaksbehandlerInnstillinger(
        hjemler = hjemler.map { Hjemmel.of(it) },
        ytelser = ytelser.map { Ytelse.of(it) },
        typer = typer.map { Type.of(it) }
    )

    fun EnheterMedLovligeYtelser.mapToView() = this.enheter.map { enhet -> enhet.mapToView() }

    fun EnhetMedLovligeYtelser.mapToView() =
        EnhetView(
            id = enhet.enhetId,
            navn = enhet.navn,
            lovligeYtelser = ytelser.map { ytelse -> ytelse.id }
        )
}
