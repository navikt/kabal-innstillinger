package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeYtelser
import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeYtelser
import no.nav.klage.oppgave.util.TokenUtil
import org.springframework.stereotype.Service

@Service
class InnloggetAnsattRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tokenUtil: TokenUtil,
) {

    fun getEnhetMedYtelserForSaksbehandler(): EnhetMedLovligeYtelser =
        saksbehandlerRepository.getEnhetMedYtelserForSaksbehandler(getInnloggetIdent())

    fun getEnheterMedYtelserForSaksbehandler(): EnheterMedLovligeYtelser =
        saksbehandlerRepository.getEnheterMedYtelserForSaksbehandler(getInnloggetIdent())

    fun getInnloggetIdent() = tokenUtil.getIdent()
}
