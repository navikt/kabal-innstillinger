package no.nav.klage.oppgave.domain.saksbehandler.entities

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import no.nav.klage.kodeverk.ytelse.Ytelse

@Converter
class YtelseConverter : AttributeConverter<Ytelse, String?> {

    override fun convertToDatabaseColumn(entity: Ytelse?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Ytelse? =
        id?.let { Ytelse.of(it) }
}