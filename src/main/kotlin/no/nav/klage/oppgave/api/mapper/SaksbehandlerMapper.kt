package no.nav.klage.oppgave.api.mapper

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.api.view.EnhetView
import no.nav.klage.oppgave.api.view.SaksbehandlerView
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
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

    fun mapToView(saksbehandlerInfo: SaksbehandlerInfo) =
        SaksbehandlerView(
            navIdent = saksbehandlerInfo.navIdent,
            roller = saksbehandlerInfo.roller.mapNotNull { rolleMapper[it.id] },
            enheter = mapToView(saksbehandlerInfo.enheter),
            ansattEnhet = mapToView(saksbehandlerInfo.ansattEnhet),
        )

    fun mapToView(saksbehandlerInnstillinger: SaksbehandlerInnstillinger) =
        SaksbehandlerView.InnstillingerView(
            hjemler = saksbehandlerInnstillinger.hjemler.map { it.id },
            ytelser = saksbehandlerInnstillinger.ytelser.map { it.id },
            typer = saksbehandlerInnstillinger.typer.map { it.id }
        )

    fun mapToDomain(innstillingerView: SaksbehandlerView.InnstillingerView) =
        SaksbehandlerInnstillinger(
            hjemler = innstillingerView.hjemler.map { Hjemmel.of(it) },
            ytelser = innstillingerView.ytelser.map { Ytelse.of(it) },
            typer = innstillingerView.typer.map { Type.of(it) }
        )

    fun mapToView(enheterMedLovligeYtelser: EnheterMedLovligeYtelser) =
        enheterMedLovligeYtelser.enheter.map { enhet -> mapToView(enhet) }

    fun mapToView(enhetMedLovligeYtelser: EnhetMedLovligeYtelser) =
        EnhetView(
            id = enhetMedLovligeYtelser.enhet.enhetId,
            navn = enhetMedLovligeYtelser.enhet.navn,
            lovligeYtelser = enhetMedLovligeYtelser.ytelser.map { ytelse -> ytelse.id }
        )
}
