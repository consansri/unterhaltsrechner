package de.consansri.unterhaltsrechner.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class EuroTest {

    private fun cents(input: String): Long? = (Euro.parse(input) as? Euro.ParseResult.Valid)?.value?.cents

    private fun errorOf(input: String): Euro.ParseError? = (Euro.parse(input) as? Euro.ParseResult.Invalid)?.error

    @Test
    fun parsesGermanAndPlainFormats() {
        assertEquals(99_000L, cents("990"))
        assertEquals(1_250L, cents("12,50"))
        assertEquals(1_250L, cents("12,5"))
        assertEquals(1_250L, cents("12.5"), "a dot with 1–2 decimals is a decimal separator")
        assertEquals(123_456L, cents("1.234,56"))
        assertEquals(123_400L, cents("1.234"), "a dot before three digits is a thousands separator")
        assertEquals(123_456_700L, cents("1.234.567"))
        assertEquals(8_333L, cents("83,33 €"))
        assertEquals(1_200L, cents("12,"), "intermediate state while typing")
        assertEquals(50L, cents(",5"))
        assertEquals(0L, cents(""), "an empty field counts as 0")
        assertEquals(0L, cents("   "))
        assertEquals(100L, cents("001"))
    }

    @Test
    fun rejectsInvalidInputWithTheMatchingError() {
        assertEquals(Euro.ParseError.MALFORMED, errorOf("NaN"))
        assertEquals(Euro.ParseError.MALFORMED, errorOf("Infinity"))
        assertEquals(Euro.ParseError.NEGATIVE, errorOf("-5"))
        assertEquals(Euro.ParseError.TOO_MANY_DECIMALS, errorOf("12,345"))
        assertEquals(Euro.ParseError.MALFORMED, errorOf("1e5"))
        assertEquals(Euro.ParseError.MALFORMED, errorOf("abc"))
        assertEquals(Euro.ParseError.MALFORMED, errorOf(","))
        assertEquals(Euro.ParseError.MALFORMED, errorOf("1,2,3"))
        assertEquals(Euro.ParseError.TOO_LARGE, errorOf("12345678"))
        assertIs<Euro.ParseResult.Invalid>(Euro.parse("-5"))
        assertNull(cents("abc"))
    }

    @Test
    fun formats() {
        assertEquals("1.234,56 €", Euro(123_456).toString())
        assertEquals("0,05 €", Euro(5).toString())
        assertEquals("-12,30 €", Euro(-1_230).toString())
        assertEquals("990,00 €", 990.euro.toString())
        assertEquals("990", 990.euro.toInputText())
        assertEquals("1234,50", Euro(123_450).toInputText())
    }

    @Test
    fun roundsHalfAwayFromZero() {
        assertEquals(Euro(8_333), 1_000.euro.dividedBy(12))
        assertEquals(Euro(1_667), 100.euro.dividedBy(6))
        assertEquals(Euro(1), Euro(5).dividedBy(10), "0,5 cent rounds up")
        assertEquals(Euro(-1), Euro(-5).dividedBy(10))
        assertEquals(Euro(60_917), 731.euro.proportion(1_250.euro, 1_500.euro))
    }
}
