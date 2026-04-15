package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.kodeverk.AzureGroup
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.api.view.Saksbehandler
import no.nav.klage.oppgave.clients.klagelookup.BatchedGroupsHitResponse
import no.nav.klage.oppgave.clients.klagelookup.KlageLookupGateway
import no.nav.klage.oppgave.clients.klagelookup.PersonResponse
import no.nav.klage.oppgave.clients.klagelookup.UserResponse
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerEnhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerName
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.entities.SaksbehandlerAccess
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SaksbehandlerServiceTest {
    private val innstillingerService: InnstillingerService = mockk()
    private val klageLookupGateway: KlageLookupGateway = mockk()
    private val tilgangService: TilgangService = mockk()
    private val saksbehandlerAccessService: SaksbehandlerAccessService = mockk()

    private val SAKSBEHANDLER_IDENT_1 = "SAKSBEHANDLER_IDENT_1"
    private val SAKSBEHANDLER_NAME_1 = SaksbehandlerName(
        fornavn = "fornavn1", etternavn = "etternavn1", sammensattNavn = "sammensattNavn1"
    )

    private val SAKSBEHANDLER_1_PERSONLIG_INFO = SaksbehandlerPersonligInfo(
        navIdent = "abc1231",
        fornavn = "fornavn1",
        etternavn = "etternavn1",
        sammensattNavn = "sammensattNavn1",
        enhet = SaksbehandlerEnhet(enhetId = "", navn = "")
    )

    private val SAKSBEHANDLER_1 = Saksbehandler(
        navIdent = SAKSBEHANDLER_IDENT_1, navn = SAKSBEHANDLER_NAME_1.sammensattNavn
    )

    private val SAKSBEHANDLER_IDENT_2 = "SAKSBEHANDLER_IDENT_2"
    private val SAKSBEHANDLER_NAME_2 = SaksbehandlerName(
        fornavn = "fornavn2", etternavn = "etternavn2", sammensattNavn = "sammensattNavn2"
    )

    private val SAKSBEHANDLER_2_PERSONLIG_INFO = SaksbehandlerPersonligInfo(
        navIdent = "abc1232",
        fornavn = "fornavn2",
        etternavn = "etternavn2",
        sammensattNavn = "sammensattNavn2",
        enhet = SaksbehandlerEnhet(enhetId = "", navn = "")
    )

    private val SAKSBEHANDLER_2 = Saksbehandler(
        navIdent = SAKSBEHANDLER_IDENT_2, navn = SAKSBEHANDLER_NAME_2.sammensattNavn
    )

    private val FNR = "FNR"

    private val saksbehandlerService =
        SaksbehandlerService(
            innstillingerService = innstillingerService,
            klageLookupGateway = klageLookupGateway,
            tilgangService = tilgangService,
            saksbehandlerAccessService = saksbehandlerAccessService,
        )

    private val person = PersonResponse(
        foedselsnr = FNR,
        fornavn = "fornavn",
        mellomnavn = null,
        etternavn = "etternavn",
        sammensattNavn = "fornavn etternavn",
        kjoenn = "M",
        doed = null,
        strengtFortrolig = false,
        strengtFortroligUtland = false,
        fortrolig = false,
        egenAnsatt = false,
        vergemaalEllerFremtidsfullmakt = false,
        sikkerhetstiltak = null,
        protectedFamilyMembers = emptyList(),
    )

    private val personStrengtFortrolig = PersonResponse(
        foedselsnr = FNR,
        fornavn = "fornavn",
        mellomnavn = null,
        etternavn = "etternavn",
        sammensattNavn = "fornavn etternavn",
        kjoenn = "M",
        doed = null,
        strengtFortrolig = true,
        strengtFortroligUtland = false,
        fortrolig = false,
        egenAnsatt = false,
        vergemaalEllerFremtidsfullmakt = false,
        sikkerhetstiltak = null,
        protectedFamilyMembers = emptyList(),
    )

    private val personFortrolig = PersonResponse(
        foedselsnr = FNR,
        fornavn = "fornavn",
        mellomnavn = null,
        etternavn = "etternavn",
        sammensattNavn = "fornavn etternavn",
        kjoenn = "M",
        doed = null,
        strengtFortrolig = false,
        strengtFortroligUtland = false,
        fortrolig = true,
        egenAnsatt = false,
        vergemaalEllerFremtidsfullmakt = false,
        sikkerhetstiltak = null,
        protectedFamilyMembers = emptyList(),
    )

    private fun batchedGroupsForSaksbehandlere(vararg navIdents: String): List<BatchedGroupsHitResponse> =
        navIdents.map {
            BatchedGroupsHitResponse(
                navIdent = it,
                groupIds = listOf(AzureGroup.KABAL_SAKSBEHANDLING.id),
            )
        }

    private fun batchedGroupsForROLs(vararg navIdents: String): List<BatchedGroupsHitResponse> =
        navIdents.map {
            BatchedGroupsHitResponse(
                navIdent = it,
                groupIds = listOf(AzureGroup.KABAL_ROL.id),
            )
        }

    @Test
    fun `getSaksbehandlere inneholder relevante saksbehandlere for ytelse og fnr`() {
        every { klageLookupGateway.getPerson(any(), any()) }.returns(person)
        every { klageLookupGateway.getUserGroupsBatched(listOf(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2)) } returns
            batchedGroupsForSaksbehandlere(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2)
        every { tilgangService.hasSaksbehandlerAccessToSak(any(), any(), any(), any(), any()) }.returns(true)
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
        every { klageLookupGateway.getUserInfoForGivenNavIdent(SAKSBEHANDLER_IDENT_1) }.returns(SAKSBEHANDLER_1_PERSONLIG_INFO)
        every { klageLookupGateway.getUserInfoForGivenNavIdent(SAKSBEHANDLER_IDENT_2) }.returns(SAKSBEHANDLER_2_PERSONLIG_INFO)

        val result = saksbehandlerService.getSaksbehandlere(
            fnr = FNR,
            ytelse = Ytelse.AAP_AAP,
            sakId = "abc",
            fagsystem = Fagsystem.FS36
        )
        assertThat(result.saksbehandlere).contains(SAKSBEHANDLER_1)
        assertThat(result.saksbehandlere).contains(SAKSBEHANDLER_2)
    }

    @Test
    fun `getSaksbehandlere filtrerer bort saksbehandler uten KABAL_SAKSBEHANDLING rolle`() {
        every { klageLookupGateway.getPerson(any(), any()) }.returns(person)
        every { klageLookupGateway.getUserGroupsBatched(listOf(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2)) } returns
            listOf(
                BatchedGroupsHitResponse(
                    navIdent = SAKSBEHANDLER_IDENT_1,
                    groupIds = listOf(AzureGroup.KABAL_SAKSBEHANDLING.id),
                ),
                BatchedGroupsHitResponse(
                    navIdent = SAKSBEHANDLER_IDENT_2,
                    groupIds = emptyList(),
                ),
            )
        every { tilgangService.hasSaksbehandlerAccessToSak(any(), any(), any(), any(), any()) }.returns(true)
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
        every { klageLookupGateway.getUserInfoForGivenNavIdent(SAKSBEHANDLER_IDENT_1) }.returns(SAKSBEHANDLER_1_PERSONLIG_INFO)

        val result = saksbehandlerService.getSaksbehandlere(
            fnr = FNR,
            ytelse = Ytelse.AAP_AAP,
            sakId = "abc",
            fagsystem = Fagsystem.FS36
        )

        assertThat(result.saksbehandlere).containsExactly(SAKSBEHANDLER_1)
    }

    @Test
    fun `getMedunderskrivere inneholder ikke innsender, men relevant medunderskriver`() {
        every { klageLookupGateway.getPerson(any(), any()) }.returns(person)
        every { klageLookupGateway.getUserGroupsBatched(listOf(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2)) } returns
            batchedGroupsForSaksbehandlere(SAKSBEHANDLER_IDENT_1, SAKSBEHANDLER_IDENT_2)
        every { tilgangService.hasSaksbehandlerAccessToSak(any(), any(), any(), any(), any()) }.returns(true)
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
        every { klageLookupGateway.getUserInfoForGivenNavIdent(SAKSBEHANDLER_IDENT_1) }.returns(SAKSBEHANDLER_1_PERSONLIG_INFO)
        every { klageLookupGateway.getUserInfoForGivenNavIdent(SAKSBEHANDLER_IDENT_2) }.returns(SAKSBEHANDLER_2_PERSONLIG_INFO)

        val result = saksbehandlerService.getMedunderskrivere(
            ident = SAKSBEHANDLER_IDENT_1,
            ytelse = Ytelse.AAP_AAP,
            fnr = FNR,
            sakId = "abc",
            fagsystem = Fagsystem.FS36,
        )
        assertThat(result.medunderskrivere).doesNotContain(SAKSBEHANDLER_1)
        assertThat(result.medunderskrivere).contains(SAKSBEHANDLER_2)
    }

    @Test
    fun `Person med beskyttelsesbehov Strengt Fortrolig skal ikke ha medunderskriver`() {
        every { klageLookupGateway.getPerson(any(), any()) }.returns(personStrengtFortrolig)

        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(any()) }.returns(
            emptyList()
        )

        val result = saksbehandlerService.getMedunderskrivere(
            ident = SAKSBEHANDLER_IDENT_1,
            ytelse = Ytelse.AAP_AAP,
            fnr = FNR,
            sakId = "abc",
            fagsystem = Fagsystem.FS36,
        )
        assertThat(result.medunderskrivere).isEmpty()
    }

    @Test
    fun `Person med beskyttelsesbehov Fortrolig skal ikke ha medunderskriver`() {
        every { klageLookupGateway.getPerson(any(), any()) }.returns(personFortrolig)

        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(any()) }.returns(
            emptyList()
        )

        val result = saksbehandlerService.getMedunderskrivere(
            ident = SAKSBEHANDLER_IDENT_1,
            ytelse = Ytelse.AAP_AAP,
            fnr = FNR,
            sakId = "abc",
            fagsystem = Fagsystem.FS36,
        )
        assertThat(result.medunderskrivere).isEmpty()
    }

    @Nested
    inner class GetROLList {
        @Test
        fun `getROLList inneholder relevante ROL for ytelse og fnr`() {
            val rolIdent = "ROL_IDENT"
            val rolName = SaksbehandlerPersonligInfo(
                navIdent = rolIdent,
                fornavn = "rol",
                etternavn = "bruker",
                sammensattNavn = "rol bruker",
                enhet = SaksbehandlerEnhet(enhetId = "", navn = "")
            )

            every { klageLookupGateway.getPerson(any(), any()) }.returns(person)
            every { klageLookupGateway.getUsersInGroup(AzureGroup.KABAL_ROL) }.returns(
                listOf(
                    UserResponse(
                        navIdent = rolIdent,
                        fornavn = "rol",
                        etternavn = "bruker",
                        sammensattNavn = "rol bruker",
                    )
                )
            )
            every { klageLookupGateway.getUserGroupsBatched(listOf(rolIdent)) } returns batchedGroupsForROLs(rolIdent)
            every { tilgangService.hasSaksbehandlerAccessToSak(any(), any(), any(), any(), any()) }.returns(true)
            every { klageLookupGateway.getUserInfoForGivenNavIdent(rolIdent) }.returns(rolName)

            val result = saksbehandlerService.getROLList(
                fnr = FNR,
                ytelse = Ytelse.AAP_AAP,
                sakId = "abc",
                fagsystem = Fagsystem.FS36
            )
            assertThat(result.saksbehandlere).contains(
                Saksbehandler(navIdent = rolIdent, navn = "rol bruker")
            )
        }

        @Test
        fun `getROLList med person med Strengt Fortrolig beskyttelsesbehov returnerer tom liste`() {
            every { klageLookupGateway.getPerson(any(), any()) }.returns(personStrengtFortrolig)
            every { klageLookupGateway.getUsersInGroup(AzureGroup.KABAL_ROL) }.returns(emptyList())

            val result = saksbehandlerService.getROLList(
                fnr = FNR,
                ytelse = Ytelse.AAP_AAP,
                sakId = "abc",
                fagsystem = Fagsystem.FS36
            )
            assertThat(result.saksbehandlere).isEmpty()
        }
    }
}