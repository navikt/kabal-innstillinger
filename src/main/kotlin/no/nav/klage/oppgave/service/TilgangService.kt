package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun harSaksbehandlerTilgangTil(ident: String, fnr: String): Boolean {
        return verifiserTilgangTilPersonForSaksbehandler(
            fnr = fnr,
            ident = ident,
            kanBehandleStrengtFortrolig = { saksbehandlerRepository.kanBehandleStrengtFortrolig(ident) },
            kanBehandleFortrolig = { saksbehandlerRepository.kanBehandleFortrolig(ident) },
            kanBehandleEgenAnsatt = { saksbehandlerRepository.kanBehandleEgenAnsatt(ident) },
        )
    }

    private fun verifiserTilgangTilPersonForSaksbehandler(
        fnr: String,
        ident: String,
        kanBehandleStrengtFortrolig: () -> Boolean,
        kanBehandleFortrolig: () -> Boolean,
        kanBehandleEgenAnsatt: () -> Boolean
    ): Boolean {
        val personInfo = pdlFacade.getPersonInfo(fnr)
        val harBeskyttelsesbehovFortrolig = personInfo.harBeskyttelsesbehovFortrolig()
        val harBeskyttelsesbehovStrengtFortrolig = personInfo.harBeskyttelsesbehovStrengtFortrolig()
        val erEgenAnsatt = egenAnsattService.erEgenAnsatt(fnr)

        if (harBeskyttelsesbehovStrengtFortrolig) {
            securelogger.info("erStrengtFortrolig")
            //Merk at vi ikke sjekker egenAnsatt her, strengt fortrolig trumfer det
            if (kanBehandleStrengtFortrolig.invoke()) {
                securelogger.info("Access granted to strengt fortrolig for $ident")
            } else {
                securelogger.info("Access denied to strengt fortrolig for $ident")
                return false
            }
        }
        if (harBeskyttelsesbehovFortrolig) {
            securelogger.info("erFortrolig")
            //Merk at vi ikke sjekker egenAnsatt her, fortrolig trumfer det
            if (kanBehandleFortrolig.invoke()) {
                securelogger.info("Access granted to fortrolig for $ident")
            } else {
                securelogger.info("Access denied to fortrolig for $ident")
                return false
            }
        }
        if (erEgenAnsatt && !(harBeskyttelsesbehovFortrolig || harBeskyttelsesbehovStrengtFortrolig)) {
            securelogger.info("erEgenAnsatt")
            //Er kun egenAnsatt, har ikke et beskyttelsesbehov i tillegg
            if (kanBehandleEgenAnsatt.invoke()) {
                securelogger.info("Access granted to egen ansatt for $ident")
            } else {
                securelogger.info("Access denied to egen ansatt for $ident")
                return false
            }
        }
        return true
    }
}