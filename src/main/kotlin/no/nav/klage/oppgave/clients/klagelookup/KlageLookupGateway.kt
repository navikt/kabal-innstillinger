package no.nav.klage.oppgave.clients.klagelookup

import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerEnhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerGroups
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerSluttdato
import no.nav.klage.oppgave.service.TilgangService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class KlageLookupGateway(
    private val klageLookupClient: KlageLookupClient,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getUserInfoForGivenNavIdent(navIdent: String): SaksbehandlerPersonligInfo {
        logger.debug("Getting user info for $navIdent from KlageLookup")
        val data = klageLookupClient.getUserInfo(navIdent = navIdent)
        return data.toSaksbehandlerPersonligInfo()
    }

    fun getUserInfoForNavIdentList(navIdentList: List<String>): List<SaksbehandlerPersonligInfo> {
        logger.debug("Getting user info for $navIdentList from KlageLookup")
        val data = klageLookupClient.getUserInfoBatched(navIdentList = navIdentList)
        if (data.misses.isNotEmpty()) {
            logger.warn("Did not find user info for ${data.misses} from KlageLookup")
        }
        return data.hits.map { it.toSaksbehandlerPersonligInfo() }
    }

    fun getUserGroupsBatched(navIdentList: List<String>): List<BatchedGroupsHitResponse> {
        logger.debug("Getting batched user info for $navIdentList from KlageLookup")
        val data = klageLookupClient.getUserGroupsBatched(navIdentList = navIdentList)
        return data.hits
    }

    fun getGroupsForGivenNavIdent(navIdent: String): SaksbehandlerGroups {
        logger.debug("Getting group memberships for $navIdent from KlageLookup")
        val data = klageLookupClient.getUserGroups(navIdent = navIdent)
        return data.toSaksbehandlerGroups()
    }

    fun getUsersInGroup(azureGroup: AzureGroup): List<UserResponse> {
        logger.debug("Getting users in group $azureGroup from KlageLookup")
        val data = klageLookupClient.getUsersInGroup(azureGroup = azureGroup)
        return data.users
    }

    fun getUsersInEnhet(enhetsnummer: String): List<UserResponse> {
        logger.debug("Getting users in enhet $enhetsnummer from KlageLookup")
        val data = klageLookupClient.getUsersInEnhet(enhetsnummer = enhetsnummer)
        return data.users
    }

    fun getPerson(fnr: String, sak: Sak?): PersonResponse {
        return klageLookupClient.getPerson(fnr = fnr, sak = sak)
    }

    fun getSluttdatoForGivenNavIdent(navIdent: String): SaksbehandlerSluttdato {
        logger.debug("Getting sluttdato for $navIdent from KlageLookup")
        val data = klageLookupClient.getUserSluttdato(navIdent = navIdent)
        return data.toSaksbehandlerSluttdato()
    }

    fun getSluttdatoForNavIdentList(navIdentList: List<String>): List<SaksbehandlerSluttdato> {
        logger.debug("Getting sluttdato for $navIdentList from KlageLookup")
        val data = klageLookupClient.getSluttdatoBatched(navIdentList = navIdentList)
        return data.toSaksbehandlerSluttdatoList()
    }

    fun getAccess(
        /** fnr, dnr or aktorId */
        brukerId: String,
        navIdent: String,
        ytelse: Ytelse,
        sakId: String?,
        fagsystem: Fagsystem?,
    ): TilgangService.Access {
        return klageLookupClient.getAccess(
            brukerId = brukerId,
            navIdent = navIdent,
            sakId = sakId,
            ytelse = ytelse,
            fagsystem = fagsystem,
        )
    }

    fun ExtendedUserResponse.toSaksbehandlerPersonligInfo(): SaksbehandlerPersonligInfo {
        return SaksbehandlerPersonligInfo(
            navIdent = this.navIdent,
            fornavn = this.fornavn,
            etternavn = this.etternavn,
            sammensattNavn = this.sammensattNavn,
            enhet = SaksbehandlerEnhet(
                enhetId = this.enhet.enhetNr,
                navn = this.enhet.enhetNavn,
            )
        )
    }

    fun GroupsResponse.toSaksbehandlerGroups(): SaksbehandlerGroups {
        return SaksbehandlerGroups(
            groups = this.groupIds.map { AzureGroup.of(it) }
        )
    }

    private fun SluttdatoResponse.toSaksbehandlerSluttdato(): SaksbehandlerSluttdato {
        return SaksbehandlerSluttdato(
            navIdent = this.navIdent,
            sluttdato = this.sluttdato,
        )
    }

    private fun BatchedSluttdatoResponse.toSaksbehandlerSluttdatoList(): List<SaksbehandlerSluttdato> {
        val resultList = mutableListOf<SaksbehandlerSluttdato>()
        hits.forEach {
            resultList.add(
                SaksbehandlerSluttdato(
                    navIdent = it.navIdent,
                    sluttdato = it.sluttdato,
                )
            )
        }
        misses.forEach {
            resultList.add(
                SaksbehandlerSluttdato(
                    navIdent = it,
                    sluttdato = null,
                )
            )
        }
        return resultList
    }
}