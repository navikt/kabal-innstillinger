package no.nav.klage.oppgave.config.problem

import no.nav.klage.oppgave.exceptions.*
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getTeamLogger
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ProblemHandlingControllerAdvice : ResponseEntityExceptionHandler() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val ourLogger = getLogger(javaClass.enclosingClass)
        private val teamLogger = getTeamLogger()
    }

    @ExceptionHandler
    fun handleMissingTilgang(ex: MissingTilgangException, request: NativeWebRequest): ProblemDetail =
        create(HttpStatus.FORBIDDEN, ex)

    @ExceptionHandler
    fun handleResponseHttpStatusException(
        ex: WebClientResponseException,
    ): ProblemDetail =
        createProblemForWebClientResponseException(ex)

    @ExceptionHandler
    fun handleEnhetNotFoundException(
        ex: EnhetNotFoundException,
    ): ProblemDetail =
        create(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
    ): ProblemDetail =
        create(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler
    fun handleGroupNotFoundException(
        ex: GroupNotFoundException,
    ): ProblemDetail =
        create(HttpStatus.NOT_FOUND, ex)

    @ExceptionHandler
    fun handleAbbreviationAlreadyExistsException(
        ex: AbbreviationAlreadyExistsException,
    ): ProblemDetail =
        create(HttpStatus.CONFLICT, ex)

    @ExceptionHandler
    fun handleIllegalInputException(
        ex: IllegalInputException,
    ): ProblemDetail =
        create(HttpStatus.BAD_REQUEST, ex)

    private fun createProblemForWebClientResponseException(ex: WebClientResponseException): ProblemDetail {
        logError(
            httpStatus = HttpStatus.valueOf(ex.statusCode.value()),
            errorMessage = ex.statusText,
            exception = ex
        )

        return ProblemDetail.forStatus(ex.statusCode).apply {
            title = ex.statusText
            detail = ex.responseBodyAsString
        }
    }

    private fun create(httpHttpStatus: HttpStatus, ex: Exception): ProblemDetail {
        val errorMessage = ex.message ?: "No error message available"

        logError(
            httpStatus = httpHttpStatus,
            errorMessage = errorMessage,
            exception = ex
        )

        return ProblemDetail.forStatusAndDetail(httpHttpStatus, errorMessage).apply {
            title = errorMessage
        }
    }

    private fun logError(httpStatus: HttpStatus, errorMessage: String, exception: Exception) {
        when {
            httpStatus.is5xxServerError -> {
                ourLogger.error("Exception thrown to client: ${exception.javaClass.name}. See team-logs for more details.")
                teamLogger.error("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }

            else -> {
                ourLogger.warn("Exception thrown to client: ${exception.javaClass.name}. See team-logs for more details.")
                teamLogger.warn("Exception thrown to client: ${httpStatus.reasonPhrase}, $errorMessage", exception)
            }
        }
    }
}