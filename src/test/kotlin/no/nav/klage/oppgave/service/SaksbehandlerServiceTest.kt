package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerName
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import no.nav.klage.oppgave.repositories.InnstillingerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.RoleUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaksbehandlerServiceTest {
    private val innloggetAnsattRepository: InnloggetAnsattRepository = mockk()
    private val innstillingerRepository: InnstillingerRepository = mockk()
    private val azureGateway: AzureGateway = mockk()
    private val pdlFacade: PdlFacade = mockk()
    private val saksbehandlerRepository: SaksbehandlerRepository = mockk()
    private val egenAnsattService: EgenAnsattService = mockk()
    private val tilgangService: TilgangService = mockk()
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository = mockk()
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
            pdlFacade = pdlFacade,
            saksbehandlerRepository = saksbehandlerRepository,
            egenAnsattService = egenAnsattService,
            tilgangService = tilgangService,
            saksbehandlerAccessRepository = saksbehandlerAccessRepository,
            roleUtils = roleUtils,
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
        every { saksbehandlerAccessRepository.findAllByYtelserContaining(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_1, modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_2, modifiedBy = "",

                    )
            )
        )
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
        every { saksbehandlerAccessRepository.findAllByYtelserContaining(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_1, modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_2, modifiedBy = "",

                    )
            )
        )
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

        every { saksbehandlerAccessRepository.findAllByYtelserContaining(any()) }.returns(
            emptyList()
        )

        val result = saksbehandlerService.getMedunderskrivere(SAKSBEHANDLER_IDENT_1, Ytelse.AAP_AAP, FNR)
        assertThat(result.medunderskrivere).isEmpty()
    }

    @Test
    fun `getYtelserToAdd with new ytelser gives expected result`() {
        val inputYtelser = setOf(Ytelse.AAP_AAP, Ytelse.OMS_PLS, Ytelse.OMS_OLP)
        val existingYtelser = setOf(Ytelse.OMS_OLP)

        val ytelserToAddResult = saksbehandlerService.getYtelserToAdd(
            inputYtelser = inputYtelser,
            existingInnstillingerYtelser = existingYtelser
        )
        val expectedResult = setOf(Ytelse.AAP_AAP, Ytelse.OMS_PLS)

        assertThat(ytelserToAddResult).isEqualTo(expectedResult)
    }

    @Test
    fun `getYtelserToAdd with same ytelser gives empty set`() {
        val inputYtelser = setOf(Ytelse.AAP_AAP, Ytelse.OMS_PLS, Ytelse.OMS_OLP)
        val existingYtelser = setOf(Ytelse.AAP_AAP, Ytelse.OMS_PLS, Ytelse.OMS_OLP)

        val ytelserToAddResult = saksbehandlerService.getYtelserToAdd(
            inputYtelser = inputYtelser,
            existingInnstillingerYtelser = existingYtelser
        )

        assertThat(ytelserToAddResult).isEmpty()
    }

    @Test
    fun `getYtelserToKeep with overlap gives expected result`() {
        val inputYtelser = setOf(Ytelse.AAP_AAP, Ytelse.OMS_PLS, Ytelse.OMS_OLP)
        val existingYtelser = setOf(Ytelse.OMS_OLP, Ytelse.AAP_AAP, Ytelse.BAR_BAR)

        val ytelserToAddResult = saksbehandlerService.getYtelserToKeep(
            inputYtelser = inputYtelser,
            existingInnstillingerYtelser = existingYtelser
        )
        val expectedResult = setOf(Ytelse.AAP_AAP, Ytelse.OMS_OLP)

        assertThat(ytelserToAddResult).isEqualTo(expectedResult)
    }

    @Test
    fun `getYtelserToKeep with no existing ytelser gives empty set`() {
        val inputYtelser = setOf(Ytelse.AAP_AAP, Ytelse.OMS_PLS, Ytelse.OMS_OLP)
        val existingYtelser = emptySet<Ytelse>()

        val ytelserToAddResult = saksbehandlerService.getYtelserToKeep(
            inputYtelser = inputYtelser,
            existingInnstillingerYtelser = existingYtelser
        )

        assertThat(ytelserToAddResult).isEmpty()
    }

    @Test
    fun `getUpdatedHjemmelSet, add new hjemmel sets from new ytelse`() {
        val ytelserToAdd = setOf(Ytelse.ENF_ENF, Ytelse.BAR_BAR)
        val ytelserToKeep = setOf(Ytelse.OMS_PLS)
        val existingHjemler = setOf(Hjemmel.FTRL_9_3, Hjemmel.FTRL_9_5, Hjemmel.FTRL_9_14)

        val output = saksbehandlerService.getUpdatedHjemmelSet(
            ytelserToAdd = ytelserToAdd,
            ytelserToKeep = ytelserToKeep,
            existingHjemler = existingHjemler
        )

        val expectedResult = setOf(
            Hjemmel.FTRL_9_3,
            Hjemmel.FTRL_9_5,
            Hjemmel.FTRL_9_14,
            Hjemmel.BTRL_2,
            Hjemmel.BTRL_4,
            Hjemmel.BTRL_5,
            Hjemmel.BTRL_9,
            Hjemmel.BTRL_10,
            Hjemmel.BTRL_11,
            Hjemmel.BTRL_12,
            Hjemmel.BTRL_13,
            Hjemmel.BTRL_17,
            Hjemmel.BTRL_18,
            Hjemmel.EOES_AVTALEN,
            Hjemmel.NORDISK_KONVENSJON,
            Hjemmel.ANDRE_TRYGDEAVTALER,
            Hjemmel.FTRL_15_2,
            Hjemmel.FTRL_15_3,
            Hjemmel.FTRL_15_4,
            Hjemmel.FTRL_15_5,
            Hjemmel.FTRL_15_6,
            Hjemmel.FTRL_15_8,
            Hjemmel.FTRL_15_9,
            Hjemmel.FTRL_15_10,
            Hjemmel.FTRL_15_11,
            Hjemmel.FTRL_15_12,
            Hjemmel.FTRL_15_13,
            Hjemmel.FTRL_22_12,
            Hjemmel.FTRL_22_13,
            Hjemmel.FTRL_22_15,
        )

        assertThat(output).isEqualTo(expectedResult)
    }

    @Test
    fun `getUpdatedHjemmelSet, keep hjemler based on ytelserToKeep, remove unapplicable`() {
        val ytelserToAdd = emptySet<Ytelse>()
        val ytelserToKeep = setOf(Ytelse.OMS_OMP)
        val existingHjemler = setOf(Hjemmel.FTRL_9_3, Hjemmel.FTRL_9_5, Hjemmel.FTRL_8_2)

        val output = saksbehandlerService.getUpdatedHjemmelSet(
            ytelserToAdd = ytelserToAdd,
            ytelserToKeep = ytelserToKeep,
            existingHjemler = existingHjemler
        )

        val expectedResult = setOf(
            Hjemmel.FTRL_9_3,
            Hjemmel.FTRL_9_5,
        )

        assertThat(output).isEqualTo(expectedResult)
    }
}