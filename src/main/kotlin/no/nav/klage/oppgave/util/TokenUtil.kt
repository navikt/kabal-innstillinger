package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class TokenUtil(
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getSaksbehandlerTokenWithPdlScope(): String {
        val clientProperties = clientConfigurationProperties.registration["pdl-onbehalfof"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getAppAccessTokenWithNomScope(): String {
        val clientProperties = clientConfigurationProperties.registration["nom-maskintilmaskin"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getOnBehalfOfTokenWithKlageLookupScope(): String {
        val clientProperties = clientConfigurationProperties.registration["klage-lookup-onbehalfof"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getAppAccessTokenWithKlageLookupScope(): String {
        val clientProperties = clientConfigurationProperties.registration["klage-lookup-maskintilmaskin"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.access_token!!
    }

    fun getCurrentIdent(): String =
        tokenValidationContextHolder.getTokenValidationContext().getJwtToken(SecurityConfiguration.ISSUER_AAD)
            ?.jwtTokenClaims?.get("NAVident")?.toString()
            ?: throw RuntimeException("Ident not found in token")

    fun getCurrentTokenType(): TokenType {
        val validationContext = runCatching { tokenValidationContextHolder.getTokenValidationContext() }.getOrNull()
        val tokenType = if (validationContext == null) {
            TokenType.UNAUTHENTICATED
        } else {
            val idtype =
                runCatching { validationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD)?.jwtTokenClaims?.get("idtyp") }.getOrNull()
            val navIdent =
                runCatching {
                    validationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD)?.jwtTokenClaims?.get("NAVident")
                }.getOrNull()
            if (idtype != null && idtype == "app") {
                TokenType.CC
            } else if (navIdent != null) {
                TokenType.OBO
            } else {
                TokenType.UNAUTHENTICATED
            }
        }
        return tokenType
    }

    enum class TokenType {
        CC,
        OBO,
        UNAUTHENTICATED,
    }
}
