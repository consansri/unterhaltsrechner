package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.euro
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LegalYearTest {

    @Test
    fun picksTheLegalYearValidOnADate() {
        assertEquals(LegalYear.Y2026, LegalYear.at(LocalDate(2026, 11, 4)))
        assertEquals(LegalYear.Y2025, LegalYear.at(LocalDate(2025, 12, 31)))
        assertEquals(LegalYear.Y2026, LegalYear.at(LocalDate(2026, 1, 1)))
        assertEquals(LegalYear.Y2027, LegalYear.at(LocalDate(2027, 1, 1)))
        assertEquals(LegalYear.Y2027, LegalYear.at(LocalDate(2030, 6, 1)), "later dates use the newest entry")
        assertEquals(LegalYear.Y2023, LegalYear.at(LocalDate(2020, 1, 1)), "earlier dates use the oldest entry")
    }

    @Test
    fun tableValues() {
        assertEquals(990.euro, LegalYear.Y2026.studentNeed)
        assertEquals(930.euro, LegalYear.Y2024.studentNeed)
        assertIs<LegalYear.Status.Provisional>(LegalYear.Y2027.status)
        LegalYear.entries.filter { it.year <= 2026 }.forEach { assertEquals(LegalYear.Status.Official, it.status, "$it") }
        assertEquals(LegalYear.entries.sortedBy { it.year }, LegalYear.entries, "entries must be sorted ascending")
    }
}
