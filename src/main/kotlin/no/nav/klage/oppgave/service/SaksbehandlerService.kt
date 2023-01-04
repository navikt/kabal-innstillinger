package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.klageenheter
import no.nav.klage.kodeverk.ytelseTilKlageenheter
import no.nav.klage.oppgave.api.view.MedunderskrivereForYtelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.api.view.Saksbehandlere
import no.nav.klage.oppgave.api.view.Signature
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.saksbehandler.*
import no.nav.klage.oppgave.domain.saksbehandler.entities.Innstillinger
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.*
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.generateShortNameOrNull
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SaksbehandlerService(
    private val innloggetAnsattRepository: InnloggetAnsattRepository,
    private val innstillingerRepository: InnstillingerRepository,
    private val azureGateway: AzureGateway,
    private val roleUtils: RoleUtils,
    private val enhetRepository: EnhetRepository,
    private val pdlFacade: PdlFacade,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val egenAnsattService: EgenAnsattService,
    private val tilgangService: TilgangService,
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val VIKAFOSSEN = "2103"
    }

    fun findValgtEnhet(ident: String): EnhetMedLovligeYtelser {
        return innloggetAnsattRepository.getEnhetMedYtelserForSaksbehandler()
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
            innloggetAnsattRepository.getEnhetMedYtelserForSaksbehandler()

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
        val ansattEnhetForInnloggetSaksbehandler = innloggetAnsattRepository.getEnhetMedYtelserForSaksbehandler()

        val saksbehandlerInnstillinger = findSaksbehandlerInnstillinger(
            innloggetAnsattRepository.getInnloggetIdent(),
            ansattEnhetForInnloggetSaksbehandler,
        )

        val rollerForInnloggetSaksbehandler = azureGateway.getRollerForInnloggetSaksbehandler()
        val enheterForInnloggetSaksbehandler = innloggetAnsattRepository.getEnheterMedYtelserForSaksbehandler()
        val valgtEnhet = findValgtEnhet(innloggetAnsattRepository.getInnloggetIdent())
        val tildelteYtelser = getSaksbehandlerAccessYtelseIdList(navIdent)

        return SaksbehandlerInfo(
            navIdent = navIdent,
            roller = rollerForInnloggetSaksbehandler,
            enheter = enheterForInnloggetSaksbehandler,
            ansattEnhet = ansattEnhetForInnloggetSaksbehandler,
            valgtEnhet = valgtEnhet,
            saksbehandlerInnstillinger = saksbehandlerInnstillinger,
            tildelteYtelser = tildelteYtelser.map { Ytelse.of(it) }
        )
    }

    fun getMedunderskrivere(ident: String, ytelse: Ytelse, fnr: String?): MedunderskrivereForYtelse {
        return MedunderskrivereForYtelse(
            ytelse = ytelse.id,
            medunderskrivere = getPossibleSaksbehandlereForYtelseAndFnr(
                ytelse = ytelse,
                fnr = fnr!!
            ).filter { it.navIdent != ident })
    }

    fun getSaksbehandlere(ytelse: Ytelse, fnr: String): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlereForYtelseAndFnr(
                ytelse = ytelse,
                fnr = fnr
            ).toList()
        )
    }

    private fun getPossibleSaksbehandlereForYtelseAndFnr(ytelse: Ytelse, fnr: String): Set<Saksbehandler> {
        val personInfo = pdlFacade.getPersonInfo(fnr)
        val harBeskyttelsesbehovFortrolig = personInfo.harBeskyttelsesbehovFortrolig()
        val harBeskyttelsesbehovStrengtFortrolig = personInfo.harBeskyttelsesbehovStrengtFortrolig()
        val erEgenAnsatt = egenAnsattService.erEgenAnsatt(fnr)

        if (harBeskyttelsesbehovStrengtFortrolig) {
            //Kode 6 har skal ikke ha medunderskrivere
            return emptySet()
        }
        if (harBeskyttelsesbehovFortrolig) {
            //Kode 7 skal ha medunderskrivere fra alle ytelser, men kun de med kode 7-rettigheter
            //TODO: Dette er klønete, vi burde gjort dette i ETT kall til AD, ikke n.
            val saksbehandlere = klageenheter
                .filter { it.navn != VIKAFOSSEN }
                .flatMap { enhetRepository.getAnsatteIEnhet(it.navn) }
                .asSequence()
                .distinct()
                .filter { roleUtils.isSaksbehandler(ident = it) }
                .filter { roleUtils.kanBehandleFortrolig(ident = it) }
                .filter { egenAnsattFilter(fnr = fnr, erEgenAnsatt = erEgenAnsatt, ident = it) }
                .map { Saksbehandler(navIdent = it, navn = getNameForIdent(it).sammensattNavn) }
                .toList()

            return saksbehandlere.toSet()
        }

        return if (ytelseTilKlageenheter.contains(ytelse)) {
            val saksbehandlere = ytelseTilKlageenheter[ytelse]!!
                .filter { it.navn != VIKAFOSSEN }
                .flatMap { enhetRepository.getAnsatteIEnhet(it.navn) }
                .distinct()
                .filter { roleUtils.isSaksbehandler(ident = it) }
                .filter { egenAnsattFilter(fnr = fnr, erEgenAnsatt = erEgenAnsatt, ident = it) }
                .map { Saksbehandler(navIdent = it, navn = getNameForIdent(it).sammensattNavn) }
            saksbehandlere.toSet()
        } else {
            logger.error("Ytelsen $ytelse har ingen registrerte enheter i systemet vårt")
            emptySet()
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
            longName = name.fornavn + " " + name.etternavn,
            generatedShortName = generateShortNameOrNull(fornavn = name.fornavn, etternavn = name.etternavn),
            customLongName = innstillinger?.longName,
            customShortName = innstillinger?.shortName,
            customJobTitle = innstillinger?.jobTitle,
        )
    }

    private fun getSaksbehandlerAccessYtelseIdList(saksbehandlerIdent: String): List<String> {
        val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
        return saksbehandlerAccess.ytelser.map { it.id }
    }

}
