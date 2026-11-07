package de.consansri.unterhaltsrechner.money

import kotlin.jvm.JvmInline
import kotlin.math.abs

/** Percentage with two decimals, stored in hundredths of a percent (10000 = 100,00 %). For display only. */
@JvmInline
internal value class Percent(val hundredths: Long) {

    override fun toString(): String {
        val whole = hundredths / 100
        val decimals = abs(hundredths) % 100
        return "$whole,${decimals.toString().padStart(2, '0')} %"
    }

    companion object {
        /** Share of [part] in [total], rounded; `null` if [total] is 0. */
        fun of(part: Euro, total: Euro): Percent? =
            if (total.cents == 0L) null else Percent(Euro.roundedDivision(part.cents * 10_000, total.cents))
    }
}
