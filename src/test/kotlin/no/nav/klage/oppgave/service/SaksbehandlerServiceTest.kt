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
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerGroups
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
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

    private val saksbehandlerIdent1 = "SAKSBEHANDLER_IDENT_1"
    private val saksbehandlerName1 =
        SaksbehandlerName(
            fornavn = "fornavn1",
            etternavn = "etternavn1",
            sammensattNavn = "sammensattNavn1",
        )

    private val saksbehandler1PersonligInfo =
        SaksbehandlerPersonligInfo(
            navIdent = "abc1231",
            fornavn = "fornavn1",
            etternavn = "etternavn1",
            sammensattNavn = "sammensattNavn1",
            enhet = SaksbehandlerEnhet(enhetId = "saksbehandlerEnhetId", navn = ""),
        )

    private val saksbehandler1 =
        Saksbehandler(
            navIdent = saksbehandlerIdent1,
            navn = saksbehandlerName1.sammensattNavn,
            ansattEnhetId = "saksbehandlerEnhetId",
        )

    private val saksbehandlerIdent2 = "SAKSBEHANDLER_IDENT_2"
    private val saksbehandlerName2 =
        SaksbehandlerName(
            fornavn = "fornavn2",
            etternavn = "etternavn2",
            sammensattNavn = "sammensattNavn2",
        )

    private val saksbehandler2PersonligInfo =
        SaksbehandlerPersonligInfo(
            navIdent = "abc1232",
            fornavn = "fornavn2",
            etternavn = "etternavn2",
            sammensattNavn = "sammensattNavn2",
            enhet = SaksbehandlerEnhet(enhetId = "saksbehandlerEnhetId", navn = ""),
        )

    private val saksbehandler2 =
        Saksbehandler(
            navIdent = saksbehandlerIdent2,
            navn = saksbehandlerName2.sammensattNavn,
            ansattEnhetId = "saksbehandlerEnhetId",
        )

    private val fnr = "FNR"

    private val saksbehandlerService =
        SaksbehandlerService(
            innstillingerService = innstillingerService,
            klageLookupGateway = klageLookupGateway,
            tilgangService = tilgangService,
            saksbehandlerAccessService = saksbehandlerAccessService,
        )

    private val person =
        PersonResponse(
            foedselsnr = fnr,
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
        )

    private val personStrengtFortrolig =
        PersonResponse(
            foedselsnr = fnr,
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
        )

    private val personFortrolig =
        PersonResponse(
            foedselsnr = fnr,
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
        every { klageLookupGateway.getPerson(any()) }.returns(person)
        every { klageLookupGateway.getUserGroupsBatched(listOf(saksbehandlerIdent1, saksbehandlerIdent2)) } returns
            batchedGroupsForSaksbehandlere(saksbehandlerIdent1, saksbehandlerIdent2)
        every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = any(), fnr = any()) }.returns(true)
        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent1,
                    modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent2,
                    modifiedBy = "",
                ),
            ),
        )
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent1) }.returns(saksbehandler1PersonligInfo)
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent2) }.returns(saksbehandler2PersonligInfo)
        every { klageLookupGateway.getGroupsForGivenNavIdent(any()) } returns SaksbehandlerGroups(emptyList())
        every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(any()) } returns emptySet()
        every { innstillingerService.findSaksbehandlerInnstillinger(ident = any()) } returns
            SaksbehandlerInnstillinger(
                anonymous = false,
            )

        val result =
            saksbehandlerService.getSaksbehandlere(
                fnr = fnr,
                ytelse = Ytelse.AAP_AAP,
                sakId = "abc",
                fagsystem = Fagsystem.AO01,
            )
        assertThat(result.saksbehandlere).contains(saksbehandler1)
        assertThat(result.saksbehandlere).contains(saksbehandler2)
    }

    @Test
    fun `getSaksbehandlere filtrerer bort saksbehandler uten KABAL_SAKSBEHANDLING rolle`() {
        every { klageLookupGateway.getPerson(any()) }.returns(person)
        every { klageLookupGateway.getUserGroupsBatched(listOf(saksbehandlerIdent1, saksbehandlerIdent2)) } returns
            listOf(
                BatchedGroupsHitResponse(
                    navIdent = saksbehandlerIdent1,
                    groupIds = listOf(AzureGroup.KABAL_SAKSBEHANDLING.id),
                ),
                BatchedGroupsHitResponse(
                    navIdent = saksbehandlerIdent2,
                    groupIds = emptyList(),
                ),
            )
        every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = any(), fnr = any()) }.returns(true)
        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent1,
                    modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent2,
                    modifiedBy = "",
                ),
            ),
        )
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent1) }.returns(saksbehandler1PersonligInfo)
        every { klageLookupGateway.getGroupsForGivenNavIdent(any()) } returns SaksbehandlerGroups(emptyList())
        every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(any()) } returns emptySet()
        every { innstillingerService.findSaksbehandlerInnstillinger(ident = any()) } returns
            SaksbehandlerInnstillinger(
                anonymous = false,
            )

        val result =
            saksbehandlerService.getSaksbehandlere(
                fnr = fnr,
                ytelse = Ytelse.AAP_AAP,
                sakId = "abc",
                fagsystem = Fagsystem.AO01,
            )

        assertThat(result.saksbehandlere).containsExactly(saksbehandler1)
    }

    @Test
    fun `getSaksbehandlere forholder seg til persongalleri for FS36`() {
        val fortroligFnr = "FORTROLIG_FNR"

        every { klageLookupGateway.getPersongalleri(any()) }.returns(listOf(fortroligFnr, fnr))
        every { klageLookupGateway.getPerson(fnr) }.returns(person)
        every { klageLookupGateway.getPerson(fortroligFnr) }.returns(personFortrolig)
        every { klageLookupGateway.getUserGroupsBatched(listOf(saksbehandlerIdent1, saksbehandlerIdent2)) } returns
            batchedGroupsForSaksbehandlere(saksbehandlerIdent1, saksbehandlerIdent2)
        every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = any(), fnr = fnr) }.returns(true)
        every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = saksbehandlerIdent1, fnr = fortroligFnr) }.returns(false)
        every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = saksbehandlerIdent2, fnr = fortroligFnr) }.returns(true)
        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(Ytelse.FOR_FOR) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent1,
                    modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent2,
                    modifiedBy = "",
                ),
            ),
        )
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent1) }.returns(saksbehandler1PersonligInfo)
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent2) }.returns(saksbehandler2PersonligInfo)
        every { klageLookupGateway.getGroupsForGivenNavIdent(any()) } returns SaksbehandlerGroups(emptyList())
        every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(any()) } returns emptySet()
        every { innstillingerService.findSaksbehandlerInnstillinger(ident = any()) } returns
            SaksbehandlerInnstillinger(
                anonymous = false,
            )

        val result =
            saksbehandlerService.getSaksbehandlere(
                fnr = fnr,
                ytelse = Ytelse.FOR_FOR,
                sakId = "abc",
                fagsystem = Fagsystem.FS36,
            )

        assertThat(result.saksbehandlere).containsExactly(saksbehandler2)
    }

    @Test
    fun `getMedunderskrivere inneholder ikke innsender, men relevant medunderskriver`() {
        every { klageLookupGateway.getPerson(any()) }.returns(person)
        every { klageLookupGateway.getUserGroupsBatched(listOf(saksbehandlerIdent1, saksbehandlerIdent2)) } returns
            batchedGroupsForSaksbehandlere(saksbehandlerIdent1, saksbehandlerIdent2)
        every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = any(), fnr = any()) }.returns(true)
        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(Ytelse.AAP_AAP) }.returns(
            listOf(
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent1,
                    modifiedBy = "",
                ),
                SaksbehandlerAccess(
                    saksbehandlerIdent = saksbehandlerIdent2,
                    modifiedBy = "",
                ),
            ),
        )
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent1) }.returns(saksbehandler1PersonligInfo)
        every { klageLookupGateway.getUserInfoForGivenNavIdent(saksbehandlerIdent2) }.returns(saksbehandler2PersonligInfo)
        every { klageLookupGateway.getGroupsForGivenNavIdent(any()) } returns SaksbehandlerGroups(emptyList())
        every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(any()) } returns emptySet()
        every { innstillingerService.findSaksbehandlerInnstillinger(ident = any()) } returns
            SaksbehandlerInnstillinger(
                anonymous = false,
            )

        val result =
            saksbehandlerService.getMedunderskrivere(
                ident = saksbehandlerIdent1,
                ytelse = Ytelse.AAP_AAP,
                fnr = fnr,
                sakId = "abc",
                fagsystem = Fagsystem.AO01,
            )
        assertThat(result.medunderskrivere).doesNotContain(saksbehandler1)
        assertThat(result.medunderskrivere).contains(saksbehandler2)
    }

    @Test
    fun `Person med beskyttelsesbehov Strengt Fortrolig skal ikke ha medunderskriver`() {
        every { klageLookupGateway.getPerson(any()) }.returns(personStrengtFortrolig)

        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(any()) }.returns(
            emptyList(),
        )

        val result =
            saksbehandlerService.getMedunderskrivere(
                ident = saksbehandlerIdent1,
                ytelse = Ytelse.AAP_AAP,
                fnr = fnr,
                sakId = "abc",
                fagsystem = Fagsystem.AO01,
            )
        assertThat(result.medunderskrivere).isEmpty()
    }

    @Test
    fun `Person med beskyttelsesbehov Fortrolig skal ikke ha medunderskriver`() {
        every { klageLookupGateway.getPerson(any()) }.returns(personFortrolig)

        every { saksbehandlerAccessService.getAllSaksbehandlerAccessesForYtelse(any()) }.returns(
            emptyList(),
        )

        val result =
            saksbehandlerService.getMedunderskrivere(
                ident = saksbehandlerIdent1,
                ytelse = Ytelse.AAP_AAP,
                fnr = fnr,
                sakId = "abc",
                fagsystem = Fagsystem.AO01,
            )
        assertThat(result.medunderskrivere).isEmpty()
    }

    @Nested
    inner class GetROLList {
        @Test
        fun `getROLList inneholder relevante ROL for ytelse og fnr`() {
            val rolIdent = "ROL_IDENT"
            val rolName =
                SaksbehandlerPersonligInfo(
                    navIdent = rolIdent,
                    fornavn = "rol",
                    etternavn = "bruker",
                    sammensattNavn = "rol bruker",
                    enhet = SaksbehandlerEnhet(enhetId = "saksbehandlerEnhetId", navn = ""),
                )

            every { klageLookupGateway.getPerson(any()) }.returns(person)
            every { klageLookupGateway.getUsersInGroup(AzureGroup.KABAL_ROL) }.returns(
                listOf(
                    UserResponse(
                        navIdent = rolIdent,
                        fornavn = "rol",
                        etternavn = "bruker",
                        sammensattNavn = "rol bruker",
                    ),
                ),
            )
            every { klageLookupGateway.getUserGroupsBatched(listOf(rolIdent)) } returns batchedGroupsForROLs(rolIdent)
            every { tilgangService.hasSaksbehandlerAccessToPerson(navIdent = any(), fnr = any()) }.returns(true)
            every { klageLookupGateway.getUserInfoForGivenNavIdent(rolIdent) }.returns(rolName)
            every { klageLookupGateway.getGroupsForGivenNavIdent(any()) } returns SaksbehandlerGroups(emptyList())
            every { saksbehandlerAccessService.getSaksbehandlerAssignedYtelseSet(any()) } returns emptySet()
            every { innstillingerService.findSaksbehandlerInnstillinger(ident = any()) } returns
                SaksbehandlerInnstillinger(
                    anonymous = false,
                )

            val result =
                saksbehandlerService.getROLList(
                    fnr = fnr,
                    ytelse = Ytelse.AAP_AAP,
                    sakId = "abc",
                    fagsystem = Fagsystem.AO01,
                )
            assertThat(result.saksbehandlere).contains(
                Saksbehandler(navIdent = rolIdent, navn = "rol bruker", ansattEnhetId = "saksbehandlerEnhetId"),
            )
        }

        @Test
        fun `getROLList med person med Strengt Fortrolig beskyttelsesbehov returnerer tom liste`() {
            every { klageLookupGateway.getPerson(any()) }.returns(personStrengtFortrolig)
            every { klageLookupGateway.getUsersInGroup(AzureGroup.KABAL_ROL) }.returns(emptyList())

            val result =
                saksbehandlerService.getROLList(
                    fnr = fnr,
                    ytelse = Ytelse.AAP_AAP,
                    sakId = "abc",
                    fagsystem = Fagsystem.AO01,
                )
            assertThat(result.saksbehandlere).isEmpty()
        }
    }
}
