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
        private val securelogger = getSecureLogger()
    }

    fun getSaksbehandlerAccessTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["azure-onbehalfof"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken!!
    }

    fun getSaksbehandlerTokenWithPdlScope(): String {
        val clientProperties = clientConfigurationProperties.registration["pdl-onbehalfof"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken!!
    }

    fun getAppAccessTokenWithNomScope(): String {
        val clientProperties = clientConfigurationProperties.registration["nom-maskintilmaskin"]!!
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken!!
    }

    fun getCurrentIdent(): String =
        tokenValidationContextHolder.getTokenValidationContext().getJwtToken(SecurityConfiguration.ISSUER_AAD)
            ?.jwtTokenClaims?.get("NAVident")?.toString()
            ?: throw RuntimeException("Ident not found in token")

    fun getRoleIdsFromToken(): List<String> =
        tokenValidationContextHolder.getTokenValidationContext().getJwtToken(SecurityConfiguration.ISSUER_AAD)
            ?.jwtTokenClaims?.getAsList("groups").orEmpty().toList()
}
