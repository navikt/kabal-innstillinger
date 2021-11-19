package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.domain.kodeverk.Ytelse
import no.nav.klage.oppgave.domain.kodeverk.enheterPerYtelse
import no.nav.klage.oppgave.domain.saksbehandler.*
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.*
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val valgtEnhetRepository: ValgtEnhetRepository,
    private val innstillingerRepository: InnstillingerRepository,
    private val azureGateway: AzureGateway,
    private val enhetRepository: EnhetRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getEnheterMedTemaerForSaksbehandler(): EnheterMedLovligeYtelser =
        innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler()

    fun getMedunderskrivere(ident: String, ytelse: Ytelse): Medunderskrivere =
        if (enheterPerYtelse.contains(ytelse)) {
            val medunderskrivere = enheterPerYtelse[ytelse]!!
                .flatMap { enhetRepository.getAnsatteIEnhet(it) }
                .filter { it != ident }
                .map { Medunderskriver(it, getNameForIdent(it)) }
            Medunderskrivere(ytelse.id, medunderskrivere)
        } else {
            logger.error("Ytelsen $ytelse har ingen registrerte enheter i systemet vårt")
            Medunderskrivere(ytelse.id, emptyList())
        }

    private fun saksbehandlerHarTilgangTilYtelse(ident: String, ytelse: Ytelse) =
        saksbehandlerRepository.getEnheterMedYtelserForSaksbehandler(ident).enheter.flatMap { it.ytelser }
            .contains(ytelse)

    fun getNameForIdent(it: String) = saksbehandlerRepository.getNameForSaksbehandler(it)

    @Transactional
    fun storeValgtEnhetId(ident: String, enhetId: String): EnhetMedLovligeYtelser {
        val enhet =
            innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler().enheter.find { it.enhet.enhetId == enhetId }
                ?: throw MissingTilgangException("Saksbehandler $ident har ikke tilgang til enhet $enhetId")

        valgtEnhetRepository.save(
            mapToValgtEnhet(ident, enhet)
        )
        return enhet
    }

    private fun mapToValgtEnhet(ident: String, enhet: EnhetMedLovligeYtelser): ValgtEnhet {
        return ValgtEnhet(
            saksbehandlerident = ident,
            enhetId = enhet.enhet.enhetId,
            enhetNavn = enhet.enhet.navn,
            tidspunkt = LocalDateTime.now()
        )
    }

    @Transactional
    fun findValgtEnhet(ident: String): EnhetMedLovligeYtelser {
        return valgtEnhetRepository.findByIdOrNull(ident)
            ?.let { valgtEnhet -> innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler().enheter.find { it.enhet.enhetId == valgtEnhet.enhetId } }
            ?: storeValgtEnhetId(
                ident,
                innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler().enheter.first().enhet.enhetId
            )
    }

    @Transactional
    fun findInnstillinger(ident: String): SaksbehandlerInnstillinger {
        return innstillingerRepository.findByIdOrNull(ident)?.toSaksbehandlerInnstillinger()
            ?: SaksbehandlerInnstillinger()
    }

    @Transactional
    fun storeInnstillinger(
        navIdent: String,
        saksbehandlerInnstillinger: SaksbehandlerInnstillinger
    ): SaksbehandlerInnstillinger {
        return innstillingerRepository.save(
            Innstillinger.fromSaksbehandlersInnstillinger(
                navIdent,
                saksbehandlerInnstillinger
            )
        ).toSaksbehandlerInnstillinger()
    }

    fun getDataOmSaksbehandler(navIdent: String): SaksbehandlerInfo {
        val dataOmInnloggetSaksbehandler = azureGateway.getDataOmInnloggetSaksbehandler()
        val rollerForInnloggetSaksbehandler = azureGateway.getRollerForInnloggetSaksbehandler()
        val enheterForInnloggetSaksbehandler = innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler()
        val valgtEnhet = findValgtEnhet(innloggetSaksbehandlerRepository.getInnloggetIdent())
        val innstillinger = findInnstillinger(innloggetSaksbehandlerRepository.getInnloggetIdent())
        return SaksbehandlerInfo(
            dataOmInnloggetSaksbehandler,
            rollerForInnloggetSaksbehandler,
            enheterForInnloggetSaksbehandler,
            valgtEnhet,
            innstillinger
        )
    }
}
