package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.SaksbehandlerAccessResponse
import no.nav.klage.oppgave.api.view.YtelseInput
import no.nav.klage.oppgave.repositories.EnhetRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess as SaksbehandlerAccessView
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess as SaksbehandlerAccessEntity

@Service
@Transactional
class SaksbehandlerAccessService(
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository,
    private val enhetRepository: EnhetRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun getSaksbehandlerAccessView(saksbehandlerIdent: String): SaksbehandlerAccessView {
        val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
        return SaksbehandlerAccessView(
            saksbehandlerIdent = saksbehandlerAccess.saksbehandlerIdent,
            saksbehandlerName = saksbehandlerService.getNameForIdent(saksbehandlerIdent).sammensattNavn,
            ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
            created = saksbehandlerAccess.created,
            accessRightsModified = saksbehandlerAccess.accessRightsModified,
        )
    }

    fun getSaksbehandlere(enhet: String): SaksbehandlerAccessResponse {
        return SaksbehandlerAccessResponse(accessRights = enhetRepository.getAnsatteIEnhet(enhet)
            .filter { saksbehandlerRepository.erSaksbehandler(it) }
            .map { ident ->
                if (saksbehandlerAccessRepository.existsById(ident)) {
                    getSaksbehandlerAccessView(ident)
                } else {
                    SaksbehandlerAccessView(
                        saksbehandlerIdent = ident,
                        saksbehandlerName = saksbehandlerService.getNameForIdent(ident).sammensattNavn,
                        ytelseIdList = emptyList(),
                        created = null,
                        accessRightsModified = null,
                    )
                }
            })
    }

    fun setYtelser(
        ytelseInput: YtelseInput,
        innloggetAnsattIdent: String
    ): SaksbehandlerAccessResponse {
        logger.debug("setYtelser for saksbehandlere with ytelser {}", ytelseInput)

        val now = LocalDateTime.now()
        val saksbehandlerAccessList = mutableListOf<SaksbehandlerAccessView>()

        ytelseInput.accessRights.forEach { accessRight ->
            val ytelseSet = accessRight.ytelseIdList.map { Ytelse.of(it) }.toSet()
            val saksbehandlerAccess = if (!saksbehandlerAccessRepository.existsById(accessRight.saksbehandlerIdent)) {
                saksbehandlerAccessRepository.save(
                    SaksbehandlerAccessEntity(
                        saksbehandlerIdent = accessRight.saksbehandlerIdent,
                        modifiedBy = innloggetAnsattIdent,
                        ytelser = ytelseSet,
                        created = now,
                        accessRightsModified = now,
                    )
                )
            } else {
                saksbehandlerAccessRepository.getReferenceById(accessRight.saksbehandlerIdent).apply {
                    if (ytelser != ytelseSet) {
                        ytelser = ytelseSet
                        modifiedBy = innloggetAnsattIdent
                        accessRightsModified = now
                    } else {
                        logger.debug("No changes for saksbehandler {}", accessRight.saksbehandlerIdent)
                    }
                }
            }

            saksbehandlerAccessList += SaksbehandlerAccessView(
                saksbehandlerIdent = saksbehandlerAccess.saksbehandlerIdent,
                saksbehandlerName = saksbehandlerService.getNameForIdent(saksbehandlerAccess.saksbehandlerIdent).sammensattNavn,
                ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
                created = saksbehandlerAccess.created,
                accessRightsModified = saksbehandlerAccess.accessRightsModified,
            )
        }
        return SaksbehandlerAccessResponse(accessRights = saksbehandlerAccessList)
    }

    fun getSaksbehandlerIdentsForYtelse(ytelse: Ytelse): List<String> {
        logger.debug("Getting saksbehandlere for ytelse $ytelse")
        val results = saksbehandlerAccessRepository.findAllByYtelserContaining(ytelse)
        return results.map {
            it.saksbehandlerIdent
        }
    }
}