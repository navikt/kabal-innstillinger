package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.klageenhetToYtelser
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.view.MedunderskrivereForYtelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.api.view.Saksbehandlere
import no.nav.klage.oppgave.api.view.Signature
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.clients.klagelookup.Sak
import no.nav.klage.oppgave.domain.saksbehandler.*
import no.nav.klage.oppgave.util.generateShortNameOrNull
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SaksbehandlerService(
    private val innstillingerService: InnstillingerService,
    private val tilgangService: TilgangService,
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val klageLookupGateway: KlageLookupGateway,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getDataOmSaksbehandler(navIdent: String): SaksbehandlerInfo {
        val enhetMedYtelserForSaksbehandler = getEnhetMedYtelserForSaksbehandler(navIdent = navIdent)
        val assignedYtelser = saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(navIdent)

        val saksbehandlerInnstillinger = innstillingerService.findSaksbehandlerInnstillinger(
            ident = navIdent,
        )

        val enheterMedYtelserForSaksbehandler = getEnheterMedYtelserForSaksbehandler(navIdent = navIdent)

        return SaksbehandlerInfo(
            navIdent = navIdent,
            navn = klageLookupGateway.getUserInfoForGivenNavIdent(navIdent = navIdent).sammensattNavn,
            roller = klageLookupGateway.getGroupsForGivenNavIdent(navIdent = navIdent).groups.map { it.id },
            enheter = enheterMedYtelserForSaksbehandler,
            ansattEnhet = enhetMedYtelserForSaksbehandler,
            saksbehandlerInnstillinger = saksbehandlerInnstillinger,
            tildelteYtelser = assignedYtelser
        )
    }

    fun getMedunderskrivere(
        ident: String,
        ytelse: Ytelse,
        fnr: String,
        sakId: String,
        fagsystem: Fagsystem,
    ): MedunderskrivereForYtelse {
        return MedunderskrivereForYtelse(
            ytelse = ytelse.id,
            medunderskrivere = getPossibleSaksbehandlere(
                fnr = fnr,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
                isSearchingMedunderskriverOrRol = true,
                sakId = sakId,
                ytelse = ytelse,
                fagsystem = fagsystem,
                requestsROLs = false,
            ).filter { it.navIdent != ident }
                .sortedBy { it.navn }
        )
    }

    fun getSaksbehandlere(
        fnr: String,
        ytelse: Ytelse,
        sakId: String,
        fagsystem: Fagsystem,
    ): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlere(
                fnr = fnr,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
                sakId = sakId,
                ytelse = ytelse,
                fagsystem = fagsystem,
                requestsROLs = false,
            ).sortedBy { it.navn }
        )
    }

    fun getSaksbehandlereForBruker(
        fnr: String,
        ytelse: Ytelse,
    ): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlere(
                fnr = fnr,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
                ytelse = ytelse,
                sakId = null,
                fagsystem = null,
                requestsROLs = false,
            ).sortedBy { it.navn }
        )
    }

    fun getROLList(
        fnr: String,
        ytelse: Ytelse,
        sakId: String,
        fagsystem: Fagsystem,
    ): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlere(
                fnr = fnr,
                saksbehandlerIdentList = getROLIdents(),
                isSearchingMedunderskriverOrRol = true,
                sakId = sakId,
                ytelse = ytelse,
                fagsystem = fagsystem,
                requestsROLs = true,
            ).sortedBy { it.navn }
        )
    }

    private fun getPossibleSaksbehandlere(
        fnr: String,
        saksbehandlerIdentList: List<String>,
        isSearchingMedunderskriverOrRol: Boolean = false,
        ytelse: Ytelse,
        sakId: String?,
        fagsystem: Fagsystem?,
        requestsROLs: Boolean,
    ): Set<Saksbehandler> {
        val sak = if (sakId != null && fagsystem != null) {
            Sak(
                sakId = sakId,
                ytelse = ytelse,
                fagsystem = fagsystem,
            )
        } else null
        val personInfo = klageLookupGateway.getPerson(fnr = fnr, sak = sak)
        val harBeskyttelsesbehovFortrolig = personInfo.personOrFamilyIsFortrolig()
        val harBeskyttelsesbehovStrengtFortrolig = personInfo.personOrFamilyIsStrengtFortrolig()

        if (isSearchingMedunderskriverOrRol && harBeskyttelsesbehovStrengtFortrolig) {
            //Kode 6 skal ikke ha medunderskrivere, og skal ikke kunne tildeles av andre.
            return emptySet()
        }
        if (isSearchingMedunderskriverOrRol && harBeskyttelsesbehovFortrolig) {
            //Kode 7 skal ikke ha medunderskrivere, og skal ikke kunne tildeles av andre.
            return emptySet()
        }

        val saksbehandlerGroups = klageLookupGateway.getUserGroupsBatched(navIdentList = saksbehandlerIdentList)

        return saksbehandlerIdentList
            .filter { currentNavIdent ->
                saksbehandlerGroups.find { it.navIdent == currentNavIdent }?.groupIds?.contains(if (requestsROLs) AzureGroup.KABAL_ROL.id else AzureGroup.KABAL_SAKSBEHANDLING.id)
                    ?: false
            }
            .filter {
                tilgangService.hasSaksbehandlerAccessToSak(
                    fnr = fnr,
                    navIdent = it,
                    sakId = sakId,
                    ytelse = ytelse,
                    fagsystem = fagsystem,
                )
            }
            .mapNotNull {
                try {
                    Saksbehandler(navIdent = it, navn = getNameForIdent(it).sammensattNavn)
                } catch (e: Exception) {
                    logger.warn("Error when getting name for ident $it", e)
                    null
                }
            }
            .toSet()
    }

    fun getSignature(navIdent: String): Signature {
        val innstillinger = innstillingerService.findSaksbehandlerInnstillinger(ident = navIdent)

        val name = try {
            getNameForIdent(navIdent = navIdent)
        } catch (e: Exception) {
            return Signature(
                longName = "$navIdent - Ugyldig ident",
                generatedShortName = "$navIdent - Ugyldig ident",
                customLongName = "$navIdent - Ugyldig ident",
                customShortName = "$navIdent - Ugyldig ident",
                customJobTitle = null,
                anonymous = false,
            )
        }

        return Signature(
            longName = name.fornavn + " " + name.etternavn,
            generatedShortName = generateShortNameOrNull(fornavn = name.fornavn, etternavn = name.etternavn),
            customLongName = innstillinger.longName,
            customShortName = innstillinger.shortName,
            customJobTitle = if (saksbehandlerIsROL(navIdent)) "rådgivende overlege" else innstillinger.jobTitle,
            anonymous = innstillinger.anonymous,
        )
    }

    private fun saksbehandlerIsROL(navIdent: String): Boolean {
        val roleList = klageLookupGateway.getGroupsForGivenNavIdent(navIdent = navIdent)
        return roleList.groups.contains(AzureGroup.KABAL_ROL)
    }

    private fun getSaksbehandlerIdentsForYtelse(ytelse: Ytelse): List<String> {
        logger.debug("Getting saksbehandlere for ytelse {}", ytelse)
        val results = saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(ytelse)
        return results.map {
            it.saksbehandlerIdent
        }
    }

    private fun getROLIdents(): List<String> {
        logger.debug("Getting ROL list")
        return klageLookupGateway.getUsersInGroup(AzureGroup.KABAL_ROL).map { it.navIdent }
    }

    private fun getNameForIdent(navIdent: String): SaksbehandlerName {
        val saksbehandlerPersonligInfo = klageLookupGateway.getUserInfoForGivenNavIdent(navIdent)
        return SaksbehandlerName(
            fornavn = saksbehandlerPersonligInfo.fornavn,
            etternavn = saksbehandlerPersonligInfo.etternavn,
            sammensattNavn = saksbehandlerPersonligInfo.sammensattNavn,
        )
    }

    private fun getEnhetMedYtelserForSaksbehandler(navIdent: String): EnhetMedLovligeYtelser =
        klageLookupGateway.getUserInfoForGivenNavIdent(navIdent = navIdent).enhet.let { saksbehandlerEnhet ->
            EnhetMedLovligeYtelser(
                enhet = saksbehandlerEnhet,
                ytelser = getYtelserForEnhet(enhet = saksbehandlerEnhet)
            )
        }

    private fun getYtelserForEnhet(enhet: SaksbehandlerEnhet): List<Ytelse> =
        klageenhetToYtelser.filter { it.key.navn == enhet.enhetId }.flatMap { it.value }

    private fun getEnheterMedYtelserForSaksbehandler(navIdent: String): EnheterMedLovligeYtelser =
        listOf(klageLookupGateway.getUserInfoForGivenNavIdent(navIdent = navIdent).enhet).berikMedYtelser()

    private fun List<SaksbehandlerEnhet>.berikMedYtelser(): EnheterMedLovligeYtelser {
        return EnheterMedLovligeYtelser(this.map {
            EnhetMedLovligeYtelser(
                enhet = it,
                ytelser = getYtelserForEnhet(it)
            )
        })
    }

    fun storeInnstillingerButKeepSignature(
        navIdent: String,
        newSaksbehandlerInnstillinger: SaksbehandlerInnstillinger
    ): SaksbehandlerInnstillinger {
        return innstillingerService.storeInnstillingerButKeepSignature(
            navIdent = navIdent,
            newSaksbehandlerInnstillinger = newSaksbehandlerInnstillinger,
            assignedYtelseSet = saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(navIdent)
        )
    }
}
