package de.consansri.unterhaltsrechner.money

import de.consansri.unterhaltsrechner.money.PeriodicAmount.Period
import kotlin.test.Test
import kotlin.test.assertEquals

class PeriodicAmountTest {

    @Test
    fun convertsToMonthly() {
        assertEquals(Euro(8_333), PeriodicAmount(1_000.euro, Period.YEAR).perMonth)
        assertEquals(Euro(1_667), PeriodicAmount(100.euro, Period.SEMESTER).perMonth)
        assertEquals(500.euro, PeriodicAmount(500.euro, Period.MONTH).perMonth)
    }
}
