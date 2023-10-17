package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerName
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.util.RoleUtils
import no.nav.klage.oppgave.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SaksbehandlerServiceTest {
    private val innstillingerService: InnstillingerService = mockk()
    private val azureGateway: AzureGateway = mockk()
    private val pdlFacade: PdlFacade = mockk()
    private val egenAnsattService: EgenAnsattService = mockk()
    private val tilgangService: TilgangService = mockk()
    private val saksbehandlerAccessService: SaksbehandlerAccessService = mockk()
    private val roleUtils: RoleUtils = mockk()
    private val tokenUtil: TokenUtil = mockk()

    private val SAKSBEHANDLER_IDENT_1 = "SAKSBEHANDLER_IDENT_1"
    private val SAKSBEHANDLER_NAME_1 = SaksbehandlerName(
        fornavn = "fornavn1", etternavn = "etternavn1", sammensattNavn = "sammensattNavn1"
    )

    private val SAKSBEHANDLER_1_PERSONLIG_INFO = SaksbehandlerPersonligInfo(
        fornavn = "fornavn1", etternavn = "etternavn1", sammensattNavn = "sammensattNavn1", enhet = Enhet(enhetId = "", navn = "")
    )

    private val SAKSBEHANDLER_1 = Saksbehandler(
        navIdent = SAKSBEHANDLER_IDENT_1, navn = SAKSBEHANDLER_NAME_1.sammensattNavn
    )

    private val SAKSBEHANDLER_IDENT_2 = "SAKSBEHANDLER_IDENT_2"
    private val SAKSBEHANDLER_NAME_2 = SaksbehandlerName(
        fornavn = "fornavn2", etternavn = "etternavn2", sammensattNavn = "sammensattNavn2"
    )

    private val SAKSBEHANDLER_2_PERSONLIG_INFO = SaksbehandlerPersonligInfo(
        fornavn = "fornavn2", etternavn = "etternavn2", sammensattNavn = "sammensattNavn2", enhet = Enhet(enhetId = "", navn = "")
    )

    private val SAKSBEHANDLER_2 = Saksbehandler(
        navIdent = SAKSBEHANDLER_IDENT_2, navn = SAKSBEHANDLER_NAME_2.sammensattNavn
    )

    private val FNR = "FNR"

    private val saksbehandlerService =
        SaksbehandlerService(
            innstillingerService = innstillingerService,
            azureGateway = azureGateway,
            pdlFacade = pdlFacade,
            tilgangService = tilgangService,
            saksbehandlerAccessService = saksbehandlerAccessService,
            roleUtils = roleUtils,
        )

    private val person = Person(
        foedselsnr = FNR,
        fornavn = null,
        mellomnavn = null,
        etternavn = null,
        sammensattNavn = null,
        beskyttelsesbehov = null,
        kjoenn = null,
        sivilstand = null,
    )

    private val personStrengtFortrolig = Person(
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
        every { tilgangService.harSaksbehandlerTilgangTil(any(), any()) }.returns(true)
        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_1, modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_2, modifiedBy = "",

                    )
            )
        )
        every { azureGateway.getDataOmSaksbehandler(SAKSBEHANDLER_IDENT_1) }.returns( SAKSBEHANDLER_1_PERSONLIG_INFO )
        every { azureGateway.getDataOmSaksbehandler(SAKSBEHANDLER_IDENT_2) }.returns( SAKSBEHANDLER_2_PERSONLIG_INFO )

        val result = saksbehandlerService.getSaksbehandlere(Ytelse.AAP_AAP, FNR)
        assertThat(result.saksbehandlere).contains(SAKSBEHANDLER_1)
        assertThat(result.saksbehandlere).contains(SAKSBEHANDLER_2)
    }

    @Test
    fun `getMedunderskrivere inneholder ikke innsender, men relevant medunderskriver`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(person)
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        every { tilgangService.harSaksbehandlerTilgangTil(any(), any()) }.returns(true)
        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_1, modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = SAKSBEHANDLER_IDENT_2, modifiedBy = "",

                    )
            )
        )
        every { azureGateway.getDataOmSaksbehandler(SAKSBEHANDLER_IDENT_1) }.returns( SAKSBEHANDLER_1_PERSONLIG_INFO )
        every { azureGateway.getDataOmSaksbehandler(SAKSBEHANDLER_IDENT_2) }.returns( SAKSBEHANDLER_2_PERSONLIG_INFO )

        val result = saksbehandlerService.getMedunderskrivere(SAKSBEHANDLER_IDENT_1, Ytelse.AAP_AAP, FNR)
        assertThat(result.medunderskrivere).doesNotContain(SAKSBEHANDLER_1)
        assertThat(result.medunderskrivere).contains(SAKSBEHANDLER_2)
    }

    @Test
    fun `Person med beskyttelsesbehov Strengt Fortrolig skal ikke ha medunderskriver`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(personStrengtFortrolig)
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)

        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(any()) }.returns(
            emptyList()
        )

        val result = saksbehandlerService.getMedunderskrivere(SAKSBEHANDLER_IDENT_1, Ytelse.AAP_AAP, FNR)
        assertThat(result.medunderskrivere).isEmpty()
    }
}