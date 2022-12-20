package no.nav.klage.oppgave.clients.azure

import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.cache.annotation.Cacheable
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
            "onPremisesSamAccountName,displayName,givenName,surname,mail,officeLocation,userPrincipalName,id,jobTitle,streetAddress"

        private const val groupMemberSelect = "id,mail,onPremisesSamAccountName,displayName"
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
            .bodyToMono<AzureUser>()
            .block().let { secureLogger.debug("me: $it"); it }
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.NAV_IDENT_TO_AZURE_USER_CACHE)
    fun getSaksbehandler(navIdent: String): AzureUser {
        logger.debug("Fetching data about authenticated user from Microsoft Graph")
        return findUserByNavIdent(navIdent)
    }

    @Retryable
    fun getInnloggetSaksbehandlersGroups(): List<AzureGroup> {
        logger.debug("Fetching data about authenticated users groups from Microsoft Graph")

        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me/memberOf")
                    .build()
            }.header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .retrieve()
            .bodyToMono<AzureGroupList>()
            .block()?.value?.map { secureLogger.debug("AD Gruppe: $it"); it }
            ?: throw RuntimeException("AzureAD data about authenticated users groups could not be fetched")
    }

    private fun findUserByNavIdent(navIdent: String): AzureUser = microsoftGraphWebClient.get()
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
        .bodyToMono<AzureUserList>().block()?.value?.firstOrNull()?.let { secureLogger.debug("Saksbehandler: $it"); it }
        ?: throw RuntimeException("AzureAD data about user by nav ident could not be fetched")

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.GROUPMEMBERS_CACHE)
    fun getGroupMembersNavIdents(groupid: String): List<String> {
        val azureGroupMember: List<AzureGroupMember> = microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/groups/{groupid}/members")
                    .queryParam("\$select", groupMemberSelect)
                    .build(groupid)
            }
            .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .retrieve()
            .bodyToMono<AzureGroupMemberList>().block()?.value
            ?: throw RuntimeException("AzureAD data about group members nav idents could not be fetched")
        return azureGroupMember.map { secureLogger.debug("Group member $it"); it }
            .mapNotNull { it.onPremisesSamAccountName }
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.NAV_IDENT_TO_AZURE_GROUP_LIST_CACHE)
    fun getSaksbehandlersGroups(navIdent: String): List<AzureGroup> {
        logger.debug("Fetching data about users groups from Microsoft Graph")
        val user = findUserByNavIdent(navIdent)
        return getGroupsByUserPrincipalName(user.userPrincipalName)
    }

    @Retryable
    @Cacheable(CacheWithJCacheConfiguration.ANSATTE_I_ENHET_CACHE)
    fun getEnhetensAnsattesNavIdents(enhetNr: String): List<String> {
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
            .bodyToMono<AzureSlimUserList>()
            .block()
            .let { userList -> userList?.value?.map { it.onPremisesSamAccountName } }
            ?: throw RuntimeException("AzureAD data about authenticated user could not be fetched")
    }

    private fun getGroupsByUserPrincipalName(userPrincipalName: String): List<AzureGroup> {
        val aadAzureGroups: List<AzureGroup> = microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/users/{userPrincipalName}/memberOf")
                    .build(userPrincipalName)
            }
            .header("Authorization", "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithGraphScope()}")
            .retrieve()
            .bodyToMono<AzureGroupList>().block()?.value?.map { secureLogger.debug("AD Gruppe by navident: $it"); it }
            ?: throw RuntimeException("AzureAD data about groups by user principal name could not be fetched")
        return aadAzureGroups
    }


}