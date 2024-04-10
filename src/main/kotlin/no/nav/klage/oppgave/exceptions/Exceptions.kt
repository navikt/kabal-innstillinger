package no.nav.klage.oppgave.exceptions

class MissingTilgangException(msg: String) : RuntimeException(msg)

class EnhetNotFoundForSaksbehandlerException(msg: String) : RuntimeException(msg)

class AbbreviationAlreadyExistsException(msg: String) : RuntimeException(msg)