package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseTilHjemler
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerInnstillinger
import no.nav.klage.oppgave.domain.saksbehandler.entities.Innstillinger
import no.nav.klage.oppgave.repositories.InnstillingerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
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
        const val SEPARATOR = ","
        @Value("\${DELETE_EXPIRED_DRY_RUN}")
        private var deleteExpiredDryRun: Boolean = true
    }

    fun findSaksbehandlerInnstillinger(
        ident: String,
    ): SaksbehandlerInnstillinger {
        return innstillingerRepository.findBySaksbehandlerident(ident)
            ?.toSaksbehandlerInnstillinger()
            ?: SaksbehandlerInnstillinger()
    }

    fun storeInnstillingerButKeepSignature(
        navIdent: String,
        newSaksbehandlerInnstillinger: SaksbehandlerInnstillinger,
        assignedYtelseList: List<Ytelse>,
    ): SaksbehandlerInnstillinger {
        val oldInnstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)

        return innstillingerRepository.save(
            Innstillinger(
                saksbehandlerident = navIdent,
                hjemler = newSaksbehandlerInnstillinger.hjemler.joinToString(SEPARATOR) { it.id },
                ytelser = newSaksbehandlerInnstillinger.ytelser.filter { it in assignedYtelseList }
                    .joinToString(SEPARATOR) { it.id },
                typer = newSaksbehandlerInnstillinger.typer.joinToString(SEPARATOR) { it.id },
                shortName = oldInnstillinger?.shortName,
                longName = oldInnstillinger?.longName,
                jobTitle = oldInnstillinger?.jobTitle,
                modified = LocalDateTime.now()
            )
        ).toSaksbehandlerInnstillinger()
    }

    fun updateYtelseAndHjemmelInnstillinger(
        navIdent: String,
        inputYtelseSet: Set<Ytelse>,
        assignedYtelseList: List<Ytelse>,
    ) {
        val filteredYtelseList = inputYtelseSet.filter { it in assignedYtelseList }

        if (!innstillingerRepository.existsById(navIdent)) {
            val hjemmelSet = getUpdatedHjemmelSet(
                ytelserToAdd = filteredYtelseList.toSet()
            )

            innstillingerRepository.save(
                Innstillinger(
                    saksbehandlerident = navIdent,
                    hjemler = hjemmelSet
                        .joinToString(SEPARATOR) { it.id },
                    ytelser = filteredYtelseList
                        .joinToString(SEPARATOR) { it.id },
                    typer = "",
                    shortName = null,
                    longName = null,
                    jobTitle = null,
                    modified = LocalDateTime.now()
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
                ytelser = filteredYtelseList
                    .joinToString(SEPARATOR) { it.id }
                hjemler = hjemmelSet
                    .joinToString(SEPARATOR) { it.id }
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

    private fun getOrCreateInnstillinger(navIdent: String): Innstillinger {
        var innstillinger = innstillingerRepository.findBySaksbehandlerident(navIdent)
        if (innstillinger == null) {
            innstillinger = innstillingerRepository.save(
                Innstillinger(
                    saksbehandlerident = navIdent,
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
            ytelseTilHjemler[ytelse]?.let { hjemmelSet.addAll(it) }
        }

        if (ytelserToKeep != null && existingHjemler != null) {
            for (hjemmel in existingHjemler) {
                for (ytelse in ytelserToKeep) {
                    if (ytelseTilHjemler[ytelse]?.contains(hjemmel) == true) {
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
        var output = "Deleting innstillinger for saksbehandler $navIdent"
        if (!deleteExpiredDryRun) {
            output += "Actually deleting innstillinger for ident $navIdent"
//        innstillingerRepository.deleteById(navIdent)
        }
        return output + "\n"
    }
}