package no.nav.klage.oppgave.clients.axsys

import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.gateway.AxsysGateway
import org.springframework.stereotype.Service

@Service
class DefaultAxsysGateway(
    private val axsysClient: AxsysClient,
    private val tilgangerMapper: TilgangerMapper
) : AxsysGateway {

    @Deprecated("Erstattet av enhet i SaksbehandlerPersonligInfo som vi henter fra Azure")
    override fun getEnheterForSaksbehandler(ident: String): List<Enhet> =
        tilgangerMapper.mapTilgangerToEnheter(axsysClient.getTilgangerForSaksbehandler(ident))
}