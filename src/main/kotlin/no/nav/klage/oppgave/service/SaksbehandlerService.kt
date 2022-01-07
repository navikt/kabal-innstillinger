package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.ytelseTilKlageenheter
import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.domain.saksbehandler.*
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.InnstillingerRepository
import no.nav.klage.oppgave.repositories.ValgtEnhetRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val valgtEnhetRepository: ValgtEnhetRepository,
    private val innstillingerRepository: InnstillingerRepository,
    private val azureGateway: AzureGateway,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

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

    fun getMedunderskrivere(ident: String, enhetId: String, ytelse: Ytelse, fnr: String? = null): Medunderskrivere =
        if (fnr != null) {
            val personInfo = pdlFacade.getPersonInfo(fnr)
            val harBeskyttelsesbehovFortrolig = personInfo.harBeskyttelsesbehovFortrolig()
            val harBeskyttelsesbehovStrengtFortrolig = personInfo.harBeskyttelsesbehovStrengtFortrolig()
            val erEgenAnsatt = egenAnsattService.erEgenAnsatt(fnr)

            if (harBeskyttelsesbehovStrengtFortrolig) {
                //Kode 6 har skal ikke ha medunderskrivere
                Medunderskrivere(ytelse.id, emptyList())
            }
            if (harBeskyttelsesbehovFortrolig) {
                //Kode 7 skal ha medunderskrivere fra alle ytelser, men kun de med kode 7-rettigheter
                //TODO: Dette er klønete, vi burde gjort dette i ETT kall til AD, ikke n.
                val medunderskrivere = saksbehandlerRepository.getSaksbehandlereSomKanBehandleFortrolig()
                    .filter { it != ident }
                    .filter { egenAnsattFilter(fnr, erEgenAnsatt, ident) }
                    .map { Medunderskriver(it, getNameForIdent(it)) }
                Medunderskrivere(tema = null, ytelse = ytelse.id, medunderskrivere = medunderskrivere)
            }
            if (ytelseTilKlageenheter.contains(ytelse)) {
                val medunderskrivere = ytelseTilKlageenheter[ytelse]!!
                    .filter { it.navn != VIKAFOSSEN }
                    .flatMap { enhetRepository.getAnsatteIEnhet(it.navn) }
                    .filter { it != ident }
                    .filter { saksbehandlerRepository.erSaksbehandler(it) }
                    .filter { egenAnsattFilter(fnr, erEgenAnsatt, ident) }
                    .distinct()
                    .map { Medunderskriver(it, getNameForIdent(it)) }
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = medunderskrivere)
            } else {
                logger.error("Ytelsen $ytelse har ingen registrerte enheter i systemet vårt")
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = emptyList())
            }

        } else {
            if (ytelseTilKlageenheter.contains(ytelse)) {
                val medunderskrivere = ytelseTilKlageenheter[ytelse]!!
                    .filter { it.navn != VIKAFOSSEN }
                    .flatMap { enhetRepository.getAnsatteIEnhet(it.navn) }
                    .filter { it != ident }
                    .filter { saksbehandlerRepository.erSaksbehandler(it) }
                    .distinct()
                    .map { Medunderskriver(it, getNameForIdent(it)) }
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = medunderskrivere)
            } else {
                logger.error("Ytelsen $ytelse har ingen registrerte enheter i systemet vårt")
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = emptyList())
            }
        }
}
