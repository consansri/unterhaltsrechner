package de.consansri.unterhaltsrechner.berechnung

import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.ZBetrag
import de.consansri.unterhaltsrechner.types.Zeitraum
import de.consansri.unterhaltsrechner.types.euro

enum class Elternteil(val bezeichnung: String) {
    VATER("Vater"),
    MUTTER("Mutter");

    override fun toString() = bezeichnung
}

/** Wer das Kindergeld ausgezahlt bekommt. Ein Elternteil muss es dann an das Kind weiterleiten. */
enum class KindergeldEmpfaenger(val bezeichnung: String, val elternteil: Elternteil?) {
    KIND("Kind", null),
    VATER("Vater", Elternteil.VATER),
    MUTTER("Mutter", Elternteil.MUTTER);

    override fun toString() = bezeichnung
}

/** Wie eine Ausbildungsvergütung des Kindes um ausbildungsbedingten Aufwand gekürzt wird. */
enum class AusbildungsAufwand(val bezeichnung: String, val erlaeuterung: String) {
    IM_BEDARF_ENTHALTEN(
        "voll anrechnen",
        "Bei eigenem Haushalt ist der ausbildungsbedingte Aufwand bereits im Bedarf enthalten " +
            "(z. B. Leitlinien NRW Nr. 13.1 Abs. 2, 13.2).",
    ),
    PAUSCHALE_100(
        "− 100 € Pauschale",
        "Pauschaler ausbildungsbedingter Mehrbedarf von 100 € (Leitlinien vieler OLG, v. a. für Auszubildende " +
            "im Haushalt der Eltern).",
    ),
    ZEHN_PROZENT(
        "− 10 %, max. 100 €",
        "Kürzung um 10 %, höchstens 100 € (Unterhaltsleitlinien OLG Naumburg).",
    );

    override fun toString() = bezeichnung

    fun anrechenbar(verguetung: Euro): Euro = when (this) {
        IM_BEDARF_ENTHALTEN -> verguetung
        PAUSCHALE_100 -> (verguetung - 100.euro).atLeast(Euro.NULL)
        ZEHN_PROZENT -> verguetung - verguetung.geteiltDurch(10).atMost(100.euro)
    }
}

/**
 * Anrechnung von Einkünften aus einem Neben- oder Ferienjob während des Studiums.
 *
 * Studierende trifft keine Erwerbsobliegenheit; solche Einkünfte sind nur nach Billigkeit
 * anzurechnen (§ 1577 Abs. 2 BGB entsprechend, Leitlinien Nr. 13.2).
 */
enum class NebenjobRegel(val bezeichnung: String, val erlaeuterung: String) {
    NICHT_ANRECHNEN(
        "nicht anrechnen",
        "Regelfall bei geringfügiger Tätigkeit (z. B. Ferienjob, Minijob): Einkünfte aus einer unterhaltsrechtlich " +
            "nicht gebotenen Tätigkeit bleiben in der Regel unberücksichtigt.",
    ),
    FREI_BIS_MINIJOB(
        "frei bis Minijob-Grenze",
        "Billigkeitsabwägung: Einkünfte bis zur Minijob-Grenze bleiben frei, der Rest wird angerechnet.",
    ),
    EIGENER_FREIBETRAG(
        "eigener Freibetrag",
        "Billigkeitsabwägung (§ 1577 Abs. 2 BGB entsprechend): Einkünfte bis zum gewählten Freibetrag bleiben frei.",
    ),
    VOLL_ANRECHNEN(
        "voll anrechnen",
        "Volle Anrechnung, z. B. bei einer Tätigkeit, die neben dem Studium zumutbar und üblich ist.",
    );

    override fun toString() = bezeichnung
}

data class ElternteilEingabe(
    /** Einkommen laut BAföG-Bescheid (Einkommen nach § 21 BAföG, also nach Steuern und Sozialpauschale). */
    val einkommen: ZBetrag = ZBetrag(Euro.NULL, Zeitraum.JAHR),
    /** Vorrangige Unterhaltspflichten (z. B. für minderjährige Geschwister) und sonstige Abzüge. */
    val abzuege: ZBetrag = ZBetrag.NULL,
    /** Tatsächliche Warmmiete, falls sie die im Selbstbehalt enthaltene Warmmiete übersteigt. */
    val warmmiete: ZBetrag = ZBetrag.NULL,
)

/**
 * Alle Eingaben der Berechnung. Felder mit `null` übernehmen den Tabellenwert des gewählten Jahres –
 * so passen sie sich beim Jahreswechsel an, eigene Werte bleiben dagegen erhalten.
 */
data class Eingaben(
    val grundbedarf: ZBetrag? = null,
    val krankenversicherung: ZBetrag = ZBetrag.NULL,
    val studiengebuehren: ZBetrag = ZBetrag(Euro.NULL, Zeitraum.SEMESTER),

    val bafoeg: ZBetrag = ZBetrag.NULL,
    val bafoegVorausleistung: Boolean = false,
    val ausbildungsverguetung: ZBetrag = ZBetrag.NULL,
    val ausbildungsAufwand: AusbildungsAufwand = AusbildungsAufwand.IM_BEDARF_ENTHALTEN,
    val nebenjob: ZBetrag = ZBetrag.NULL,
    val nebenjobRegel: NebenjobRegel = NebenjobRegel.NICHT_ANRECHNEN,
    val nebenjobFreibetrag: ZBetrag = ZBetrag.NULL,
    val sonstigeEinkuenfte: ZBetrag = ZBetrag.NULL,

    val kindergeld: ZBetrag? = null,
    val kindergeldEmpfaenger: KindergeldEmpfaenger = KindergeldEmpfaenger.KIND,

    val vater: ElternteilEingabe = ElternteilEingabe(),
    val mutter: ElternteilEingabe = ElternteilEingabe(),
) {
    fun elternteil(e: Elternteil): ElternteilEingabe = when (e) {
        Elternteil.VATER -> vater
        Elternteil.MUTTER -> mutter
    }

    fun mitElternteil(e: Elternteil, eingabe: ElternteilEingabe): Eingaben = when (e) {
        Elternteil.VATER -> copy(vater = eingabe)
        Elternteil.MUTTER -> copy(mutter = eingabe)
    }
}
