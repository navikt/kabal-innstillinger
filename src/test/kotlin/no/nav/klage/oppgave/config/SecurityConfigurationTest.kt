package no.nav.klage.oppgave.config

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.auth.test.AzureSecurityContractTest
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.controller.HjemmelController
import no.nav.klage.oppgave.service.InnstillingerService
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@WebMvcTest(HjemmelController::class)
@Import(SecurityConfiguration::class)
class SecurityConfigurationTest : AzureSecurityContractTest() {
    @MockkBean(relaxed = true)
    lateinit var innstillingerService: InnstillingerService

    override fun protectedRequest() = get("/hjemler").param("ytelseId", Ytelse.BIL_BIL.id)
}
