package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.clients.klagelookup.NavIdentAccessResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TilgangServiceTest {
    private val klageLookupGateway = mockk<KlageLookupGateway>()
    private val tilgangService = TilgangService(klageLookupGateway = klageLookupGateway)

    private val fnr = "12345678910"

    @Test
    fun `returnerer bare navIdenter med tilgang, og manglende svar regnes som ingen tilgang`() {
        every { klageLookupGateway.getAccessBatched(brukerId = fnr, navIdentList = listOf("Z1", "Z2", "Z3")) } returns
            listOf(
                NavIdentAccessResponse(navIdent = "Z1", access = true, reason = "Access granted"),
                NavIdentAccessResponse(navIdent = "Z2", access = false, reason = "Skjermet"),
            )

        val result = tilgangService.getNavIdentsWithAccessToPerson(fnr = fnr, navIdentList = listOf("Z1", "Z2", "Z3"))

        assertThat(result).containsExactly("Z1")
    }

    @Test
    fun `tom liste gir ingen kall`() {
        assertThat(tilgangService.getNavIdentsWithAccessToPerson(fnr = fnr, navIdentList = emptyList())).isEmpty()
        verify(exactly = 0) { klageLookupGateway.getAccessBatched(brukerId = any(), navIdentList = any()) }
    }
}
