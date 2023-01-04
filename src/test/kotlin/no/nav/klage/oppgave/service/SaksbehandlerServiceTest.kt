package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerName
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.repositories.InnstillingerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.RoleUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class SaksbehandlerServiceTest {
    private val innloggetAnsattRepository: InnloggetAnsattRepository = mockk()
    private val innstillingerRepository: InnstillingerRepository = mockk()
    private val azureGateway: AzureGateway = mockk()
    private val pdlFacade: PdlFacade = mockk()
    private val saksbehandlerRepository: SaksbehandlerRepository = mockk()
    private val egenAnsattService: EgenAnsattService = mockk()
    private val tilgangService: TilgangService = mockk()
    private val saksbehandlerAccessService: SaksbehandlerAccessService = mockk()
    private val roleUtils: RoleUtils = mockk()

    private val SAKSBEHANDLER_IDENT_1 = "SAKSBEHANDLER_IDENT_1"
    private val SAKSBEHANDLER_NAME_1 = SaksbehandlerName(
        fornavn = "fornavn1", etternavn = "etternavn1", sammensattNavn = "sammensattNavn1"
    )

    private val SAKSBEHANDLER_1 = Saksbehandler(
        navIdent = SAKSBEHANDLER_IDENT_1, navn = SAKSBEHANDLER_NAME_1.sammensattNavn
    )

    private val SAKSBEHANDLER_IDENT_2 = "SAKSBEHANDLER_IDENT_2"
    private val SAKSBEHANDLER_NAME_2 = SaksbehandlerName(
        fornavn = "fornavn2", etternavn = "etternavn2", sammensattNavn = "sammensattNavn2"
    )

    private val SAKSBEHANDLER_2 = Saksbehandler(
        navIdent = SAKSBEHANDLER_IDENT_2, navn = SAKSBEHANDLER_NAME_2.sammensattNavn
    )

    private val FNR = "FNR"

    private val saksbehandlerService =
        SaksbehandlerService(
            innloggetAnsattRepository = innloggetAnsattRepository,
            innstillingerRepository = innstillingerRepository,
            azureGateway = azureGateway,
            enhetRepository = mockk(),
            pdlFacade = pdlFacade,
            saksbehandlerRepository = saksbehandlerRepository,
            egenAnsattService = egenAnsattService,
            tilgangService = tilgangService,
            roleUtils = roleUtils,
            saksbehandlerAccessService = saksbehandlerAccessService
        )

    val person = Person(
        foedselsnr = FNR,
        fornavn = null,
        mellomnavn = null,
        etternavn = null,
        sammensattNavn = null,
        beskyttelsesbehov = null,
        kjoenn = null,
        sivilstand = null,
    )

    val personStrengtFortrolig = Person(
        foedselsnr = FNR,
        fornavn = null,
        mellomnavn = null,
        etternavn = null,
        sammensattNavn = null,
        beskyttelsesbehov = Beskyttelsesbehov.STRENGT_FORTROLIG,
        kjoenn = null,
        sivilstand = null,
    )

    @Test
    fun `getSaksbehandlere inneholder relevante saksbehandlere for ytelse og fnr`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(person)
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        every { saksbehandlerAccessService.getSaksbehandlerIdentsForYtelse(Ytelse.AAP_AAP) }.returns(listOf(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2))
        every { roleUtils.isSaksbehandler(SAKSBEHANDLER_IDENT_1) }.returns(true)
        every { roleUtils.isSaksbehandler(SAKSBEHANDLER_IDENT_2) }.returns(true)
        every { saksbehandlerRepository.getNameForSaksbehandler(SAKSBEHANDLER_IDENT_1) }.returns(SAKSBEHANDLER_NAME_1)
        every { saksbehandlerRepository.getNameForSaksbehandler(SAKSBEHANDLER_IDENT_2) }.returns(SAKSBEHANDLER_NAME_2)

        val result = saksbehandlerService.getSaksbehandlere(Ytelse.AAP_AAP, FNR)
        assertThat(result.saksbehandlere).contains(SAKSBEHANDLER_1)
        assertThat(result.saksbehandlere).contains(SAKSBEHANDLER_2)
    }

    @Test
    fun `getMedunderskrivere inneholder ikke innsender, men relevant medunderskriver`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(person)
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        every { saksbehandlerAccessService.getSaksbehandlerIdentsForYtelse(Ytelse.AAP_AAP) }.returns(listOf(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2))
        every { roleUtils.isSaksbehandler(SAKSBEHANDLER_IDENT_1) }.returns(true)
        every { roleUtils.isSaksbehandler(SAKSBEHANDLER_IDENT_2) }.returns(true)
        every { saksbehandlerRepository.getNameForSaksbehandler(SAKSBEHANDLER_IDENT_1) }.returns(SAKSBEHANDLER_NAME_1)
        every { saksbehandlerRepository.getNameForSaksbehandler(SAKSBEHANDLER_IDENT_2) }.returns(SAKSBEHANDLER_NAME_2)

        val result = saksbehandlerService.getMedunderskrivere(SAKSBEHANDLER_IDENT_1, Ytelse.AAP_AAP, FNR)
        assertThat(result.medunderskrivere).doesNotContain(SAKSBEHANDLER_1)
        assertThat(result.medunderskrivere).contains(SAKSBEHANDLER_2)
    }

    @Test
    fun `Person med beskyttelsesbehov Strengt Fortrolig skal ikke ha medunderskriver`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(personStrengtFortrolig)
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)

        val result = saksbehandlerService.getMedunderskrivere(SAKSBEHANDLER_IDENT_1, Ytelse.AAP_AAP, FNR)
        assertThat(result.medunderskrivere).isEmpty()
    }
}