package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.clients.sts.StsClient
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
    private val stsClient: StsClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun getSaksbehandlerAccessTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["azure-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getSaksbehandlerAccessTokenWithAxsysScope(): String {
        val clientProperties = clientConfigurationProperties.registration["axsys-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getAppAccessTokenWithAxsysScope(): String {
        val clientProperties = clientConfigurationProperties.registration["axsys-maskintilmaskin"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getSaksbehandlerTokenWithPdlScope(): String {
        val clientProperties = clientConfigurationProperties.registration["pdl-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getStsSystembrukerToken(): String = stsClient.oidcToken()

    fun getAccessTokenFrontendSent(): String =
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD).tokenAsString

    fun getIdent(): String =
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD)
            .jwtTokenClaims?.get("NAVident")?.toString()
            ?: throw RuntimeException("Ident not found in token")

    //NB! Returnerer objectId'er, ikke navn p?? gruppene!
    fun getRollerFromToken(): List<String> =
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD)
            .jwtTokenClaims?.getAsList("groups").orEmpty().toList()

    //Brukes ikke per n??:
    fun erMaskinTilMaskinToken(): Boolean {
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD)
            .jwtTokenClaims?.allClaims?.forEach { securelogger.info("${it.key} - ${it.value}") }

        return getClaim("sub") == getClaim("oid")
    }

    private fun getClaim(name: String) =
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD)
            .jwtTokenClaims?.getStringClaim(name)
}
