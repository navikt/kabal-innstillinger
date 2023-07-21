package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.klageenhetTilYtelser
import no.nav.klage.oppgave.api.view.MedunderskrivereForYtelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.api.view.Saksbehandlere
import no.nav.klage.oppgave.api.view.Signature
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
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
    private val egenAnsattService: EgenAnsattService,
    private val tilgangService: TilgangService,
    private val saksbehandlerAccessService: SaksbehandlerAccessService,
    private val roleUtils: RoleUtils,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun getDataOmSaksbehandler(navIdent: String): SaksbehandlerInfo {
        val enhetMedYtelserForSaksbehandler = getEnhetMedYtelserForSaksbehandler(navIdent = navIdent)
        val assignedYtelser = saksbehandlerAccessService.getSaksbehandlerAssignedYtelseIdList(navIdent)

        val saksbehandlerInnstillinger = innstillingerService.findSaksbehandlerInnstillinger(
            ident = navIdent,
        )

        val rollerForSaksbehandler = azureGateway.getRollerForSaksbehandler(navIdent = navIdent)
        val enheterMedYtelserForSaksbehandler = getEnheterMedYtelserForSaksbehandler(navIdent = navIdent)

        return SaksbehandlerInfo(
            navIdent = navIdent,
            roller = rollerForSaksbehandler,
            enheter = enheterMedYtelserForSaksbehandler,
            ansattEnhet = enhetMedYtelserForSaksbehandler,
            saksbehandlerInnstillinger = saksbehandlerInnstillinger,
            tildelteYtelser = assignedYtelser
        )
    }

    fun getMedunderskrivere(ident: String, ytelse: Ytelse, fnr: String?): MedunderskrivereForYtelse {
        return MedunderskrivereForYtelse(
            ytelse = ytelse.id,
            medunderskrivere = getPossibleSaksbehandlereForFnr(
                fnr = fnr!!,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
            ).filter { it.navIdent != ident }
                .sortedBy { it.navn }
        )
    }

    fun getSaksbehandlere(ytelse: Ytelse, fnr: String): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlereForFnr(
                fnr = fnr,
                saksbehandlerIdentList = getSaksbehandlerIdentsForYtelse(ytelse),
            ).sortedBy { it.navn }
        )
    }

    fun getROLList(fnr: String): Saksbehandlere {
        return Saksbehandlere(
            saksbehandlere = getPossibleSaksbehandlereForFnr(
                fnr = fnr,
                saksbehandlerIdentList = getROLIdents(),
            ).filter { it.navIdent != tokenUtil.getCurrentIdent() }
                .sortedBy { it.navn }
        )
    }

    private fun getPossibleSaksbehandlereForFnr(fnr: String, saksbehandlerIdentList: List<String>): Set<Saksbehandler> {
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

        return saksbehandlerIdentList
            .filter {
                try {
                    egenAnsattFilter(fnr = fnr, erEgenAnsatt = erEgenAnsatt, ident = it)
                } catch (e: Exception) {
                    logger.warn("Error when checking egenAnsattFilter for ident $it", e)
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


    private fun egenAnsattFilter(fnr: String, erEgenAnsatt: Boolean, ident: String) =
        if (!erEgenAnsatt) {
            true
        } else {
            tilgangService.harSaksbehandlerTilgangTil(ident = ident, fnr = fnr)
        }


    fun getSignature(navIdent: String): Signature {
        val innstillinger = innstillingerService.findSaksbehandlerInnstillinger(ident = navIdent)

        val name = getNameForIdent(navIdent = navIdent)

        return Signature(
            longName = name.fornavn + " " + name.etternavn,
            generatedShortName = generateShortNameOrNull(fornavn = name.fornavn, etternavn = name.etternavn),
            customLongName = innstillinger.longName,
            customShortName = innstillinger.shortName,
            customJobTitle = innstillinger.jobTitle,
        )
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
        klageenhetTilYtelser.filter { it.key.navn == enhet.enhetId }.flatMap { it.value }

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
}
