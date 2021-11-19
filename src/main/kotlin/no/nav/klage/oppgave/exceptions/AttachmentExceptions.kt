package no.nav.klage.oppgave.exceptions

class AttachmentTooLargeException(override val message: String = "TOO_LARGE") : RuntimeException() {
    @Synchronized
    fun fillInStackTrace(): Throwable {
        //Remove stacktrace
        return this
    }
}

class AttachmentEncryptedException(override val message: String = "ENCRYPTED") : RuntimeException()
class AttachmentIsEmptyException(override val message: String = "EMPTY") : RuntimeException()
class AttachmentHasVirusException(override val message: String = "VIRUS") : RuntimeException()
