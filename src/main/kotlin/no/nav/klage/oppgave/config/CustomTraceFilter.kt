package no.nav.klage.oppgave.config

import brave.Tracer
import brave.baggage.BaggageField
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * Adding some custom NAV-specific attributes to standard Spring Sleuth
 */
@Component
@Profile("!local")
@Order(-20)
class CustomTraceFilter(
    @Autowired private val tracer: Tracer,
    @Value("\${spring.application.name}") private val appName: String,
    @Value("\${navCallId}") private val navCallIdFieldName: String,
    @Value("\${navConsumerId}") private val navConsumerIdFieldName: String

) : GenericFilterBean() {


    override fun doFilter(
        request: ServletRequest?, response: ServletResponse,
        chain: FilterChain
    ) {
        val currentSpan = tracer.currentSpan()

        val navCallIdField = BaggageField.create(navCallIdFieldName)
        navCallIdField.updateValue(tracer.currentSpan().context(), currentSpan.context().traceIdString())

        val navConsumerIdField = BaggageField.create(navConsumerIdFieldName)
        navConsumerIdField.updateValue(tracer.currentSpan().context(), appName)

        chain.doFilter(request, response)
    }
}