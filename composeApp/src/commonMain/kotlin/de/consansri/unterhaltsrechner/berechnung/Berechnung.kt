package de.consansri.unterhaltsrechner.berechnung

import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Jahr
import de.consansri.unterhaltsrechner.types.Prozent
import de.consansri.unterhaltsrechner.types.Rechtsstatus

/**
 * Unterhalt für ein volljähriges, studierendes Kind mit eigenem Haushalt.
 *
 * 1. Bedarf = Regelbedarf der Düsseldorfer Tabelle + Kranken-/Pflegeversicherung + Studiengebühren.
 * 2. Darauf werden die eigenen Einkünfte des Kindes (inkl. BAföG) angerechnet.
 * 3. Das Kindergeld wird in voller Höhe angerechnet (§ 1612b Abs. 1 Nr. 2 BGB).
 * 4. Den Restbedarf tragen die Eltern anteilig nach ihrem Einkommen abzüglich des angemessenen
 *    Selbstbehalts (§ 1606 Abs. 3 S. 1 BGB), jeweils höchstens bis zu diesem verfügbaren Betrag.
 *
 * Alle Beträge sind Monatsbeträge. Die Funktion ist rein und liefert neben dem Ergebnis den
 * vollständigen Rechenweg, den die Oberfläche unverändert anzeigt.
 */
fun berechne(eingaben: Eingaben, jahr: Jahr): Ergebnis {
    val rechenweg = mutableListOf<Abschnitt>()

    // 1. Bedarf
    val grundbedarf = (eingaben.grundbedarf?.proMonat ?: jahr.bedarfStudent)
    val kv = eingaben.krankenversicherung.proMonat
    val studiengebuehren = eingaben.studiengebuehren.proMonat
    val bedarf = grundbedarf + kv + studiengebuehren
    rechenweg += Abschnitt(
        "1. Bedarf des Kindes",
        buildList {
            add(
                Schritt(
                    "Regelbedarf Studierende (eigener Haushalt)", grundbedarf, Schritt.Art.POSITION,
                    grundlage = "Düsseldorfer Tabelle $jahr",
                    erlaeuterung = if (eingaben.grundbedarf == null) {
                        "Enthält bis ${jahr.bedarfWarmmiete} Warmmiete."
                    } else {
                        "Abweichend vom Tabellenwert ${jahr.bedarfStudent}."
                    },
                ),
            )
            if (kv != Euro.NULL) {
                add(
                    Schritt(
                        "Kranken- und Pflegeversicherung", kv, Schritt.Art.ZUSCHLAG,
                        grundlage = "Düsseldorfer Tabelle, Anm. zu KV/PV",
                        erlaeuterung = "Nicht im Regelbedarf enthalten.",
                    ),
                )
            }
            if (studiengebuehren != Euro.NULL) {
                add(
                    Schritt(
                        "Studiengebühren", studiengebuehren, Schritt.Art.ZUSCHLAG,
                        grundlage = "Düsseldorfer Tabelle, Anm. zu Studiengebühren",
                        erlaeuterung = "Nicht im Regelbedarf enthalten (Semesterbeiträge schon).",
                    ),
                )
            }
            add(Schritt("Gesamtbedarf", bedarf, Schritt.Art.SUMME))
        },
    )

    // 2. Einkünfte des Kindes und Kindergeld
    val bafoeg = if (eingaben.bafoegVorausleistung) Euro.NULL else eingaben.bafoeg.proMonat
    val verguetung = eingaben.ausbildungsverguetung.proMonat
    val verguetungAnrechenbar = eingaben.ausbildungsAufwand.anrechenbar(verguetung)
    val nebenjob = eingaben.nebenjob.proMonat
    val nebenjobAnrechenbar = when (eingaben.nebenjobRegel) {
        NebenjobRegel.NICHT_ANRECHNEN -> Euro.NULL
        NebenjobRegel.FREI_BIS_MINIJOB -> (nebenjob - jahr.minijobGrenze).atLeast(Euro.NULL)
        NebenjobRegel.EIGENER_FREIBETRAG -> (nebenjob - eingaben.nebenjobFreibetrag.proMonat).atLeast(Euro.NULL)
        NebenjobRegel.VOLL_ANRECHNEN -> nebenjob
    }
    val sonstige = eingaben.sonstigeEinkuenfte.proMonat
    val einkommenKind = bafoeg + verguetungAnrechenbar + nebenjobAnrechenbar + sonstige
    val kindergeld = eingaben.kindergeld?.proMonat ?: jahr.kindergeld
    val restbedarf = (bedarf - einkommenKind - kindergeld).atLeast(Euro.NULL)

    rechenweg += Abschnitt(
        "2. Anrechnung auf den Bedarf",
        buildList {
            add(Schritt("Gesamtbedarf", bedarf, Schritt.Art.POSITION))
            if (eingaben.bafoeg.betrag != Euro.NULL) {
                add(
                    if (eingaben.bafoegVorausleistung) {
                        Schritt(
                            "BAföG (Vorausleistung)", Euro.NULL, Schritt.Art.ABZUG,
                            grundlage = "§§ 36, 37 BAföG",
                            erlaeuterung = "Vorausleistungen mindern den Unterhalt nicht – der Anspruch geht auf das Land über.",
                        )
                    } else {
                        Schritt(
                            "BAföG", bafoeg, Schritt.Art.ABZUG,
                            grundlage = "Leitlinien Nr. 2.4, 13.2",
                            erlaeuterung = "Voll anzurechnen, auch der Darlehensanteil.",
                        )
                    },
                )
            }
            if (verguetung != Euro.NULL) {
                add(
                    Schritt(
                        "Ausbildungsvergütung (${eingaben.ausbildungsAufwand.bezeichnung})", verguetungAnrechenbar,
                        Schritt.Art.ABZUG,
                        grundlage = "Leitlinien Nr. 10.2.3, 13.2",
                        erlaeuterung = "Vergütung $verguetung. ${eingaben.ausbildungsAufwand.erlaeuterung}",
                    ),
                )
            }
            if (nebenjob != Euro.NULL) {
                add(
                    Schritt(
                        "Nebenjob (${eingaben.nebenjobRegel.bezeichnung})", nebenjobAnrechenbar, Schritt.Art.ABZUG,
                        grundlage = "§ 1577 Abs. 2 BGB entspr., Leitlinien Nr. 13.2",
                        erlaeuterung = "Verdienst $nebenjob. ${eingaben.nebenjobRegel.erlaeuterung}",
                    ),
                )
            }
            if (sonstige != Euro.NULL) {
                add(Schritt("Sonstige Einkünfte", sonstige, Schritt.Art.ABZUG, grundlage = "Leitlinien Nr. 13.2"))
            }
            add(
                Schritt(
                    "Kindergeld (voll)", kindergeld, Schritt.Art.ABZUG,
                    grundlage = "§ 1612b Abs. 1 Nr. 2 BGB",
                    erlaeuterung = "Bei volljährigen Kindern wird das Kindergeld in voller Höhe auf den Bedarf angerechnet.",
                ),
            )
            add(
                Schritt(
                    "Restbedarf (von den Eltern zu tragen)", restbedarf, Schritt.Art.SUMME,
                    erlaeuterung = if (bedarf - einkommenKind - kindergeld < Euro.NULL) {
                        "Die Anrechnung übersteigt den Bedarf – der Restbedarf ist 0."
                    } else {
                        null
                    },
                ),
            )
        },
    )

    // 3. Haftungsanteile der Eltern
    val vorlaeufig = Elternteil.entries.map { e ->
        val eingabe = eingaben.elternteil(e)
        val einkommen = eingabe.einkommen.proMonat
        val abzuege = eingabe.abzuege.proMonat
        val mietMehrbedarf = (eingabe.warmmiete.proMonat - jahr.selbstbehaltWarmmiete).atLeast(Euro.NULL)
        val selbstbehalt = jahr.selbstbehalt + mietMehrbedarf
        val verfuegbar = (einkommen - abzuege - selbstbehalt).atLeast(Euro.NULL)
        Vorlaeufig(e, einkommen, abzuege, mietMehrbedarf, selbstbehalt, verfuegbar)
    }
    val summeVerfuegbar = vorlaeufig.fold(Euro.NULL) { acc, v -> acc + v.verfuegbar }
    val anteile = verteile(restbedarf, vorlaeufig.map { it.verfuegbar })
    val gedeckt = anteile.fold(Euro.NULL, Euro::plus)
    val fehlbetrag = restbedarf - gedeckt

    val elternErgebnisse = vorlaeufig.zip(anteile) { v, anteil ->
        ElternteilErgebnis(
            elternteil = v.elternteil,
            einkommen = v.einkommen,
            abzuege = v.abzuege,
            selbstbehalt = v.selbstbehalt,
            verfuegbar = v.verfuegbar,
            quote = Prozent.anteil(v.verfuegbar, summeVerfuegbar),
            unterhalt = anteil,
            kindergeldWeiterleiten = if (eingaben.kindergeldEmpfaenger.elternteil == v.elternteil) kindergeld else Euro.NULL,
        )
    }

    vorlaeufig.zip(elternErgebnisse) { v, ergebnis ->
        rechenweg += Abschnitt(
            "3. ${v.elternteil}: einsetzbares Einkommen",
            buildList {
                add(
                    Schritt(
                        "Einkommen", v.einkommen, Schritt.Art.POSITION,
                        grundlage = "BAföG-Bescheid (§ 21 BAföG)",
                        erlaeuterung = "Monatsbetrag des im Bescheid ausgewiesenen Einkommens.",
                    ),
                )
                if (v.abzuege != Euro.NULL) {
                    add(
                        Schritt(
                            "Vorrangige Unterhaltspflichten / Abzüge", v.abzuege, Schritt.Art.ABZUG,
                            grundlage = "§ 1609 BGB, Leitlinien Nr. 13.3 Abs. 2",
                        ),
                    )
                }
                add(
                    Schritt(
                        "Angemessener Selbstbehalt", jahr.selbstbehalt, Schritt.Art.ABZUG,
                        grundlage = "§ 1603 Abs. 1 BGB, Düsseldorfer Tabelle $jahr",
                        erlaeuterung = "Enthält ${jahr.selbstbehaltWarmmiete} Warmmiete.",
                    ),
                )
                if (v.mietMehrbedarf != Euro.NULL) {
                    add(
                        Schritt(
                            "Erhöhung wegen höherer Warmmiete", v.mietMehrbedarf, Schritt.Art.ABZUG,
                            grundlage = "Düsseldorfer Tabelle $jahr",
                            erlaeuterung = "Der Selbstbehalt soll bei höheren, nicht unangemessenen Wohnkosten erhöht werden.",
                        ),
                    )
                }
                add(Schritt("Einsetzbares Einkommen", v.verfuegbar, Schritt.Art.SUMME))
            },
        )
    }

    rechenweg += Abschnitt(
        "4. Aufteilung auf die Eltern",
        buildList {
            add(Schritt("Restbedarf", restbedarf, Schritt.Art.POSITION))
            add(
                Schritt(
                    "Summe einsetzbarer Einkommen", summeVerfuegbar, Schritt.Art.POSITION,
                    grundlage = "§ 1606 Abs. 3 S. 1 BGB",
                    erlaeuterung = "Die Eltern haften anteilig im Verhältnis ihrer einsetzbaren Einkommen.",
                ),
            )
            elternErgebnisse.forEach { e ->
                add(
                    Schritt(
                        "Anteil ${e.elternteil}", e.unterhalt, Schritt.Art.SUMME,
                        erlaeuterung = when {
                            e.quote == null -> "Kein Elternteil ist leistungsfähig."
                            summeVerfuegbar < restbedarf ->
                                "Mangelfall: ${e.elternteil} zahlt das gesamte einsetzbare Einkommen ${e.verfuegbar}."
                            else -> "$restbedarf × ${e.verfuegbar} ÷ $summeVerfuegbar (Quote ${e.quote})"
                        },
                    ),
                )
            }
            if (fehlbetrag != Euro.NULL) {
                add(
                    Schritt(
                        "Ungedeckter Fehlbetrag", fehlbetrag, Schritt.Art.SUMME,
                        erlaeuterung = "Die Eltern sind über ihren Selbstbehalt hinaus nicht leistungsfähig.",
                    ),
                )
            }
        },
    )

    val hinweise = buildList {
        if (jahr.status == Rechtsstatus.VORLAEUFIG) add(Hinweis.VorlaeufigeWerte(jahr.hinweis.orEmpty()))
        when {
            restbedarf == Euro.NULL -> add(Hinweis.BedarfGedeckt)
            vorlaeufig.all { it.einkommen == Euro.NULL } -> add(Hinweis.EinkommenFehlt)
            summeVerfuegbar == Euro.NULL -> add(Hinweis.KeinElternteilLeistungsfaehig)
            fehlbetrag > Euro.NULL -> add(Hinweis.Mangelfall(fehlbetrag))
        }
    }

    return Ergebnis(
        jahr = jahr,
        bedarf = bedarf,
        einkommenKind = einkommenKind,
        kindergeld = kindergeld,
        restbedarf = restbedarf,
        vater = elternErgebnisse.first { it.elternteil == Elternteil.VATER },
        mutter = elternErgebnisse.first { it.elternteil == Elternteil.MUTTER },
        fehlbetrag = fehlbetrag,
        hinweise = hinweise,
        rechenweg = rechenweg,
    )
}

private class Vorlaeufig(
    val elternteil: Elternteil,
    val einkommen: Euro,
    val abzuege: Euro,
    val mietMehrbedarf: Euro,
    val selbstbehalt: Euro,
    val verfuegbar: Euro,
)

/**
 * Verteilt [betrag] im Verhältnis von [gewichte] (§ 1606 Abs. 3 BGB).
 *
 * - Die Anteile ergeben in Summe exakt [betrag] (kumulative Rundung, kein Cent geht verloren).
 * - Kein Anteil übersteigt sein Gewicht (Leistungsfähigkeit). Reichen die Gewichte insgesamt nicht,
 *   trägt jeder genau sein Gewicht (Mangelfall).
 */
internal fun verteile(betrag: Euro, gewichte: List<Euro>): List<Euro> {
    val summe = gewichte.fold(Euro.NULL, Euro::plus)
    if (summe == Euro.NULL) return gewichte.map { Euro.NULL }
    if (summe <= betrag) return gewichte

    var kumuliert = Euro.NULL
    var bisherVerteilt = Euro.NULL
    return gewichte.map { gewicht ->
        kumuliert += gewicht
        val verteiltBisHier = betrag.anteil(kumuliert, summe)
        (verteiltBisHier - bisherVerteilt).also { bisherVerteilt = verteiltBisHier }
    }
}
