package de.consansri.unterhaltsrechner.berechnung

import de.consansri.unterhaltsrechner.types.Euro
import de.consansri.unterhaltsrechner.types.Jahr
import de.consansri.unterhaltsrechner.types.ZBetrag
import de.consansri.unterhaltsrechner.types.Zeitraum
import de.consansri.unterhaltsrechner.types.euro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BerechnungTest {

    private fun monatlich(euro: Int) = ZBetrag.monatlich(euro.euro)

    private fun eltern(vater: Int, mutter: Int) = Eingaben(
        vater = ElternteilEingabe(einkommen = monatlich(vater)),
        mutter = ElternteilEingabe(einkommen = monatlich(mutter)),
    )

    /** Bei jeder Berechnung: Unterhalt + Fehlbetrag = Restbedarf, Anteile ≤ einsetzbares Einkommen. */
    private fun pruefeInvarianten(e: Ergebnis) {
        assertEquals(e.restbedarf, e.unterhaltGesamt + e.fehlbetrag, "Unterhalt + Fehlbetrag = Restbedarf")
        e.eltern.forEach { assertTrue(it.unterhalt <= it.verfuegbar, "${it.elternteil}: Anteil ≤ einsetzbares Einkommen") }
    }

    @Test
    fun kindergeldWirdVollAngerechnetUndNachSelbstbehaltGeteilt() {
        val e = berechne(eltern(vater = 3_000, mutter = 2_000), Jahr.J2026)

        assertEquals(990.euro, e.bedarf)
        assertEquals(259.euro, e.kindergeld)
        assertEquals(731.euro, e.restbedarf)
        assertEquals(1_250.euro, e.vater.verfuegbar)
        assertEquals(250.euro, e.mutter.verfuegbar)
        assertEquals(Euro(60_917), e.vater.unterhalt)
        assertEquals(Euro(12_183), e.mutter.unterhalt)
        assertEquals("83,33 %", e.vater.quote.toString())
        assertEquals(Euro.NULL, e.fehlbetrag)
        assertTrue(e.hinweise.isEmpty())
        pruefeInvarianten(e)
    }

    @Test
    fun jahreseinkommenAusBescheidWirdAufMonatUmgerechnet() {
        val e = berechne(
            Eingaben(
                vater = ElternteilEingabe(einkommen = ZBetrag(36_000.euro, Zeitraum.JAHR)),
                mutter = ElternteilEingabe(einkommen = ZBetrag(24_000.euro, Zeitraum.JAHR)),
            ),
            Jahr.J2026,
        )
        assertEquals(Euro(60_917), e.vater.unterhalt)
        assertEquals(Euro(12_183), e.mutter.unterhalt)
    }

    @Test
    fun mangelfall() {
        val e = berechne(eltern(vater = 2_000, mutter = 1_500), Jahr.J2026)

        assertEquals(250.euro, e.vater.unterhalt)
        assertEquals(Euro.NULL, e.mutter.unterhalt)
        assertEquals(481.euro, e.fehlbetrag)
        assertEquals(listOf<Hinweis>(Hinweis.Mangelfall(481.euro)), e.hinweise)
        pruefeInvarianten(e)
    }

    @Test
    fun keinElternteilLeistungsfaehig() {
        val e = berechne(eltern(vater = 1_700, mutter = 1_000), Jahr.J2026)

        assertEquals(Euro.NULL, e.unterhaltGesamt)
        assertEquals(731.euro, e.fehlbetrag)
        assertEquals(null, e.vater.quote)
        assertEquals(listOf<Hinweis>(Hinweis.KeinElternteilLeistungsfaehig), e.hinweise)
        pruefeInvarianten(e)
    }

    @Test
    fun bafoegUndKrankenversicherung() {
        val e = berechne(
            eltern(vater = 4_000, mutter = 4_000).copy(bafoeg = monatlich(400), krankenversicherung = monatlich(120)),
            Jahr.J2026,
        )
        assertEquals(1_110.euro, e.bedarf)
        assertEquals(400.euro, e.einkommenKind)
        assertEquals(451.euro, e.restbedarf)
        assertEquals(Euro(22_550), e.vater.unterhalt)
        assertEquals(Euro(22_550), e.mutter.unterhalt)
        pruefeInvarianten(e)
    }

    @Test
    fun einkommenDesKindesDecktAuchKrankenversicherungUndStudiengebuehren() {
        val e = berechne(
            eltern(vater = 4_000, mutter = 4_000).copy(
                krankenversicherung = monatlich(120),
                studiengebuehren = ZBetrag(1_500.euro, Zeitraum.SEMESTER),
                sonstigeEinkuenfte = monatlich(1_000),
            ),
            Jahr.J2026,
        )
        assertEquals(1_360.euro, e.bedarf)
        assertEquals(101.euro, e.restbedarf, "1.360 − 1.000 − 259")
    }

    @Test
    fun bafoegAlsVorausleistungMindertNicht() {
        val e = berechne(
            eltern(vater = 3_000, mutter = 2_000).copy(bafoeg = monatlich(400), bafoegVorausleistung = true),
            Jahr.J2026,
        )
        assertEquals(Euro.NULL, e.einkommenKind)
        assertEquals(731.euro, e.restbedarf)
    }

    @Test
    fun nebenjobRegeln() {
        fun restbedarf(regel: NebenjobRegel, freibetrag: Int = 0) = berechne(
            eltern(vater = 4_000, mutter = 4_000).copy(
                nebenjob = monatlich(700),
                nebenjobRegel = regel,
                nebenjobFreibetrag = monatlich(freibetrag),
            ),
            Jahr.J2026,
        ).restbedarf

        assertEquals(731.euro, restbedarf(NebenjobRegel.NICHT_ANRECHNEN))
        assertEquals(634.euro, restbedarf(NebenjobRegel.FREI_BIS_MINIJOB), "700 − 603 = 97 angerechnet")
        assertEquals(331.euro, restbedarf(NebenjobRegel.EIGENER_FREIBETRAG, freibetrag = 300))
        assertEquals(31.euro, restbedarf(NebenjobRegel.VOLL_ANRECHNEN))
    }

    @Test
    fun ausbildungsverguetung() {
        assertEquals(800.euro, AusbildungsAufwand.IM_BEDARF_ENTHALTEN.anrechenbar(800.euro))
        assertEquals(700.euro, AusbildungsAufwand.PAUSCHALE_100.anrechenbar(800.euro))
        assertEquals(Euro.NULL, AusbildungsAufwand.PAUSCHALE_100.anrechenbar(80.euro))
        assertEquals(720.euro, AusbildungsAufwand.ZEHN_PROZENT.anrechenbar(800.euro))
        assertEquals(1_400.euro, AusbildungsAufwand.ZEHN_PROZENT.anrechenbar(1_500.euro), "höchstens 100 €")
    }

    @Test
    fun kindergeldEmpfaengerLeitetZusaetzlichWeiter() {
        val e = berechne(
            eltern(vater = 3_000, mutter = 2_000).copy(kindergeldEmpfaenger = KindergeldEmpfaenger.MUTTER),
            Jahr.J2026,
        )
        assertEquals(Euro.NULL, e.vater.kindergeldWeiterleiten)
        assertEquals(259.euro, e.mutter.kindergeldWeiterleiten)
        assertEquals(Euro(12_183) + 259.euro, e.mutter.zahlbetrag)
        assertEquals(Euro(60_917), e.vater.zahlbetrag)
    }

    @Test
    fun bedarfGedeckt() {
        val e = berechne(eltern(vater = 4_000, mutter = 4_000).copy(bafoeg = monatlich(855)), Jahr.J2026)
        assertEquals(Euro.NULL, e.restbedarf)
        assertEquals(Euro.NULL, e.unterhaltGesamt)
        assertEquals(listOf<Hinweis>(Hinweis.BedarfGedeckt), e.hinweise)
    }

    @Test
    fun abzuegeUndWarmmieteErhoehenSelbstbehalt() {
        val e = berechne(
            Eingaben(
                vater = ElternteilEingabe(
                    einkommen = monatlich(3_000),
                    abzuege = monatlich(500),
                    warmmiete = monatlich(800),
                ),
                mutter = ElternteilEingabe(einkommen = monatlich(3_000)),
            ),
            Jahr.J2026,
        )
        assertEquals(1_900.euro, e.vater.selbstbehalt, "1.750 + (800 − 650)")
        assertEquals(600.euro, e.vater.verfuegbar, "3.000 − 500 − 1.900")
        assertEquals(1_250.euro, e.mutter.verfuegbar)
        pruefeInvarianten(e)
    }

    @Test
    fun tabellenwerteFolgenDemJahrEigeneWerteBleiben() {
        val standard = eltern(vater = 4_000, mutter = 4_000)
        assertEquals(930.euro - 250.euro, berechne(standard, Jahr.J2024).restbedarf)
        assertEquals(990.euro - 255.euro, berechne(standard, Jahr.J2025).restbedarf)

        val eigenerBedarf = standard.copy(grundbedarf = monatlich(1_100))
        assertEquals(1_100.euro - 250.euro, berechne(eigenerBedarf, Jahr.J2024).restbedarf)
        assertEquals(1_100.euro - 259.euro, berechne(eigenerBedarf, Jahr.J2026).restbedarf)
    }

    @Test
    fun ohneElterneinkommenWirdZurEingabeAufgefordert() {
        val e = berechne(Eingaben(), Jahr.J2026)
        assertEquals(listOf<Hinweis>(Hinweis.EinkommenFehlt), e.hinweise)
        assertEquals(Euro.NULL, e.unterhaltGesamt)
    }

    @Test
    fun vorlaeufigesJahrErzeugtHinweis() {
        val e = berechne(eltern(vater = 3_000, mutter = 2_000), Jahr.J2027)
        assertTrue(e.hinweise.any { it is Hinweis.VorlaeufigeWerte })
    }

    @Test
    fun verteilungVerliertKeinenCent() {
        // 1 € auf drei gleiche Gewichte: 33 + 34 + 33 Cent (kumulative Rundung)
        val anteile = verteile(1.euro, listOf(10.euro, 10.euro, 10.euro))
        assertEquals(1.euro, anteile.fold(Euro.NULL, Euro::plus))
        assertTrue(anteile.all { it == Euro(33) || it == Euro(34) })

        // Krumme Gewichte über viele Beträge: Summe stimmt immer, kein Anteil über seinem Gewicht
        val gewichte = listOf(Euro(12_345), Euro(67_891))
        for (cent in 0L..80_236L step 997) {
            val betrag = Euro(cent)
            val verteilt = verteile(betrag, gewichte)
            assertEquals(betrag, verteilt.fold(Euro.NULL, Euro::plus), "Summe bei $betrag")
            verteilt.zip(gewichte).forEach { (a, g) -> assertTrue(a <= g, "$a ≤ $g bei $betrag") }
        }
    }

    @Test
    fun rechenwegEndetMitDenAnteilen() {
        val e = berechne(eltern(vater = 3_000, mutter = 2_000), Jahr.J2026)
        val aufteilung = e.rechenweg.last()
        val anteile = aufteilung.schritte.filter { it.bezeichnung.startsWith("Anteil") }.map { it.betrag }
        assertEquals(listOf(e.vater.unterhalt, e.mutter.unterhalt), anteile)
        assertEquals(e.restbedarf, e.rechenweg[1].schritte.last().betrag)
    }
}
