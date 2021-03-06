package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.ytelseTilKlageenheter
import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.api.view.Signature
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.saksbehandler.*
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.*
import no.nav.klage.oppgave.util.generateShortNameOrNull
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val valgtEnhetRepository: ValgtEnhetRepository,
    private val innstillingerRepository: InnstillingerRepository,
    private val azureGateway: AzureGateway,
    private val pdlFacade: PdlFacade,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val enhetRepository: EnhetRepository,
    private val egenAnsattService: EgenAnsattService,
    private val tilgangService: TilgangService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val VIKAFOSSEN = "2103"
    }

    fun storeValgtEnhetId(ident: String, enhetId: String): EnhetMedLovligeYtelser {
        if (enhetId != findValgtEnhet(ident).enhet.enhetId) {
            logger.warn("Saksbehandler skal ikke kunne velge denne enheten, det er ikke den hen er ansatt i")
        }

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

    fun findValgtEnhet(ident: String): EnhetMedLovligeYtelser {
        return innloggetSaksbehandlerRepository.getEnhetMedYtelserForSaksbehandler()
    }

    private fun findSaksbehandlerInnstillinger(
        ident: String,
        ansattEnhetForInnloggetSaksbehandler: EnhetMedLovligeYtelser
    ): SaksbehandlerInnstillinger {
        return innstillingerRepository.findByIdOrNull(ident)
            ?.toSaksbehandlerInnstillinger(ansattEnhetForInnloggetSaksbehandler)
            ?: SaksbehandlerInnstillinger()
    }

    fun storeInnstillingerButKeepSignature(
        navIdent: String,
        newSaksbehandlerInnstillinger: SaksbehandlerInnstillinger
    ): SaksbehandlerInnstillinger {
        val ansattEnhetForInnloggetSaksbehandler: EnhetMedLovligeYtelser =
            innloggetSaksbehandlerRepository.getEnhetMedYtelserForSaksbehandler()

        val oldInnstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)
        val separator = ","

        return innstillingerRepository.save(
            Innstillinger(
                saksbehandlerident = navIdent,
                hjemler = newSaksbehandlerInnstillinger.hjemler.joinToString(separator) { it.id },
                ytelser = newSaksbehandlerInnstillinger.ytelser.filter { it in ansattEnhetForInnloggetSaksbehandler.ytelser }
                    .joinToString(separator) { it.id },
                typer = newSaksbehandlerInnstillinger.typer.joinToString(separator) { it.id },
                shortName = oldInnstillinger?.shortName,
                longName = oldInnstillinger?.longName,
                jobTitle = oldInnstillinger?.jobTitle,
                tidspunkt = LocalDateTime.now()
            )
        ).toSaksbehandlerInnstillinger(ansattEnhetForInnloggetSaksbehandler)
    }

    fun getDataOmSaksbehandler(navIdent: String): SaksbehandlerInfo {
        val ansattEnhetForInnloggetSaksbehandler = innloggetSaksbehandlerRepository.getEnhetMedYtelserForSaksbehandler()

        val saksbehandlerInnstillinger = findSaksbehandlerInnstillinger(
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            ansattEnhetForInnloggetSaksbehandler,
        )

        val rollerForInnloggetSaksbehandler = azureGateway.getRollerForInnloggetSaksbehandler()
        val enheterForInnloggetSaksbehandler = innloggetSaksbehandlerRepository.getEnheterMedYtelserForSaksbehandler()
        val valgtEnhet = findValgtEnhet(innloggetSaksbehandlerRepository.getInnloggetIdent())

        return SaksbehandlerInfo(
            navIdent = navIdent,
            roller = rollerForInnloggetSaksbehandler,
            enheter = enheterForInnloggetSaksbehandler,
            ansattEnhet = ansattEnhetForInnloggetSaksbehandler,
            valgtEnhet = valgtEnhet,
            saksbehandlerInnstillinger = saksbehandlerInnstillinger
        )
    }

    //TODO: Skal skrives om til ?? hente valgte ytelser fra innstillinger og bruke det for ?? finne medunderskrivere.
    //Trenger da ikke lenger ?? kalle enhetRepository.getAnsatteIEnhet(), s?? jeg bruker ikke tid p?? ?? skrive om den n??
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
                //TODO: Dette er kl??nete, vi burde gjort dette i ETT kall til AD, ikke n.
                val medunderskrivere = saksbehandlerRepository.getSaksbehandlereSomKanBehandleFortrolig()
                    .filter { it != ident }
                    .filter { egenAnsattFilter(fnr, erEgenAnsatt, ident) }
                    .map { Medunderskriver(it, getNameForIdent(it).sammensattNavn) }
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = medunderskrivere)
            }
            if (ytelseTilKlageenheter.contains(ytelse)) {
                val medunderskrivere = ytelseTilKlageenheter[ytelse]!!
                    .filter { it.navn != VIKAFOSSEN }
                    .sortedBy { it.navn != enhetId }
                    .flatMap { enhetRepository.getAnsatteIEnhet(it.navn) }
                    .filter { it != ident }
                    .filter { saksbehandlerRepository.erSaksbehandler(it) }
                    .filter { egenAnsattFilter(fnr, erEgenAnsatt, ident) }
                    .distinct()
                    .map { Medunderskriver(it, getNameForIdent(it).sammensattNavn) }
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = medunderskrivere)
            } else {
                logger.error("Ytelsen $ytelse har ingen registrerte enheter i systemet v??rt")
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = emptyList())
            }

        } else {
            if (ytelseTilKlageenheter.contains(ytelse)) {
                val medunderskrivere = ytelseTilKlageenheter[ytelse]!!
                    .filter { it.navn != VIKAFOSSEN }
                    .sortedBy { it.navn != enhetId }
                    .flatMap { enhetRepository.getAnsatteIEnhet(it.navn) }
                    .filter { it != ident }
                    .filter { saksbehandlerRepository.erSaksbehandler(it) }
                    .distinct()
                    .map { Medunderskriver(it, getNameForIdent(it).sammensattNavn) }
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = medunderskrivere)
            } else {
                logger.error("Ytelsen $ytelse har ingen registrerte enheter i systemet v??rt")
                Medunderskrivere(ytelse = ytelse.id, medunderskrivere = emptyList())
            }
        }

    private fun egenAnsattFilter(fnr: String, erEgenAnsatt: Boolean, ident: String) =
        if (!erEgenAnsatt) {
            true
        } else {
            tilgangService.harSaksbehandlerTilgangTil(ident = ident, fnr = fnr)
        }

    fun getNameForIdent(navIdent: String) =
        saksbehandlerRepository.getNameForSaksbehandler(navIdent)

    fun storeShortName(navIdent: String, shortName: String?) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.shortName = shortName
    }

    fun storeLongName(navIdent: String, longName: String?) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.longName = longName
    }

    fun storeJobTitle(navIdent: String, jobTitle: String?) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.jobTitle = jobTitle
    }

    private fun getOrCreateInnstillinger(navIdent: String): Innstillinger {
        var innstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)
        if (innstillinger == null) {
            innstillinger = innstillingerRepository.save(
                Innstillinger(
                    saksbehandlerident = navIdent,
                )
            )
        }
        return innstillinger
    }

    fun getSignature(navIdent: String): Signature {
        val innstillinger = innstillingerRepository.findBySaksbehandlerident(ident = navIdent)

        val name = saksbehandlerRepository.getNameForSaksbehandler(navIdent)

        return Signature(
            longName = name.sammensattNavn,
            generatedShortName = generateShortNameOrNull(fornavn = name.fornavn, etternavn = name.etternavn),
            customLongName = innstillinger?.longName,
            customShortName = innstillinger?.shortName,
            customJobTitle = innstillinger?.jobTitle,
        )
    }

}
