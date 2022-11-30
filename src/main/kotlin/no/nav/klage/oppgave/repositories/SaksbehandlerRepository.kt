package no.nav.klage.oppgave.repositories

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.klageenhetTilYtelser
import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerName
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class SaksbehandlerRepository(
    private val azureGateway: AzureGateway,
    private val roleUtils: RoleUtils,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun harTilgangTilEnhetOgYtelse(ident: String, enhetId: String, ytelse: Ytelse): Boolean {
        val enhet = getEnheterMedYtelserForSaksbehandler(ident).enheter.firstOrNull { it.enhet.enhetId == enhetId }
        val ytelser = enhet?.ytelser
        return ytelser?.contains(ytelse) ?: false
    }

    fun harTilgangTilEnhet(ident: String, enhetId: String): Boolean {
        return getEnheterMedYtelserForSaksbehandler(ident).enheter.firstOrNull { it.enhet.enhetId == enhetId } != null
    }

    fun harTilgangTilYtelse(ident: String, ytelse: Ytelse): Boolean {
        return getEnheterMedYtelserForSaksbehandler(ident).enheter.flatMap { it.ytelser }.contains(ytelse)
    }

    fun getEnhetMedYtelserForSaksbehandler(navIdent: String): EnhetMedLovligeYtelser =
        azureGateway.getDataOmInnloggetSaksbehandler().enhet.let {
            EnhetMedLovligeYtelser(
                enhet = it,
                ytelser = getYtelserForEnhet(it)
            )
        }

    fun getEnheterMedYtelserForSaksbehandler(ident: String): EnheterMedLovligeYtelser =
        listOf(azureGateway.getDataOmInnloggetSaksbehandler().enhet).berikMedYtelser()

    private fun List<Enhet>.berikMedYtelser(): EnheterMedLovligeYtelser {
        return EnheterMedLovligeYtelser(this.map {
            EnhetMedLovligeYtelser(
                enhet = it,
                ytelser = getYtelserForEnhet(it)
            )
        })
    }

    private fun getYtelserForEnhet(enhet: Enhet): List<Ytelse> =
        klageenhetTilYtelser.filter { it.key.navn == enhet.enhetId }.flatMap { it.value }

    fun getNameForSaksbehandler(navIdent: String): SaksbehandlerName {
        val saksbehandlerPersonligInfo = azureGateway.getPersonligDataOmSaksbehandlerMedIdent(navIdent)
        return SaksbehandlerName(
            fornavn = saksbehandlerPersonligInfo.fornavn,
            etternavn = saksbehandlerPersonligInfo.etternavn,
            sammensattNavn = saksbehandlerPersonligInfo.sammensattNavn,
        )
    }
}