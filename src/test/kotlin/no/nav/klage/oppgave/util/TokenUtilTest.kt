package no.nav.klage.oppgave.util

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.auth.TexasClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class TokenUtilTest {
    private val texasClient = mockk<TexasClient>()
    private val tokenUtil = TokenUtil(texasClient = texasClient, klageLookupScope = "klage-lookup")

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `reads NAVident from the current token`() {
        authenticate(claims = mapOf("NAVident" to "Z123456"))

        assertThat(tokenUtil.getCurrentIdent()).isEqualTo("Z123456")
    }

    @Test
    fun `fails when the current token has no NAVident`() {
        authenticate(claims = mapOf("idtyp" to "app"))

        assertThrows<RuntimeException> { tokenUtil.getCurrentIdent() }
    }

    @Test
    fun `asks Texas for tokens with the klage-lookup scope`() {
        every { texasClient.onBehalfOfCurrentUser(target = "klage-lookup") } returns "obo-token"
        every { texasClient.machineToMachineToken(target = "klage-lookup") } returns "m2m-token"

        assertThat(tokenUtil.getOnBehalfOfTokenWithKlageLookupScope()).isEqualTo("obo-token")
        assertThat(tokenUtil.getAppAccessTokenWithKlageLookupScope()).isEqualTo("m2m-token")
    }

    private fun authenticate(claims: Map<String, Any>) {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "subject")
                .claims { it.putAll(claims) }
                .build()
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
    }
}
