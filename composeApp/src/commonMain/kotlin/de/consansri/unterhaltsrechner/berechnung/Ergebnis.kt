package de.consansri.unterhaltsrechner.berechnung

import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Jahr
import de.consansri.unterhaltsrechner.types.Prozent

/** Eine Zeile im Rechenweg. */
data class Schritt(
    val bezeichnung: String,
    val betrag: Euro,
    val art: Art,
    val grundlage: String? = null,
    val erlaeuterung: String? = null,
) {
    enum class Art { POSITION, ZUSCHLAG, ABZUG, SUMME }
}

/** Ein Abschnitt im Rechenweg, z. B. „1. Bedarf des Kindes“. */
data class Abschnitt(val titel: String, val schritte: List<Schritt>)

sealed interface Hinweis {
    /** Werte des Jahres sind noch nicht amtlich. */
    data class VorlaeufigeWerte(val text: String) : Hinweis

    /** Eigene Einkünfte und Kindergeld decken den Bedarf – es ist kein Unterhalt geschuldet. */
    data object BedarfGedeckt : Hinweis

    /** Für keinen Elternteil wurde ein Einkommen eingetragen. */
    data object EinkommenFehlt : Hinweis

    /** Kein Elternteil verdient mehr als den Selbstbehalt. */
    data object KeinElternteilLeistungsfaehig : Hinweis

    /** Die Eltern können den Restbedarf zusammen nur teilweise decken. */
    data class Mangelfall(val fehlbetrag: Euro) : Hinweis
}

data class ElternteilErgebnis(
    val elternteil: Elternteil,
    val einkommen: Euro,
    val abzuege: Euro,
    val selbstbehalt: Euro,
    /** Für den Unterhalt einsetzbares Einkommen: Einkommen − Abzüge − Selbstbehalt, mindestens 0. */
    val verfuegbar: Euro,
    /** Haftungsquote nach § 1606 Abs. 3 BGB; `null`, wenn kein Elternteil leistungsfähig ist. */
    val quote: Prozent?,
    /** Monatlicher Unterhaltsanteil. */
    val unterhalt: Euro,
    /** Kindergeld, das dieser Elternteil erhält und an das Kind weiterleiten muss. */
    val kindergeldWeiterleiten: Euro,
) {
    /** Gesamte monatliche Zahlung an das Kind. */
    val zahlbetrag: Euro get() = unterhalt + kindergeldWeiterleiten
}

data class Ergebnis(
    val jahr: Jahr,
    val bedarf: Euro,
    val einkommenKind: Euro,
    val kindergeld: Euro,
    val restbedarf: Euro,
    val vater: ElternteilErgebnis,
    val mutter: ElternteilErgebnis,
    val fehlbetrag: Euro,
    val hinweise: List<Hinweis>,
    val rechenweg: List<Abschnitt>,
) {
    val eltern: List<ElternteilErgebnis> get() = listOf(vater, mutter)
    val unterhaltGesamt: Euro get() = vater.unterhalt + mutter.unterhalt
}
