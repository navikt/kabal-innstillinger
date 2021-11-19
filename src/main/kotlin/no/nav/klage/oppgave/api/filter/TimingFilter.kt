package no.nav.klage.oppgave.api.filter

import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component
import java.io.IOException
import java.time.Duration
import java.time.Instant
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest


@Component
@WebFilter("/klagebehandlinger/*")
class TimingFilter : Filter {

    companion object {
        private val logger = getLogger(TimingFilter::class.java)
    }

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        // empty
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, resp: ServletResponse, chain: FilterChain) {
        val start = Instant.now()
        try {
            chain.doFilter(req, resp)
        } finally {
            val finish = Instant.now()
            val time = Duration.between(start, finish).toMillis()
            logger.debug("{}: {} ms ", (req as HttpServletRequest).requestURI, time)
        }
    }

    override fun destroy() {
        // empty
    }
}