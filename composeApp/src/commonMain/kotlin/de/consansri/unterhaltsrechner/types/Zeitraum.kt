package de.consansri.unterhaltsrechner.types

enum class Zeitraum(val monate: Int, val suffix: String) {
    MONAT(1, "/ M"),
    SEMESTER(6, "/ Sem"),
    JAHR(12, "/ J");

    // Wechselt zum nächsten Zeitraum (praktisch für den Klick im UI)
    fun next(): Zeitraum {
        val values = entries.toTypedArray()
        return values[(this.ordinal + 1) % values.size]
    }
}