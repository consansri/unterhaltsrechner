package de.consansri.unterhaltsrechner.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PercentTest {

    @Test
    fun shareOfTotal() {
        assertEquals("83,33 %", Percent.of(1_250.euro, 1_500.euro).toString())
        assertEquals("100,00 %", Percent.of(250.euro, 250.euro).toString())
        assertNull(Percent.of(Euro.ZERO, Euro.ZERO))
    }
}
