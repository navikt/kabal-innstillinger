package no.nav.klage.oppgave.exceptions

class MissingTilgangException(msg: String) : RuntimeException(msg)

class EnhetNotFoundForSaksbehandlerException(msg: String) : RuntimeException(msg)

class AbbreviationAlreadyExistsException(msg: String) : RuntimeException(msg)

class IllegalInputException(msg: String) : RuntimeException(msg)

class UserNotFoundException(msg: String) : RuntimeException(msg)

class GroupNotFoundException(msg: String) : RuntimeException(msg)

class EnhetNotFoundException(msg: String) : RuntimeException(msg)