package no.nav.klage.oppgave.api

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.api.controller.FeatureToggleController
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.zalando.problem.spring.web.autoconfigure.ProblemAutoConfiguration
import org.zalando.problem.spring.web.autoconfigure.ProblemJacksonAutoConfiguration
import org.zalando.problem.spring.web.autoconfigure.ProblemJacksonWebMvcAutoConfiguration

@WebMvcTest(FeatureToggleController::class)
@ImportAutoConfiguration(
    classes = [
        ProblemJacksonWebMvcAutoConfiguration::class,
        ProblemJacksonAutoConfiguration::class,
        ProblemAutoConfiguration::class]
)
@ActiveProfiles("local")
class ProblemHandlingTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var unleash: Unleash

    @MockkBean
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @BeforeEach
    fun setup() {
        every {
            unleash.isEnabled(
                any(),
                any<UnleashContext>()
            )
        } throws WebClientResponseException.create(
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.reasonPhrase,
            HttpHeaders(),
            "{ \"error\": \"min tekst\" }".toByteArray(Charsets.UTF_8),
            null,
            null
        )
    }

    @Test
    fun `thrown exception should result in appropriate problem`() {
        this.mockMvc.perform(get("/aapenfeaturetoggle/testing")).andDo(print()).andExpect(status().isConflict())
            .andExpect(content().string(containsString("{\"title\":\"Conflict\",\"status\":409,\"detail\":\"{ \\\"error\\\": \\\"min tekst\\\" }\"}")))
    }

}