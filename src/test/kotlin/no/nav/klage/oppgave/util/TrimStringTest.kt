package no.nav.klage.oppgave.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TrimStringTest {

    @Test
    fun `test trimming`() {
        assertThat(" a ".trimToNull()).isEqualTo("a")
        assertThat("a ".trimToNull()).isEqualTo("a")
        assertThat(" a".trimToNull()).isEqualTo("a")
        assertThat(" a a ".trimToNull()).isEqualTo("a a")
        assertThat("\na a".trimToNull()).isEqualTo("a a")

        assertThat("".trimToNull()).isNull()
        assertThat(" ".trimToNull()).isNull()
        assertThat("\n".trimToNull()).isNull()
    }

}