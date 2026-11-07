package de.consansri.unterhaltsrechner.calculation

internal enum class Parent(val label: String) {
    FATHER("Vater"),
    MOTHER("Mutter"),
    ;

    override fun toString() = label
}
