package no.nav.klage.oppgave.service.unleash

import no.finn.unleash.UnleashContext
import no.finn.unleash.strategy.Strategy
import no.nav.klage.oppgave.gateway.AxsysGateway
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component

@Component
class ByEnhetStrategy(private val axsysGateway: AxsysGateway) : Strategy {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        const val PARAM = "valgtEnhet"
    }

    override fun getName(): String {
        return "byEnhet"
    }

    override fun isEnabled(parameters: MutableMap<String, String>): Boolean {
        return false
    }

    override fun isEnabled(parameters: Map<String, String>, unleashContext: UnleashContext): Boolean =
        try {
            unleashContext.userId.map {
                val saksbehandlersEnheter: List<String> = getSaksbehandlersEnheter(unleashContext)
                val enabledEnheter: List<String>? = getEnabledEnheter(parameters)
                enabledEnheter?.intersect(saksbehandlersEnheter)?.isNotEmpty() ?: false
            }.orElse(false)
        } catch (ex: Exception) {
            logger.warn("Unable to retrieve saksbehandlers enheter", ex)
            false
        }

    private fun getSaksbehandlersEnheter(unleashContext: UnleashContext): List<String> {
        logger.debug("Getting saksbehandlers enheter")
        return axsysGateway.getEnheterForSaksbehandler(unleashContext.userId.get()).map { it.enhetId }
    }

    private fun getEnabledEnheter(parameters: Map<String, String>?): List<String>? =
        parameters?.get(PARAM)?.split(',')

}