package de.consansri.unterhaltsrechner.ui

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class GermanDateTest {

    @Test
    fun formatsDayMonthYear() {
        assertEquals("04.11.2026", LocalDate(2026, 11, 4).formatGerman())
    }
}
