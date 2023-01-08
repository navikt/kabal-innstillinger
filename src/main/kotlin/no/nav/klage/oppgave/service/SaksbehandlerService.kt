package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseTilHjemler
import no.nav.klage.kodeverk.klageenheter
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
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository,
    private val saksbehandlerAccessService: SaksbehandlerAccessService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private const val VIKAFOSSEN = "2103"
        const val SEPARATOR = ","
    }

    fun findValgtEnhet(ident: String): EnhetMedLovligeYtelser {
        return innloggetAnsattRepository.getEnhetMedYtelserForSaksbehandler()
    }

    private fun findSaksbehandlerInnstillinger(
        ident: String,
    ): SaksbehandlerInnstillinger {
        return innstillingerRepository.findByIdOrNull(ident)
            ?.toSaksbehandlerInnstillinger()
            ?: SaksbehandlerInnstillinger()
    }

    fun storeInnstillingerButKeepSignature(
        navIdent: String,
        newSaksbehandlerInnstillinger: SaksbehandlerInnstillinger
    ): SaksbehandlerInnstillinger {
        val assignedYtelseIdList = getSaksbehandlerAssignedYtelseIdList(navIdent)

        val oldInnstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)

        return innstillingerRepository.save(
            Innstillinger(
                saksbehandlerident = navIdent,
                hjemler = newSaksbehandlerInnstillinger.hjemler.joinToString(SEPARATOR) { it.id },
                ytelser = newSaksbehandlerInnstillinger.ytelser.filter { it in assignedYtelseIdList }
                    .joinToString(SEPARATOR) { it.id },
                typer = newSaksbehandlerInnstillinger.typer.joinToString(SEPARATOR) { it.id },
                shortName = oldInnstillinger?.shortName,
                longName = oldInnstillinger?.longName,
                jobTitle = oldInnstillinger?.jobTitle,
                modified = LocalDateTime.now()
            )
        ).toSaksbehandlerInnstillinger()
    }

    fun storeInnstillingerYtelser(
        navIdent: String,
        inputYtelseSet: Set<Ytelse>,
    ) {
        val assignedYtelseIdList = getSaksbehandlerAssignedYtelseIdList(navIdent)
        val filteredYtelseList = inputYtelseSet.filter { it in assignedYtelseIdList }

        if (!innstillingerRepository.existsById(navIdent)) {
            val hjemmelSet = getUpdatedHjemmelSet(
                ytelserToAdd = filteredYtelseList.toSet()
            )

            innstillingerRepository.save(
                Innstillinger(
                    saksbehandlerident = navIdent,
                    hjemler = hjemmelSet
                        .joinToString(SEPARATOR) { it.id },
                    ytelser = filteredYtelseList
                        .joinToString(SEPARATOR) { it.id },
                    typer = "",
                    shortName = null,
                    longName = null,
                    jobTitle = null,
                    modified = LocalDateTime.now()
                )
            )
        } else {

            val existingInnstillinger = findSaksbehandlerInnstillinger(
                ident = navIdent,
            )

            val existingInnstillingerYtelseSet = existingInnstillinger.ytelser.toSet()
            val existingHjemmelSet = existingInnstillinger.hjemler.toSet()

            val ytelserToAdd = getYtelserToAdd(
                inputYtelser = inputYtelseSet,
                existingInnstillingerYtelser = existingInnstillingerYtelseSet
            )
            val ytelserToKeep = getYtelserToKeep(
                inputYtelser = inputYtelseSet,
                existingInnstillingerYtelser = existingInnstillingerYtelseSet
            )

            val hjemmelSet = getUpdatedHjemmelSet(
                ytelserToAdd = ytelserToAdd, ytelserToKeep = ytelserToKeep, existingHjemler = existingHjemmelSet
            )

            innstillingerRepository.getReferenceById(navIdent).apply {
                ytelser = filteredYtelseList
                    .joinToString(SEPARATOR) { it.id }
                hjemler = hjemmelSet
                    .joinToString(SEPARATOR) { it.id }
                modified = LocalDateTime.now()
            }
        }
    }

    fun getYtelserToAdd(
        inputYtelser: Set<Ytelse>,
        existingInnstillingerYtelser: Set<Ytelse> = emptySet()
    ): Set<Ytelse> {
        return inputYtelser.filter { it !in existingInnstillingerYtelser }.toSet()
    }

    fun getYtelserToKeep(inputYtelser: Set<Ytelse>, existingInnstillingerYtelser: Set<Ytelse>): Set<Ytelse> {
        return inputYtelser.intersect(existingInnstillingerYtelser)
    }

    fun getUpdatedHjemmelSet(
        ytelserToAdd: Set<Ytelse>,
        ytelserToKeep: Set<Ytelse>? = null,
        existingHjemler: Set<Hjemmel>? = null,
    ): MutableSet<Hjemmel> {
        val hjemmelSet = mutableSetOf<Hjemmel>()

        ytelserToAdd.forEach { ytelse ->
            ytelseTilHjemler[ytelse]?.let { hjemmelSet.addAll(it) }
        }

        if (ytelserToKeep != null && existingHjemler != null) {
            for (hjemmel in existingHjemler) {
                for (ytelse in ytelserToKeep) {
                    if (ytelseTilHjemler[ytelse]?.contains(hjemmel) == true) {
                        hjemmelSet.add(hjemmel)
                        break
                    }
                }
            }
        }

        return hjemmelSet
    }

    fun getDataOmSaksbehandler(navIdent: String): SaksbehandlerInfo {
        val ansattEnhetForInnloggetSaksbehandler = innloggetAnsattRepository.getEnhetMedYtelserForSaksbehandler()
        val assignedYtelser = getSaksbehandlerAssignedYtelseIdList(navIdent)

        val saksbehandlerInnstillinger = findSaksbehandlerInnstillinger(
            ident = navIdent,
        )

        val rollerForInnloggetSaksbehandler = azureGateway.getRollerForInnloggetSaksbehandler()
        val enheterForInnloggetSaksbehandler = innloggetAnsattRepository.getEnheterMedYtelserForSaksbehandler()
        val valgtEnhet = findValgtEnhet(innloggetAnsattRepository.getInnloggetIdent())

        return SaksbehandlerInfo(
            navIdent = navIdent,
            roller = rollerForInnloggetSaksbehandler,
            enheter = enheterForInnloggetSaksbehandler,
            ansattEnhet = ansattEnhetForInnloggetSaksbehandler,
            valgtEnhet = valgtEnhet,
            saksbehandlerInnstillinger = saksbehandlerInnstillinger,
            tildelteYtelser = assignedYtelser
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
            //Kode 6 skal ikke ha medunderskrivere, og skal ikke kunne tildeles av andre.
            return emptySet()
        }
        if (harBeskyttelsesbehovFortrolig) {
            //Kode 7 skal ikke ha medunderskrivere, og skal ikke kunne tildeles av andre.
            return emptySet()
        }

        return saksbehandlerAccessService.getSaksbehandlerIdentsForYtelse(ytelse)
            .map { it }
            .filter { egenAnsattFilter(fnr = fnr, erEgenAnsatt = erEgenAnsatt, ident = it) }
            .map { Saksbehandler(navIdent = it, navn = getNameForIdent(it).sammensattNavn) }
            .toSet()
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

    private fun getSaksbehandlerAssignedYtelseIdList(saksbehandlerIdent: String): List<Ytelse> {
        return if (saksbehandlerAccessRepository.existsById(saksbehandlerIdent)) {
            val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
            saksbehandlerAccess.ytelser.toList()
        } else emptyList()
    }

    fun cleanupInnstillinger() {
        val existingInnstillinger = innstillingerRepository.findAll()
        existingInnstillinger.forEach { innstilling ->
            logger.debug("Data before cleanup: saksbehandlerident: ${innstilling.saksbehandlerident} ytelser: ${innstilling.ytelser}")
            storeInnstillingerYtelser(
                navIdent = innstilling.saksbehandlerident,
                inputYtelseSet = innstilling.ytelser.split(SEPARATOR).filterNot { it.isBlank() }.map { Ytelse.of(it) }
                    .toSet()
            )
            val resultingInnstilling =
                innstillingerRepository.findBySaksbehandlerident(innstilling.saksbehandlerident)!!
            logger.debug("Data after cleanup: saksbehandlerident: ${resultingInnstilling.saksbehandlerident} ytelser: ${resultingInnstilling.ytelser}")
        }
    }
}
