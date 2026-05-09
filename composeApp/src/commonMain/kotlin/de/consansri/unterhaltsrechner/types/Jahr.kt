package de.consansri.unterhaltsrechner.types

enum class Jahr(
    val grundbedarf: Grundbedarf,
    val minijobGrenze: ZBetrag,
    val kindergeld: ZBetrag
) {
    J2023(
        Grundbedarf(
            ZBetrag(Euro(410.00)),
            ZBetrag(Euro(520.00))
        ),
        ZBetrag(Euro(520.00)),
        ZBetrag(Euro(250.00))
    ),
    J2024(
        Grundbedarf(
            ZBetrag(Euro(410.00)),
            ZBetrag(Euro(520.00))
        ),
        ZBetrag(Euro(538.00)),
        ZBetrag(Euro(250.00))
    ),
    J2025(
        Grundbedarf(
            ZBetrag(Euro(440.00)),
            ZBetrag(Euro(550.00))
        ),
        ZBetrag(Euro(556.00)),
        ZBetrag(Euro(255.00))
    ),
    J2026(
        Grundbedarf(
            ZBetrag(Euro(440.00)),
            ZBetrag(Euro(550.00))
        ),
        ZBetrag(Euro(603.00)),
        ZBetrag(Euro(259.00))
    ),
    J2027(
        Grundbedarf(
            ZBetrag(Euro(440.00)),
            ZBetrag(Euro(550.00))
        ),
        ZBetrag(Euro(633.00)),
        ZBetrag(Euro(259.00))
    )
    ;

    override fun toString(): String = name.removePrefix("J")
}