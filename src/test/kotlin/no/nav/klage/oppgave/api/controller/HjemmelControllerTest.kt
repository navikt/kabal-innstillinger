package no.nav.klage.oppgave.api.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.service.InnstillingerService
import no.nav.klage.oppgave.util.TokenUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(HjemmelController::class)
@ActiveProfiles("local")
class HjemmelControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var tokenUtil: TokenUtil

    @MockkBean
    lateinit var innstillingerService: InnstillingerService

    @BeforeEach
    fun setup() {
        every { tokenUtil.getCurrentIdent() } returns "H149390"
    }

    @Test
    fun `get hjemmel configurations returns array and forwards filters`() {
        every {
            innstillingerService.getDistinctHjemmelSetsForYtelse(
                ytelse = Ytelse.BIL_BIL,
                includeStyringsEnhet = false,
                onlyAnketeam = true,
            )
        } returns listOf(listOf("FTRL_8_1", "FTRL_8_2"), listOf("FTRL_10_3"))

        mockMvc
            .perform(
                get("/hjemler/konfigurasjoner")
                    .param("ytelseId", Ytelse.BIL_BIL.id)
                    .param("includeSE", "false")
                    .param("onlyAnketeam", "true"),
            ).andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON))
            .andExpect(
                content().json(
                    """[{"hjemmelIds":["FTRL_8_1","FTRL_8_2"]},{"hjemmelIds":["FTRL_10_3"]}]""",
                ),
            )

        verify(exactly = 1) {
            innstillingerService.getDistinctHjemmelSetsForYtelse(
                ytelse = Ytelse.BIL_BIL,
                includeStyringsEnhet = false,
                onlyAnketeam = true,
            )
        }
    }
}
