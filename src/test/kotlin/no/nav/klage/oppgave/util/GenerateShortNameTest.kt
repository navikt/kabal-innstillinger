package no.nav.klage.oppgave.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GenerateShortNameTest {

    @Test
    fun `basic name works`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "Kalle",
                etternavn = "Anka",
            )
        ).isEqualTo("K. Anka")
    }

    @Test
    fun `two fornavn works`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "Kalle Peder",
                etternavn = "Anka",
            )
        ).isEqualTo("K.P. Anka")
    }

    @Test
    fun `three fornavn works`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "Kalle Peder Petter",
                etternavn = "Anka",
            )
        ).isEqualTo("K.P.P. Anka")
    }

    @Test
    fun `etternavn is left unchanged`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "Kalle",
                etternavn = "Anka-And",
            )
        ).isEqualTo("K. Anka-And")
    }

    @Test
    fun `etternavn is left unchanged 2`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "Kalle",
                etternavn = "Anka And",
            )
        ).isEqualTo("K. Anka And")
    }

    @Test
    fun `etternavn is left unchanged 3`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "Kalle",
                etternavn = "av Ankeborg",
            )
        ).isEqualTo("K. av Ankeborg")
    }

    @Test
    fun `fornavn with lowercase gives null`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "kalle",
                etternavn = "de España",
            )
        ).isNull()
    }

    @Test
    fun `fornavn with lowercase gives null 2`() {
        assertThat(
            generateShortNameOrNull(
                fornavn = "kalle Fnatte",
                etternavn = "de España",
            )
        ).isNull()
    }
}