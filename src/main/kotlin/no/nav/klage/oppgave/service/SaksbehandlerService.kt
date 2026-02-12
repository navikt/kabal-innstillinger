package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.klageenhetToYtelser
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.view.MedunderskrivereForYtelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.api.view.Saksbehandlere
import no.nav.klage.oppgave.api.view.Signature
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.saksbehandler.*
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.util.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SaksbehandlerService(
    private val innstillingerService: InnstillingerService,
    private val azureGateway: AzureGateway,
    private val pdlFacade: PdlFacade,
    private val tilgangService: TilgangService,
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val roleUtils: RoleUtils,
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
            navn = azureGateway.getDataOmSaksbehandler(navIdent = navIdent).sammensattNavn,
            roller = azureGateway.getRollerForSaksbehandler(navIdent = navIdent),
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
        sakId: String?,
    ): MedunderskrivereForYtelse {
        return MedunderskrivereForYtelse(
            ytelse = ytelse.id,
            medunderskrivere = getPossibleSaksbehandlereForSak(
                fnr = fnr,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
                isSearchingMedunderskriver = true,
            ).filter { it.navIdent != ident }
                .sortedBy { it.navn }
        )
    }

    fun getSaksbehandlere(ytelse: Ytelse, fnr: String, sakId: String?): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlereForSak(
                fnr = fnr,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
            ).sortedBy { it.navn }
        )
    }

    fun getROLList(fnr: String, ytelse: Ytelse?, sakId: String?): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlereForSak(
                fnr = fnr,
                saksbehandlerIdentList = getROLIdents(),
            ).sortedBy { it.navn }
        )
    }

    private fun getPossibleSaksbehandlereForSak(
        fnr: String,
        saksbehandlerIdentList: List<String>,
        isSearchingMedunderskriver: Boolean = false
    ): Set<Saksbehandler> {
        val personInfo = pdlFacade.getPersonInfo(fnr)
        val harBeskyttelsesbehovFortrolig = personInfo.harBeskyttelsesbehovFortrolig()
        val harBeskyttelsesbehovStrengtFortrolig = personInfo.harBeskyttelsesbehovStrengtFortrolig()

        if (isSearchingMedunderskriver && harBeskyttelsesbehovStrengtFortrolig) {
            //Kode 6 skal ikke ha medunderskrivere, og skal ikke kunne tildeles av andre.
            return emptySet()
        }
        if (isSearchingMedunderskriver && harBeskyttelsesbehovFortrolig) {
            //Kode 7 skal ikke ha medunderskrivere, og skal ikke kunne tildeles av andre.
            return emptySet()
        }

        return saksbehandlerIdentList
            .filter {
                try {
                    tilgangService.harSaksbehandlerTilgangTil(ident = it, fnr = fnr)
                } catch (e: Exception) {
                    logger.warn("Error when checking harSaksbehandlerTilgangTil for ident $it", e)
                    false
                }
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
        val roleList = azureGateway.getRollerForSaksbehandler(navIdent = navIdent)
        return roleUtils.roleListContainsROL(roleList)
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
        return azureGateway.getGroupMembersNavIdents(roleUtils.getROLRoleId())
    }

    private fun getNameForIdent(navIdent: String): SaksbehandlerName {
        val saksbehandlerPersonligInfo = azureGateway.getDataOmSaksbehandler(navIdent)
        return SaksbehandlerName(
            fornavn = saksbehandlerPersonligInfo.fornavn,
            etternavn = saksbehandlerPersonligInfo.etternavn,
            sammensattNavn = saksbehandlerPersonligInfo.sammensattNavn,
        )
    }

    private fun getEnhetMedYtelserForSaksbehandler(navIdent: String): EnhetMedLovligeYtelser =
        azureGateway.getDataOmSaksbehandler(navIdent = navIdent).enhet.let {
            EnhetMedLovligeYtelser(
                enhet = it,
                ytelser = getYtelserForEnhet(it)
            )
        }

    private fun getYtelserForEnhet(enhet: Enhet): List<Ytelse> =
        klageenhetToYtelser.filter { it.key.navn == enhet.enhetId }.flatMap { it.value }

    private fun getEnheterMedYtelserForSaksbehandler(navIdent: String): EnheterMedLovligeYtelser =
        listOf(azureGateway.getDataOmSaksbehandler(navIdent = navIdent).enhet).berikMedYtelser()

    private fun List<Enhet>.berikMedYtelser(): EnheterMedLovligeYtelser {
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
