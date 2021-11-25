package no.nav.klage.oppgave.service

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
}
