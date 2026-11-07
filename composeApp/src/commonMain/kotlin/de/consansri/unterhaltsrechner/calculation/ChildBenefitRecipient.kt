package de.consansri.unterhaltsrechner.calculation

/** Who receives the child benefit (Kindergeld). A parent receiving it must pass it on to the child. */
internal enum class ChildBenefitRecipient(val label: String, val parent: Parent?) {
    CHILD("Kind", null),
    FATHER("Vater", Parent.FATHER),
    MOTHER("Mutter", Parent.MOTHER),
    ;

    override fun toString() = label
}
