package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.nom.NomClient
import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.SaksbehandlerAccessRepository
import no.nav.klage.oppgave.util.RoleUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class SaksbehandlerAccessServiceTest {
    private val saksbehandlerAccessRepository: SaksbehandlerAccessRepository = mockk()
    private val innstillingerService: InnstillingerService = mockk()
    private val roleUtils: RoleUtils = mockk()
    private val azureGateway: AzureGateway = mockk()
    private val nomClient: NomClient = mockk()

    private val saksbehandlerAccessService = SaksbehandlerAccessService(
        saksbehandlerAccessRepository = saksbehandlerAccessRepository,
        innstillingerService = innstillingerService,
        roleUtils = roleUtils,
        azureGateway = azureGateway,
        nomClient = nomClient,
    )

    private val ident = "ident"
    private val sammensattNavn = "sammensattNavn"

    @Nested
    inner class GetSaksbehandlerAccessView {
        @Test
        fun `does not exists, verifies saksbehandlerName`() {
            every { saksbehandlerAccessRepository.existsById(any()) } returns false
            every { azureGateway.getDataOmSaksbehandler(any()) } returns SaksbehandlerPersonligInfo(
                fornavn = "",
                etternavn = "",
                sammensattNavn = sammensattNavn,
                enhet = Enhet(enhetId = "", navn = "")
            )

            assertEquals(
                emptyList<String>(),
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).ytelseIdList
            )
            assertEquals(
                ident,
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).saksbehandlerIdent
            )
            assertEquals(
                sammensattNavn,
                saksbehandlerAccessService.getSaksbehandlerAccessView(saksbehandlerIdent = ident).saksbehandlerName
            )
        }
    }
}