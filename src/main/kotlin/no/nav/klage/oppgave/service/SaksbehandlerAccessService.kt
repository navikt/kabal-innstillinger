package no.nav.klage.oppgave.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.SaksbehandlerAccessResponse
import no.nav.klage.oppgave.api.view.TildelteYtelserResponse
import no.nav.klage.oppgave.api.view.YtelseInput
import no.nav.klage.oppgave.clients.nom.GetAnsattResponse
import no.nav.klage.oppgave.clients.nom.NomClient
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.klage.oppgave.api.view.SaksbehandlerAccess as SaksbehandlerAccessView
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess as SaksbehandlerAccessEntity

@Service
@Transactional
class SaksbehandlerAccessService(
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository,
    private val innstillingerService: InnstillingerService,
    private val roleUtils: RoleUtils,
    private val azureGateway: AzureGateway,
    private val nomClient: NomClient,
) {


    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        @Value("\${DELETE_EXPIRED_DRY_RUN}")
        private var deleteExpiredDryRun: Boolean = true
    }

    fun getSaksbehandlerAccessView(saksbehandlerIdent: String): SaksbehandlerAccessView {
        return if (saksbehandlerAccessRepository.existsById(saksbehandlerIdent)) {
            val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
            SaksbehandlerAccessView(
                saksbehandlerIdent = saksbehandlerAccess.saksbehandlerIdent,
                saksbehandlerName = getSammensattNameForIdent(saksbehandlerIdent),
                ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
                created = saksbehandlerAccess.created,
                accessRightsModified = saksbehandlerAccess.accessRightsModified,
            )
        } else {
            getEmptySaksbehandlerAccess(saksbehandlerIdent)
        }
    }

    fun getSaksbehandlerAccessesInEnhet(enhet: String): SaksbehandlerAccessResponse {
        return SaksbehandlerAccessResponse(accessRights = getAnsatteIEnhet(enhet)
            .filter { roleUtils.isSaksbehandler(ident = it) }
            .map { ident ->
                if (saksbehandlerAccessRepository.existsById(ident)) {
                    getSaksbehandlerAccessView(ident)
                } else {
                    getEmptySaksbehandlerAccess(ident)
                }
            })
    }

    private fun getEmptySaksbehandlerAccess(saksbehandlerIdent: String) = SaksbehandlerAccessView(
        saksbehandlerIdent = saksbehandlerIdent,
        saksbehandlerName = getSammensattNameForIdent(saksbehandlerIdent),
        ytelseIdList = emptyList(),
        created = null,
        accessRightsModified = null,
    )

    fun getTildelteYtelserForEnhet(enhet: String): TildelteYtelserResponse {
        val saksbehandlereAccess = getSaksbehandlerAccessesInEnhet(enhet)
        val ytelseIdUnion = saksbehandlereAccess.accessRights.flatMap { it.ytelseIdList }.toSet()

        return TildelteYtelserResponse(ytelseIdList = ytelseIdUnion.toList())
    }

    fun setYtelserForAnsatt(
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

            innstillingerService.updateYtelseAndHjemmelInnstillinger(
                navIdent = accessRight.saksbehandlerIdent,
                inputYtelseSet = ytelseSet,
                assignedYtelseList = ytelseSet.toList()
            )

            saksbehandlerAccessList += SaksbehandlerAccessView(
                saksbehandlerIdent = saksbehandlerAccess.saksbehandlerIdent,
                saksbehandlerName = getSammensattNameForIdent(saksbehandlerAccess.saksbehandlerIdent),
                ytelseIdList = saksbehandlerAccess.ytelser.map { it.id },
                created = saksbehandlerAccess.created,
                accessRightsModified = saksbehandlerAccess.accessRightsModified,
            )
        }
        return SaksbehandlerAccessResponse(accessRights = saksbehandlerAccessList)
    }

    fun getSaksbehandlerAssignedYtelseList(saksbehandlerIdent: String): List<Ytelse> {
        return if (saksbehandlerAccessRepository.existsById(saksbehandlerIdent)) {
            val saksbehandlerAccess = saksbehandlerAccessRepository.getReferenceById(saksbehandlerIdent)
            saksbehandlerAccess.ytelser.toList()
        } else emptyList()
    }

    fun logAnsattStatusInNom() {
        val allSaksbehandlerAccessEntries = saksbehandlerAccessRepository.findAll()
        secureLogger.debug("Number of saksbehandlerAccess entries: {}", allSaksbehandlerAccessEntries.size)

        allSaksbehandlerAccessEntries.forEach {
            getAnsattInfoFromNom(it.saksbehandlerIdent)
        }

    }

    fun getAnsattInfoFromNom(navIdent: String): GetAnsattResponse {
        val ansatt = nomClient.getAnsatt(navIdent)
        secureLogger.debug(
            ansatt.toString()
        )
        return ansatt
    }

    fun getAllSaksbehandlerAccessesForYtelse(ytelse: Ytelse): List<SaksbehandlerAccess> {
        return saksbehandlerAccessRepository.findAllByYtelserContaining(ytelse)
    }


    private fun getAnsatteIEnhet(enhetId: String): List<String> {
        return azureGateway.getEnhetensAnsattesNavIdents(enhetId)
    }

    private fun getSammensattNameForIdent(navIdent: String): String {
        val saksbehandlerPersonligInfo = azureGateway.getDataOmSaksbehandler(navIdent)
        return saksbehandlerPersonligInfo.sammensattNavn
    }

    @Scheduled(cron = "\${SETTINGS_CLEANUP_CRON}", zone = "Europe/Oslo")
    @SchedulerLock(name = "deleteInnstillingerAndAccessForExpiredSaksbehandlers")
    fun deleteInnstillingerAndAccessForExpiredSaksbehandlers() {
        val allSaksbehandlerAccessEntries = saksbehandlerAccessRepository.findAll()
        secureLogger.debug("Starting scheduled cleanup process. Dryrun: {}", deleteExpiredDryRun)
        secureLogger.debug("Number of saksbehandlerAccess entries: {}", allSaksbehandlerAccessEntries.size)

        allSaksbehandlerAccessEntries.forEach {
            deleteInnstillingerAndAccessIfExpiredSaksbehandler(it.saksbehandlerIdent)
        }
    }


    private fun deleteInnstillingerAndAccessIfExpiredSaksbehandler(navIdent: String) {
        val ansatt = nomClient.getAnsatt(navIdent)
        if (ansatt.data?.ressurs?.sluttdato?.isBefore(LocalDate.now().minusWeeks(1)) == true) {
            secureLogger.debug("Sluttdato is in the past: {}", ansatt.toString())
            deleteSaksbehandler(navIdent)
            innstillingerService.deleteInnstillingerForSaksbehandler(navIdent)
        } else {
            secureLogger.debug("Still valid: {}", ansatt.toString())
        }
    }

    private fun deleteSaksbehandler(navIdent: String) {
        secureLogger.debug("Deleting saksbehandlerAccess for saksbehandler with ident {}", navIdent)
        if (!deleteExpiredDryRun) {
            secureLogger.debug("Actually deleting saksbehandlerAccess for ident {}", navIdent)
//            saksbehandlerAccessRepository.deleteById(navIdent)
        }
    }
}