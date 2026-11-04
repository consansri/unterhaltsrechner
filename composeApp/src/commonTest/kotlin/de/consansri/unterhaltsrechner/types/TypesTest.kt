package de.consansri.unterhaltsrechner.types

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class EuroTest {

    private fun cents(eingabe: String): Long? = Euro.parse(eingabe)?.inCents

    @Test
    fun parstDeutscheUndEinfacheFormate() {
        assertEquals(99_000L, cents("990"))
        assertEquals(1_250L, cents("12,50"))
        assertEquals(1_250L, cents("12,5"))
        assertEquals(1_250L, cents("12.5"), "Punkt mit 1–2 Nachkommastellen ist Dezimaltrennzeichen")
        assertEquals(123_456L, cents("1.234,56"))
        assertEquals(123_400L, cents("1.234"), "Punkt in Dreiergruppe ist Tausendertrennzeichen")
        assertEquals(123_456_700L, cents("1.234.567"))
        assertEquals(8_333L, cents("83,33 €"))
        assertEquals(1_200L, cents("12,"), "Zwischenstand beim Tippen")
        assertEquals(50L, cents(",5"))
        assertEquals(0L, cents(""), "Leeres Feld gilt als 0")
        assertEquals(0L, cents("   "))
        assertEquals(100L, cents("001"))
    }

    @Test
    fun lehntUngueltigeEingabenAb() {
        assertNull(cents("NaN"))
        assertNull(cents("Infinity"))
        assertNull(cents("-5"))
        assertNull(cents("12,345"), "mehr als zwei Nachkommastellen")
        assertNull(cents("1e5"))
        assertNull(cents("abc"))
        assertNull(cents(","))
        assertNull(cents("1,2,3"))
        assertNull(cents("12345678"), "zu groß")
        assertIs<Euro.Eingabe.Ungueltig>(Euro.pruefe("-5"))
    }

    @Test
    fun formatiert() {
        assertEquals("1.234,56 €", Euro(123_456).toString())
        assertEquals("0,05 €", Euro(5).toString())
        assertEquals("-12,30 €", Euro(-1_230).toString())
        assertEquals("990,00 €", 990.euro.toString())
        assertEquals("990", 990.euro.alsEingabe())
        assertEquals("1234,50", Euro(123_450).alsEingabe())
    }

    @Test
    fun rundetKaufmaennisch() {
        assertEquals(Euro(8_333), 1_000.euro.geteiltDurch(12))
        assertEquals(Euro(1_667), 100.euro.geteiltDurch(6))
        assertEquals(Euro(1), Euro(5).geteiltDurch(10), "0,5 Cent wird aufgerundet")
        assertEquals(Euro(-1), Euro(-5).geteiltDurch(10))
        assertEquals(Euro(60_917), 731.euro.anteil(1_250.euro, 1_500.euro))
    }
}

class ZBetragTest {
    @Test
    fun rechnetAufMonatUm() {
        assertEquals(Euro(8_333), ZBetrag(1_000.euro, Zeitraum.JAHR).proMonat)
        assertEquals(Euro(1_667), ZBetrag(100.euro, Zeitraum.SEMESTER).proMonat)
        assertEquals(500.euro, ZBetrag(500.euro, Zeitraum.MONAT).proMonat)
    }
}

class ProzentTest {
    @Test
    fun anteil() {
        assertEquals("83,33 %", Prozent.anteil(1_250.euro, 1_500.euro).toString())
        assertEquals("100,00 %", Prozent.anteil(250.euro, 250.euro).toString())
        assertNull(Prozent.anteil(Euro.NULL, Euro.NULL))
    }
}

class JahrTest {
    @Test
    fun waehltDenGeltendenRechtsstand() {
        assertEquals(Jahr.J2026, Jahr.fuer(LocalDate(2026, 11, 4)))
        assertEquals(Jahr.J2025, Jahr.fuer(LocalDate(2025, 12, 31)))
        assertEquals(Jahr.J2026, Jahr.fuer(LocalDate(2026, 1, 1)))
        assertEquals(Jahr.J2027, Jahr.fuer(LocalDate(2027, 1, 1)))
        assertEquals(Jahr.J2027, Jahr.fuer(LocalDate(2030, 6, 1)), "danach gilt der neueste Stand")
        assertEquals(Jahr.J2023, Jahr.fuer(LocalDate(2020, 1, 1)), "davor gilt der älteste Stand")
    }

    @Test
    fun tabellenwerte() {
        assertEquals(990.euro, Jahr.J2026.bedarfStudent)
        assertEquals(930.euro, Jahr.J2024.bedarfStudent)
        assertEquals(Rechtsstatus.VORLAEUFIG, Jahr.J2027.status)
        Jahr.entries.filter { it.jahr <= 2026 }.forEach { assertEquals(Rechtsstatus.AMTLICH, it.status, "$it") }
        assertEquals(Jahr.entries.sortedBy { it.jahr }, Jahr.entries, "Einträge müssen aufsteigend sortiert sein")
    }

    @Test
    fun datumsformat() {
        assertEquals("04.11.2026", LocalDate(2026, 11, 4).alsDatum())
    }
}
