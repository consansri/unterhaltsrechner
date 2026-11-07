package de.consansri.unterhaltsrechner.money

/** An amount together with the period it refers to, e.g. 1.200 € per year. Calculations use [perMonth]. */
internal data class PeriodicAmount(
    val amount: Euro = Euro.ZERO,
    val period: Period = Period.MONTH,
) {
    val perMonth: Euro get() = amount.dividedBy(period.months)

    enum class Period(val months: Int, val label: String) {
        MONTH(1, "Monat"),
        SEMESTER(6, "Semester"),
        YEAR(12, "Jahr"),
        ;

        override fun toString(): String = label
    }

    companion object {
        val ZERO = PeriodicAmount()

        fun monthly(amount: Euro) = PeriodicAmount(amount, Period.MONTH)
    }
}
