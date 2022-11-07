package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SaksbehandlerAccessService(
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun getSaksbehandlerAccess(saksbehandlerIdent: String): SaksbehandlerAccess {
        val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
        return SaksbehandlerAccess(
            saksbehandlerIdent = saksbehandlerAccess.saksbehandlerident,
            ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
            created = saksbehandlerAccess.created,
            accessRightsModified = saksbehandlerAccess.accessRightsModified,
        )
    }

    fun setYtelser(saksbehandlerIdent: String, ytelseIdList: List<String>): SaksbehandlerAccess {
        logger.debug("setYtelser for saksbehandler {} with ytelser {}", saksbehandlerIdent, ytelseIdList)

        val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
        saksbehandlerAccess.ytelser = ytelseIdList.map { Ytelse.of(it) }.toSet()
        saksbehandlerAccess.accessRightsModified = LocalDateTime.now()
        return SaksbehandlerAccess(
            saksbehandlerIdent = saksbehandlerAccess.saksbehandlerident,
            ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
            created = saksbehandlerAccess.created,
            accessRightsModified = saksbehandlerAccess.accessRightsModified,
        )
    }

}