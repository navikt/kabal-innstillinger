package no.nav.klage.oppgave.api.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.TokenUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SearchController::class)
@ActiveProfiles("local")
class SearchControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var tokenUtil: TokenUtil

    @MockkBean
    lateinit var saksbehandlerService: SaksbehandlerService

    @BeforeEach
    fun setup() {
        every { tokenUtil.getCurrentIdent() } returns "H149390"
    }

    private val mapper = jacksonObjectMapper()
    private val ytelseId = Ytelse.BIL_BIL.id
    private val navIdent = "navIdent"
    private val navn = "navn"
    private val fnr = "12312312312"
    private val enhet = "enhet"
    private val sakId = "sakId"

    private val searchMedunderskrivereInput = SearchMedunderskrivereInput(
        ytelseId = ytelseId,
        fnr = fnr,
        enhet = enhet,
        navIdent = navIdent,
        sak = null,
    )

    private val searchROLInput = SearchROLInput(
        fnr = fnr,
        sakId = sakId,
        ytelseId = ytelseId,
        fagsystemId = null,
    )

    private val searchSaksbehandlerInput = SearchSaksbehandlerInput(
        ytelseId = ytelseId,
        fnr = fnr,
        sakId = null,
        fagsystemId = null,
    )

    private val medunderskrivereForYtelse = MedunderskrivereForYtelse(
        ytelse = ytelseId,
        medunderskrivere = listOf(
            Saksbehandler(
                navIdent = navIdent,
                navn = navn,
            )
        )
    )

    private val saksbehandlere = Saksbehandlere(
        saksbehandlere = listOf(
            Saksbehandler(
                navIdent = navIdent,
                navn = navn,
            )
        )
    )

    @Test
    fun getMedunderskrivereForYtelseOgFnr() {
        every {
            saksbehandlerService.getMedunderskrivere(
                ident = any(),
                ytelse = any(),
                fnr = any(),
                sakId = any(),
                fagsystem = any(),
            )
        } returns medunderskrivereForYtelse

        mockMvc.perform(
            post("/search/medunderskrivere").content(mapper.writeValueAsString(searchMedunderskrivereInput))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun getROLForFnr() {
        every {
            saksbehandlerService.getROLList(
                fnr = any(),
                ytelse = any(),
                sakId = any(),
                fagsystem = any(),
            )
        } returns saksbehandlere

        mockMvc.perform(
            post("/search/rol").content(mapper.writeValueAsString(searchROLInput))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun getSaksbehandlereForYtelseOgFnr() {
        every {
            saksbehandlerService.getSaksbehandlere(
                ytelse = any(),
                fnr = any(),
                sakId = any(),
                fagsystem = any(),
            )
        } returns saksbehandlere

        mockMvc.perform(
            post("/search/saksbehandlere").content(mapper.writeValueAsString(searchSaksbehandlerInput))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    }
}