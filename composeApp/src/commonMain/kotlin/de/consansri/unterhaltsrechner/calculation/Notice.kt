package de.consansri.unterhaltsrechner.calculation

import de.consansri.unterhaltsrechner.money.Euro

internal sealed interface Notice {
    /** The year's values are not official yet. */
    data class ProvisionalValues(val note: String) : Notice

    /** The child's own income and child benefit cover the need; no maintenance is owed. */
    data object NeedCovered : Notice

    /** No income was entered for either parent. */
    data object ParentIncomeMissing : Notice

    /** Neither parent earns more than the self-reserve. */
    data object NoParentAbleToPay : Notice

    /** Together the parents can cover the remaining need only partly (Mangelfall). */
    data class Shortfall(val amount: Euro) : Notice
}
