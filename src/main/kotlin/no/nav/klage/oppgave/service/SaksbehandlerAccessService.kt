package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess
import no.nav.klage.oppgave.repositories.EnhetRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
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

    fun getSaksbehandlerAccess(saksbehandlerIdent: String): SaksbehandlerAccess {
        val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
        return SaksbehandlerAccess(
            saksbehandlerIdent = saksbehandlerAccess.saksbehandlerident,
            saksbehandlerName = saksbehandlerService.getNameForIdent(saksbehandlerIdent).sammensattNavn,
            ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
            created = saksbehandlerAccess.created,
            accessRightsModified = saksbehandlerAccess.accessRightsModified,
        )
    }

    //TODO: all, or just the ones with SaksbehandlerAccess? Connected to "default values".
    fun getSaksbehandlere(enhet: String): List<SaksbehandlerAccess> {
        return enhetRepository.getAnsatteIEnhet(enhet)
            .filter { saksbehandlerRepository.erSaksbehandler(it) }
            .map { ident ->
                val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(ident)
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerAccess.saksbehandlerident,
                    saksbehandlerName = saksbehandlerService.getNameForIdent(ident).sammensattNavn,
                    ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
                    created = saksbehandlerAccess.created,
                    accessRightsModified = saksbehandlerAccess.accessRightsModified,
                )
            }
    }

    fun addYtelser(
        saksbehandleridentList: List<String>,
        ytelseIdList: List<String>,
        innloggetAnsattIdent: String
    ): List<SaksbehandlerAccess> {
        logger.debug("addYtelser for saksbehandlere {} with ytelser {}", saksbehandleridentList, ytelseIdList)

        val now = LocalDateTime.now()
        val saksbehandlerAccessList = mutableListOf<SaksbehandlerAccess>()
        val ytelseSet = ytelseIdList.map { Ytelse.of(it) }.toSet()

        saksbehandleridentList.forEach { saksbehandlerident ->
            val saksbehandlerAccess = if (!saksbehandlerAccessRepository.existsById(saksbehandlerident)) {
                saksbehandlerAccessRepository.save(
                    SaksbehandlerAccessEntity(
                        saksbehandlerident = saksbehandlerident,
                        modifiedBy = innloggetAnsattIdent,
                        ytelser = ytelseSet,
                        created = now,
                        accessRightsModified = now,
                    )
                )
            } else {
                saksbehandlerAccessRepository.getReferenceById(saksbehandlerident).apply {
                    modifiedBy = innloggetAnsattIdent
                    ytelser = ytelser + ytelseSet
                    accessRightsModified = LocalDateTime.now()
                }
            }

            saksbehandlerAccessList += SaksbehandlerAccess(
                saksbehandlerIdent = saksbehandlerAccess.saksbehandlerident,
                saksbehandlerName = saksbehandlerService.getNameForIdent(saksbehandlerident).sammensattNavn,
                ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
                created = saksbehandlerAccess.created,
                accessRightsModified = saksbehandlerAccess.accessRightsModified,
            )
        }
        return saksbehandlerAccessList
    }

    fun removeYtelser(
        saksbehandleridentList: List<String>,
        ytelseIdList: List<String>,
        innloggetAnsattIdent: String
    ): List<SaksbehandlerAccess> {
        logger.debug("removeYtelser for saksbehandlere {} with ytelser {}", saksbehandleridentList, ytelseIdList)

        val now = LocalDateTime.now()
        val modifiedSaksbehandlerAccessList = mutableListOf<SaksbehandlerAccess>()
        val ytelseSet = ytelseIdList.map { Ytelse.of(it) }.toSet()

        saksbehandleridentList.forEach { saksbehandlerident ->
            val saksbehandlerAccess = if (!saksbehandlerAccessRepository.existsById(saksbehandlerident)) {
                //Should we create here?
                saksbehandlerAccessRepository.save(
                    SaksbehandlerAccessEntity(
                        saksbehandlerident = saksbehandlerident,
                        modifiedBy = innloggetAnsattIdent,
                        created = now,
                        accessRightsModified = now,
                    )
                )
            } else {
                saksbehandlerAccessRepository.getReferenceById(saksbehandlerident).apply {
                    modifiedBy = innloggetAnsattIdent
                    ytelser = ytelser - ytelseSet
                    accessRightsModified = LocalDateTime.now()
                }
            }

            modifiedSaksbehandlerAccessList += SaksbehandlerAccess(
                saksbehandlerIdent = saksbehandlerAccess.saksbehandlerident,
                saksbehandlerName = saksbehandlerService.getNameForIdent(saksbehandlerident).sammensattNavn,
                ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
                created = saksbehandlerAccess.created,
                accessRightsModified = saksbehandlerAccess.accessRightsModified,
            )
        }
        return modifiedSaksbehandlerAccessList
    }

}