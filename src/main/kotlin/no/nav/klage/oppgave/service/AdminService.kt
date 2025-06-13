package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.ANSATTE_I_ENHET_CACHE
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.GROUPMEMBERS_CACHE
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.NAV_IDENT_TO_AZURE_GROUP_LIST_CACHE
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.NAV_IDENT_TO_AZURE_USER_CACHE
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.ROLLER_CACHE
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.SAKSBEHANDLERE_I_ENHET_CACHE
import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.TILGANGER_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service

@Service
class AdminService {

    @CacheEvict(
        cacheNames = [
            TILGANGER_CACHE,
            ROLLER_CACHE,
            SAKSBEHANDLERE_I_ENHET_CACHE,
            GROUPMEMBERS_CACHE,
            ANSATTE_I_ENHET_CACHE,
            NAV_IDENT_TO_AZURE_GROUP_LIST_CACHE,
            NAV_IDENT_TO_AZURE_USER_CACHE
        ],
        allEntries = true
    )
    fun evictAllCaches() {
    }

}