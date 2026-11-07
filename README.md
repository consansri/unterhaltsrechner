# Unterhaltsrechner

Berechnet den Kindesunterhalt, den Eltern einem **volljährigen, studierenden Kind mit eigenem Haushalt**
monatlich schulden – transparent, mit vollständigem Rechenweg und Rechtsgrundlagen.

Kotlin/Compose Multiplatform-App für Desktop (JVM) und Web (Wasm/JS).

## Berechnungsmethode

Alle Beträge werden auf den Monat umgerechnet und exakt in Cent berechnet (kaufmännische Rundung).

1. **Bedarf** = Regelbedarf Studierende laut Düsseldorfer Tabelle (2026: 990 €, davon bis 440 € Warmmiete)
   \+ Kranken-/Pflegeversicherung (falls nicht familienversichert) + Studiengebühren.
2. **Anrechnung** auf den Bedarf:
   - BAföG voll, inkl. Darlehensanteil (nicht bei Vorausleistung nach § 36 BAföG),
   - Ausbildungsvergütung (wahlweise voll, − 100 € Pauschale oder − 10 %/max. 100 €),
   - Nebenjob nach Billigkeit (§ 1577 Abs. 2 BGB entsprechend: nicht, bis Minijob-Grenze frei, eigener Freibetrag, voll),
   - sonstige Einkünfte voll,
   - **Kindergeld in voller Höhe** (§ 1612b Abs. 1 Nr. 2 BGB).
3. **Haftungsanteile** (§ 1606 Abs. 3 S. 1 BGB): Je Elternteil Einkommen − vorrangige Unterhaltspflichten
   − angemessener Selbstbehalt (2026: 1.750 €, erhöht bei Warmmiete über 650 €). Der Restbedarf wird im
   Verhältnis dieser einsetzbaren Einkommen verteilt; niemand zahlt mehr als sein einsetzbares Einkommen
   (sonst Mangelfall mit ausgewiesenem Fehlbetrag).
4. **Zahlbetrag**: Unterhaltsanteil, ggf. zuzüglich weiterzuleitendem Kindergeld.

Die Eltern-Einkommen werden aus dem **BAföG-Bescheid** übernommen. Dieser beruht auf dem vorletzten
Kalenderjahr und Pauschalen; das unterhaltsrechtlich bereinigte Einkommen kann abweichen.

Der Rechtsstand (Düsseldorfer Tabelle, Kindergeld, Minijob-Grenze) wird automatisch nach dem heutigen
Datum gewählt. Werte, die noch nicht amtlich sind, werden als „vorläufig“ gekennzeichnet.

**Keine Rechtsberatung.** Gerichte können im Einzelfall abweichen.

### Neues Jahr ergänzen

In `composeApp/src/commonMain/kotlin/de/consansri/unterhaltsrechner/types/Jahr.kt` einen Eintrag ans Ende
anfügen (bzw. den vorläufigen Eintrag auf `AMTLICH` setzen) und die Werte aus der neuen Düsseldorfer Tabelle
übernehmen. Quellen: [Düsseldorfer Tabelle (OLG Düsseldorf)](https://www.olg-duesseldorf.nrw.de/infos/Duesseldorfer_Tabelle/index.php),
Unterhaltsleitlinien der Oberlandesgerichte, Bundeskindergeldgesetz, Minijob-Zentrale.

## Aufbau

| Pfad | Inhalt |
|---|---|
| `types/` | `Euro` (Cent-genau, strenges Parsen), `ZBetrag` (Betrag + Zeitraum), `Jahr` (Rechtsstand je Jahr) |
| `berechnung/` | Reine Berechnung `berechne(eingaben, jahr)` inkl. Rechenweg – ohne Compose, vollständig getestet |
| `core/` | Wiederverwendbare UI-Bausteine (`BetragFeld`, `SegmentAuswahl`, `Karte`, …) |
| `ui/` | Kopfzeile, Eingabe- und Ergebnisbereich |

## Bauen, starten, testen

```shell
./gradlew :composeApp:run                          # Desktop
./gradlew :composeApp:wasmJsBrowserDevelopmentRun  # Web (Wasm)
./gradlew :composeApp:jsBrowserDevelopmentRun      # Web (JS)

./gradlew :composeApp:jvmTest
./gradlew :composeApp:jsBrowserTest :composeApp:wasmJsBrowserTest   # benötigt Chrome/Chromium (ggf. CHROME_BIN setzen)
```

## Lizenz

[MIT](LICENSE)
