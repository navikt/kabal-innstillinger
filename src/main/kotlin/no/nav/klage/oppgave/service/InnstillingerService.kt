package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseToHjemler
import no.nav.klage.kodeverk.ytelse.Ytelse
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import no.nav.klage.oppgave.domain.saksbehandler.entities.Innstillinger
import no.nav.klage.oppgave.repositories.InnstillingerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class InnstillingerService(
    private val innstillingerRepository: InnstillingerRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun findSaksbehandlerInnstillinger(
        ident: String,
    ): SaksbehandlerInnstillinger {
        return innstillingerRepository.findBySaksbehandlerident(ident)
            ?.toSaksbehandlerInnstillinger()
            ?: SaksbehandlerInnstillinger(anonymous = false)
    }

    fun storeInnstillingerButKeepSignature(
        navIdent: String,
        newSaksbehandlerInnstillinger: SaksbehandlerInnstillinger,
        assignedYtelseSet: Set<Ytelse>,
    ): SaksbehandlerInnstillinger {
        val oldInnstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)

        return innstillingerRepository.save(
            Innstillinger(
                saksbehandlerident = navIdent,
                hjemler = newSaksbehandlerInnstillinger.hjemler,
                ytelser = newSaksbehandlerInnstillinger.ytelser.filter { it in assignedYtelseSet }.toSet(),
                shortName = oldInnstillinger?.shortName,
                longName = oldInnstillinger?.longName,
                jobTitle = oldInnstillinger?.jobTitle,
                modified = LocalDateTime.now(),
                anonymous = oldInnstillinger?.anonymous ?: false
            )
        ).toSaksbehandlerInnstillinger()
    }

    fun updateYtelseAndHjemmelInnstillinger(
        navIdent: String,
        inputYtelseSet: Set<Ytelse>,
        assignedYtelseSet: Set<Ytelse>,
    ) {
        val filteredYtelseSet = inputYtelseSet.filter { it in assignedYtelseSet }.toSet()

        if (!innstillingerRepository.existsById(navIdent)) {
            val hjemmelSet = getUpdatedHjemmelSet(
                ytelserToAdd = filteredYtelseSet,
            )

            innstillingerRepository.save(
                Innstillinger(
                    saksbehandlerident = navIdent,
                    hjemler = hjemmelSet,
                    ytelser = filteredYtelseSet,
                    shortName = null,
                    longName = null,
                    jobTitle = null,
                    modified = LocalDateTime.now(),
                    anonymous = false,
                )
            )
        } else {
            val existingInnstillinger = findSaksbehandlerInnstillinger(
                ident = navIdent,
            )

            val existingInnstillingerYtelseSet = existingInnstillinger.ytelser.toSet()
            val existingHjemmelSet = existingInnstillinger.hjemler.toSet()

            val ytelserToAdd = getYtelserToAdd(
                inputYtelser = inputYtelseSet,
                existingInnstillingerYtelser = existingInnstillingerYtelseSet
            )
            val ytelserToKeep = getYtelserToKeep(
                inputYtelser = inputYtelseSet,
                existingInnstillingerYtelser = existingInnstillingerYtelseSet
            )

            val hjemmelSet = getUpdatedHjemmelSet(
                ytelserToAdd = ytelserToAdd, ytelserToKeep = ytelserToKeep, existingHjemler = existingHjemmelSet
            )

            innstillingerRepository.getReferenceById(navIdent).apply {
                ytelser = filteredYtelseSet
                hjemler = hjemmelSet
                modified = LocalDateTime.now()
            }
        }
    }

    fun storeShortName(navIdent: String, shortName: String?) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.shortName = shortName
    }

    fun storeLongName(navIdent: String, longName: String?) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.longName = longName
    }

    fun storeJobTitle(navIdent: String, jobTitle: String?) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.jobTitle = jobTitle
    }

    fun storeAnonymous(navIdent: String, anonymous: Boolean) {
        val innstillinger = getOrCreateInnstillinger(navIdent)
        innstillinger.anonymous = anonymous
    }

    private fun getOrCreateInnstillinger(navIdent: String): Innstillinger {
        var innstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)
        if (innstillinger == null) {
            innstillinger = innstillingerRepository.save(
                Innstillinger(
                    saksbehandlerident = navIdent,
                    anonymous = false,
                )
            )
        }
        return innstillinger
    }

    private fun getUpdatedHjemmelSet(
        ytelserToAdd: Set<Ytelse>,
        ytelserToKeep: Set<Ytelse>? = null,
        existingHjemler: Set<Hjemmel>? = null,
    ): MutableSet<Hjemmel> {
        val hjemmelSet = mutableSetOf<Hjemmel>()

        ytelserToAdd.forEach { ytelse ->
            ytelseToHjemler[ytelse]?.let { hjemmelSet.addAll(it) }
        }

        if (ytelserToKeep != null && existingHjemler != null) {
            for (hjemmel in existingHjemler) {
                for (ytelse in ytelserToKeep) {
                    if (ytelseToHjemler[ytelse]?.contains(hjemmel) == true) {
                        hjemmelSet.add(hjemmel)
                        break
                    }
                }
            }
        }

        return hjemmelSet
    }

    private fun getYtelserToAdd(
        inputYtelser: Set<Ytelse>,
        existingInnstillingerYtelser: Set<Ytelse> = emptySet()
    ): Set<Ytelse> {
        return inputYtelser.filter { it !in existingInnstillingerYtelser }.toSet()
    }

    private fun getYtelserToKeep(inputYtelser: Set<Ytelse>, existingInnstillingerYtelser: Set<Ytelse>): Set<Ytelse> {
        return inputYtelser.intersect(existingInnstillingerYtelser)
    }

    fun deleteInnstillingerForSaksbehandler(navIdent: String): String {
        val output = "Deleting innstillinger for saksbehandler $navIdent"
        innstillingerRepository.deleteById(navIdent)
        return output + "\n"
    }

    //For admin endpoint, use when adding new hjemler to existing ytelse.
    fun addHjemlerForYtelse(
        ytelse: Ytelse,
        hjemmelList: List<Hjemmel>,
    ) {
        val hjemlerForYtelse = ytelseToHjemler[ytelse]
        if (hjemlerForYtelse == null) {
            error("Mangler hjemmelliste for ytelse $ytelse")
        } else if (!hjemlerForYtelse.containsAll(hjemmelList)) {
            error("En eller flere av hjemlene er ikke lagt til for ytelsen $ytelse i kodeverket.")
        }

        val allInnstillinger = innstillingerRepository.findAll()
        allInnstillinger.forEach { innstilling ->
            val saksbehandlerInnstilling = innstilling.toSaksbehandlerInnstillinger()
            if (saksbehandlerInnstilling.ytelser.contains(ytelse)) {
                val existingHjemmelSet = saksbehandlerInnstilling.hjemler.toSet()
                val hjemlerToAdd = hjemmelList.filter { hjemmel ->
                    !existingHjemmelSet.contains(hjemmel)
                }.toSet()
                val newHjemmelSet = existingHjemmelSet + hjemlerToAdd

                if (newHjemmelSet != existingHjemmelSet) {
                    logger.debug(
                        "Lagrer nytt hjemmelsett {} for saksbehandler {}",
                        newHjemmelSet,
                        innstilling.saksbehandlerident
                    )
                    innstilling.apply {
                        hjemler = newHjemmelSet
                    }
                } else {
                    logger.debug("Ingen nye hjemler å lagre for saksbehandler {}", innstilling.saksbehandlerident)
                }
            }
        }
    }

    fun getAllRegisteredHjemlerForYtelse(ytelse: Ytelse): Set<Hjemmel> {
        val relevantInnstillinger = innstillingerRepository.findByYtelserContaining(ytelse = ytelse)
        return relevantInnstillinger.flatMap { it.hjemler }.toSet()
    }
}