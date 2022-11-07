package no.nav.klage.oppgave.service.unleash

import no.finn.unleash.strategy.Strategy
import no.nav.klage.oppgave.repositories.InnloggetAnsattRepository
import org.springframework.stereotype.Component

@Component
class ByUserStrategy(private val innloggetAnsattRepository: InnloggetAnsattRepository) : Strategy {

    companion object {
        const val PARAM = "user"
    }

    override fun getName(): String = "byUserId"

    override fun isEnabled(parameters: MutableMap<String, String>): Boolean =
        getEnabledUsers(parameters)?.any { isCurrentUserEnabled(it) } ?: false

    private fun getEnabledUsers(parameters: Map<String, String>?) =
        parameters?.get(PARAM)?.split(',')

    private fun isCurrentUserEnabled(ident: String): Boolean {
        return ident == innloggetAnsattRepository.getInnloggetIdent()
    }

}