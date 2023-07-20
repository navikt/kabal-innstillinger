package no.nav.klage.oppgave.api.mapper

import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.api.view.EnhetView
import no.nav.klage.oppgave.api.view.InnstillingerView
import no.nav.klage.oppgave.api.view.SaksbehandlerView
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import no.nav.klage.oppgave.util.RoleUtils
import org.springframework.stereotype.Component

@Component
class SaksbehandlerMapper(private val roleUtils: RoleUtils) {

    fun mapToView(saksbehandlerInfo: SaksbehandlerInfo) =
        SaksbehandlerView(
            navIdent = saksbehandlerInfo.navIdent,
            roller = saksbehandlerInfo.roller.flatMap { roleUtils.getRoleNamesFromId(it.id) },
            enheter = mapToView(saksbehandlerInfo.enheter),
            ansattEnhet = mapToView(saksbehandlerInfo.ansattEnhet),
            tildelteYtelser = saksbehandlerInfo.tildelteYtelser.map { it.id }
        )

    fun mapToView(saksbehandlerInnstillinger: SaksbehandlerInnstillinger) =
        InnstillingerView(
            hjemler = saksbehandlerInnstillinger.hjemler.map { it.id },
            ytelser = saksbehandlerInnstillinger.ytelser.map { it.id },
            typer = saksbehandlerInnstillinger.typer.map { it.id }
        )

    fun mapToDomain(innstillingerView: InnstillingerView) =
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