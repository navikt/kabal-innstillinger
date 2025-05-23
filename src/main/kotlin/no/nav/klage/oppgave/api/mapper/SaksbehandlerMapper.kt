package no.nav.klage.oppgave.api.mapper

import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.ytelse.Ytelse
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
class SaksbehandlerMapper(
    private val roleUtils: RoleUtils,
) {

    fun mapToView(saksbehandlerInfo: SaksbehandlerInfo) =
        SaksbehandlerView(
            navIdent = saksbehandlerInfo.navIdent,
            navn = saksbehandlerInfo.navn,
            roller = saksbehandlerInfo.roller.flatMap { roleUtils.getRoleNamesFromId(it.id) },
            enheter = mapToView(saksbehandlerInfo.enheter),
            ansattEnhet = mapToView(saksbehandlerInfo.ansattEnhet),
            tildelteYtelser = saksbehandlerInfo.tildelteYtelser.sortedBy { it.navn }.map { it.id }
        )

    fun mapToView(saksbehandlerInnstillinger: SaksbehandlerInnstillinger) =
        InnstillingerView(
            hjemler = saksbehandlerInnstillinger.hjemler.map { it.id }.toSet(),
            ytelser = saksbehandlerInnstillinger.ytelser.sortedBy { it.navn }.map { it.id }.toSet(),
        )

    fun mapToDomain(innstillingerView: InnstillingerView) =
        SaksbehandlerInnstillinger(
            hjemler = innstillingerView.hjemler.map { Hjemmel.of(it) }.toSet(),
            ytelser = innstillingerView.ytelser.map { Ytelse.of(it) }.toSet(),
            //Placeholder, ignored later
            anonymous = false
        )

    fun mapToView(enheterMedLovligeYtelser: EnheterMedLovligeYtelser) =
        enheterMedLovligeYtelser.enheter.map { enhet -> mapToView(enhet) }

    fun mapToView(enhetMedLovligeYtelser: EnhetMedLovligeYtelser) =
        EnhetView(
            id = enhetMedLovligeYtelser.enhet.enhetId,
            navn = enhetMedLovligeYtelser.enhet.navn,
            lovligeYtelser = enhetMedLovligeYtelser.ytelser.sortedBy { it.navn }.map { ytelse -> ytelse.id }
        )
}