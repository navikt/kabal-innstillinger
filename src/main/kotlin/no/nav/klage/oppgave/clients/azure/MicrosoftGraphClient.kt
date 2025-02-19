package no.nav.klage.oppgave.clients.azure

import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.klage.oppgave.util.logErrorResponse
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatusCode
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MicrosoftGraphClient(
    private val microsoftGraphWebClient: WebClient,
    private val tokenUtil: TokenUtil
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()

        private const val userSelect =
            "displayName,givenName,surname,userPrincipalName,streetAddress"

        private const val onPremisesSamAccountNameSelect = "onPremisesSamAccountName"
    }

    @Retryable
    fun getInnloggetSaksbehandler(): AzureUser {
        logger.debug("Fetching data about authenticated user from Microsoft Graph")

        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me")
                    .queryParam("\$select", userSelect)
                    .build()
            }.header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                logErrorResponse(response, ::getInnloggetSaksbehandler.name, secureLogger)
            }
            .bodyToMono<AzureUser>()
            .block().let { secureLogger.debug("me: {}", it); it }
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.NAV_IDENT_TO_AZURE_USER_CACHE)
    fun getSaksbehandler(navIdent: String): AzureUser {
        logger.debug("Fetching data about authenticated user from Microsoft Graph")
        return findUserByNavIdent(navIdent)
    }

    private fun findUserByNavIdent(navIdent: String): AzureUser {
        logger.debug("findUserByNavIdent $navIdent")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/users")
                    .queryParam("\$filter", "onPremisesSamAccountName eq '$navIdent'")
                    .queryParam("\$select", userSelect)
                    .queryParam("\$count", true)
                    .build()
            }
            .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .header("ConsistencyLevel", "eventual")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                logErrorResponse(response, ::findUserByNavIdent.name, secureLogger)
            }
            .bodyToMono<AzureUserList>().block()?.value?.firstOrNull()
            ?.let { secureLogger.debug("Saksbehandler: {}", it); it }
            ?: throw RuntimeException("AzureAD data about user by nav ident could not be fetched")
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.GROUPMEMBERS_CACHE)
    fun getGroupMembersNavIdents(groupid: String): List<String> {
        val azureGroupMember: List<AzureOnPremisesSamAccountName> = microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/groups/{groupid}/members")
                    .queryParam("\$select", onPremisesSamAccountNameSelect)
                    .build(groupid)
            }
            .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                logErrorResponse(response, ::getGroupMembersNavIdents.name, secureLogger)
            }
            .bodyToMono<AzureOnPremisesSamAccountNameList>().block()?.value
            ?: throw RuntimeException("AzureAD data about group members nav idents could not be fetched")
        return azureGroupMember.map { secureLogger.debug("Group member {}", it); it }
            .mapNotNull { it.onPremisesSamAccountName }
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.NAV_IDENT_TO_AZURE_GROUP_LIST_CACHE)
    fun getSaksbehandlersGroups(navIdent: String): List<AzureGroup> {
        logger.debug("Fetching data about users groups from Microsoft Graph")
        val user = getSaksbehandler(navIdent)
        return getGroupsByUserPrincipalName(user.userPrincipalName)
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.ANSATTE_I_ENHET_CACHE)
    fun getEnhetensAnsattesNavIdents(enhetNr: String): List<String> {
        logger.debug("getEnhetensAnsattesNavIdents from Microsoft Graph")
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/users")
                    .queryParam("\$filter", "streetAddress eq '$enhetNr'")
                    .queryParam("\$count", true)
                    .queryParam("\$top", 500)
                    .queryParam("\$select", "userPrincipalName,onPremisesSamAccountName,displayName")
                    .build()
            }
            .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .header("ConsistencyLevel", "eventual")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                logErrorResponse(response, ::getEnhetensAnsattesNavIdents.name, secureLogger)
            }
            .bodyToMono<AzureSlimUserList>()
            .block()
            .let { userList -> userList?.value?.map { it.onPremisesSamAccountName } }
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }

    private fun getGroupsByUserPrincipalName(userPrincipalName: String): List<AzureGroup> {
        logger.debug("getGroupsByUserPrincipalName $userPrincipalName")
        val aadAzureGroups: List<AzureGroup> = microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/users/{userPrincipalName}/memberOf")
                    .queryParam("\$top", 500)
                    .build(userPrincipalName)
            }
            .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                logErrorResponse(response, ::getGroupsByUserPrincipalName.name, secureLogger)
            }
            .bodyToMono<AzureGroupList>().block()?.value?.map { secureLogger.debug("AD Gruppe by navident: {}", it); it }
            ?: throw RuntimeException("AzureAD data about groups by user principal name could not be fetched")
        return aadAzureGroups
    }
}