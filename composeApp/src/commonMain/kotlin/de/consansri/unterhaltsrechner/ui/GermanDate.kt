package de.consansri.unterhaltsrechner.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/** Date in German format, e.g. `01.01.2026`. */
internal fun LocalDate.formatGerman(): String =
    "${day.toString().padStart(2, '0')}.${month.number.toString().padStart(2, '0')}.$year"
