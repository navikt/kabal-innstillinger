package no.nav.klage.oppgave.config


import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import java.util.concurrent.TimeUnit
import javax.cache.CacheManager
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration


@EnableCaching
@Configuration
class CacheWithJCacheConfiguration(private val environment: Environment) : JCacheManagerCustomizer {

    companion object {

        const val TILGANGER_CACHE = "tilganger"
        const val ROLLER_CACHE = "roller"
        const val SAKSBEHANDLERE_I_ENHET_CACHE = "saksbehandlereienhet"
        const val GROUPMEMBERS_CACHE = "groupmembers"
        const val ANSATTE_I_ENHET_CACHE = "ansatteienhet"
        const val NAV_IDENT_TO_AZURE_GROUP_LIST_CACHE = "navidenttoazuregrouplist"
        const val NAV_IDENT_TO_AZURE_USER_CACHE = "navidenttoazureuser"

        val cacheKeys =
            listOf(
                TILGANGER_CACHE,
                ROLLER_CACHE,
                SAKSBEHANDLERE_I_ENHET_CACHE,
                GROUPMEMBERS_CACHE,
                ANSATTE_I_ENHET_CACHE,
                NAV_IDENT_TO_AZURE_GROUP_LIST_CACHE,
                NAV_IDENT_TO_AZURE_USER_CACHE
            )

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun customize(cacheManager: CacheManager) {
        cacheKeys.forEach { cacheName ->
            cacheManager.createCache(cacheName, cacheConfiguration())
        }
    }

    private fun cacheConfiguration() =
        MutableConfiguration<Any, Any>()
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration(TimeUnit.MINUTES, duration())))
            .setStoreByValue(false)
            .setStatisticsEnabled(true)

    private fun duration() =
        if (environment.activeProfiles.contains("prod-gcp")) {
            480L
        } else {
            10L
        }

}
