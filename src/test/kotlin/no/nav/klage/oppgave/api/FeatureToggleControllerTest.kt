package no.nav.klage.oppgave.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.api.controller.FeatureToggleController
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(FeatureToggleController::class)
@ActiveProfiles("local")
class FeatureToggleControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var unleash: Unleash

    @MockkBean
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @BeforeEach
    fun setup() {
        every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns "H149390"
        every { unleash.isEnabled(any(), any<UnleashContext>()) } returns true
    }

    @Test
    fun `open toggle should return true`() {
        this.mockMvc.perform(get("/aapenfeaturetoggle/testing")).andDo(print()).andExpect(status().isOk())
            .andExpect(content().string(containsString("true")))
    }

    @Test
    @Disabled("Authentication is not turned on..")
    fun `protected toggle should throw exception`() {
        this.mockMvc.perform(get("/featuretoggle/testing")).andDo(print()).andExpect(status().is4xxClientError())
    }
}