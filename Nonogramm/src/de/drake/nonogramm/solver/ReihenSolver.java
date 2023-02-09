package de.drake.nonogramm.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.drake.nonogramm.model.Feld;
import de.drake.nonogramm.model.Feldstatus;

/**
 * Kann Nonogrammzeilen (respektive -spalten) teilweise oder vollständig lösen.
 * Hierbei ist zu unterscheiden zwischen einer Lösung der Reihe (d.h. Erfüllung aller Nebenbedingungen
 * der Reihe) oder der "richtigen" Lösung der Reihe, bei der auch die restlichen Nebenbedingungen
 * des Nonogramms erfüllt werden.
 * Der ReihenSolver erlaubt es daher, Gemeinsamkeiten aller Lösungen der Reihe zu ermitteln und diese
 * im Nonogramm einzutragen.
 */
class ReihenSolver {
	
	/**
	 * Die Reihe, die aktuell bearbeitet werden soll
	 */
	private ArrayList<Feld> reihe;
	
	/**
	 * Eine mögliche Lösung der Reihe. Wird durch die Methode this.reiheLoesbarRekursion gefüllt.
	 * Um Seiteneffekte zu vermeiden, wird die Lösung nicht mit Nonogrammfeldern, sondern lediglich
	 * mit Feldstati gefüllt.
	 */
	private ArrayList<Feldstatus> loesung = null;
	
	/**
	 * Die Nebenbedingungen der aktuellen Reihe
	 */
	private ArrayList<Integer> bedingungen;
	
	/**
	 * Der Solver, der das Nonogramm lösen soll
	 */
	private Solver solver;
	
	/**
	 * Die Summe der schwarzen Felder aus den Bedingungen der Reihe.
	 */
	private int anzahlSchwarzerFelderBedingungen;
	
	/**
	 * Die Länge des längsten Blocks aus den Bedingungen.
	 */
	private int laengeLaengsterBlockBedingungen;
	
	/**
	 * Erzeugt einen neuen ReihenSolver.
	 * 
	 * @param solver
	 * 		Der Solver, der über die durchgeführten Änderungen informiert werden muss
	 */
	ReihenSolver(final Solver solver) {
		this.solver = solver;
	}
	
	/**
	 * Initialisiert den Reihensolver mit der übergebenen Reihe.
	 * 
	 * @param reihe
	 * 		Die zu bearbeitende Reihe
	 * @param bedingungen
	 * 		Die Nebenbedingungen der zu bearbeitenden Reihe
	 */
	void set(final ArrayList<Feld> reihe, final ArrayList<Integer> bedingungen) {
		this.reihe = reihe;
		this.bedingungen = bedingungen;
		this.loesung = null;
		this.anzahlSchwarzerFelderBedingungen = 0;
		this.laengeLaengsterBlockBedingungen = 0;
		for (int block : this.bedingungen) {
			if (block > this.laengeLaengsterBlockBedingungen)
				this.laengeLaengsterBlockBedingungen = block;
			this.anzahlSchwarzerFelderBedingungen += block;
		}
	}
	
	/**
	 * Bearbeitet die Reihe dahingehend, dass "sichere" Ergebnisse eingetragen werden.
	 */
	void bearbeiteReihe() {
		System.out.println("Erzeuge HashMaps...");
		HashMap<Feld, Integer> feld2blockLinksloesung = new HashMap<Feld, Integer>();
		HashMap<Feld, Integer> feld2blockRechtsloesung = new HashMap<Feld, Integer>();
		if (this.erzeugeLoesungsHashMaps(feld2blockLinksloesung, feld2blockRechtsloesung) == false) {
			return;
		}
		System.out.println("Lösungsverfahren A");
		this.fuelleUeberlappendeSchwarzeFelderAus(feld2blockLinksloesung, feld2blockRechtsloesung);
		this.schliesseFertigeBloeckeMitWeissenFeldernAb(feld2blockLinksloesung, feld2blockRechtsloesung);
		this.fuelleWeisseFelderAusDieInLinksUndRechtsloesungAnDerGleichenStelleStehen(
				feld2blockLinksloesung, feld2blockRechtsloesung);
		this.fuelleLueckenZwischenWeissenFeldern(feld2blockLinksloesung, feld2blockRechtsloesung);
		
		if (this.solver.getOptionRekursion()) {
			System.out.println("Lösungsverfahren B");
			this.loesePerRekursion();
		}
	}
	
	/**
	 * 	Zunächst werden eine Links-Lösung und eine Rechtslösung ermittelt (d.h. Lösungen,
	 *  in der alle schwarzen Felder so weit wie möglich links bzw. rechts liegen).
	 *  Auf Basis der beiden Lösungen werden die übergebenen HashMaps befüllt.
	 *  Die HashMaps geben dann an, im wievielten Bedingungsblock ein Feld in der jeweiligen Lösung
	 *  liegt.
	 *  Für schwarze Felder sind die HashMap immer gefüllt und geben die niedrigste (-> Rechtslösung)
	 *  oder höchste (->Linkslösung) Bedingung für das Feld an.
	 *  Für weiße Felder ist die HashMap nie gefüllt.
	 *  Für unknown-Felder ist die HashMap nur gefüllt, wenn die Felder in der Links- bzw.
	 *  Rechtslösung schwarz sind.
	 *  
	 *  @param feld2blockLinksloesung
	 *  		Die HashMap, in die die geforderte Zuordnung für die Linkslösung eingetragen wird
	 *  @param feld2blockRechtsloesung
	 *  		Die HashMap, in die die geforderte Zuordnung für die Rechtslösung eingetragen wird
	 *  @return
	 *  		false, wenn die Reihe unlösbar ist und daher keine Links- bzw. Rechtslösung existiert.
	 */
	private boolean erzeugeLoesungsHashMaps(final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		if (!this.reiheLoesbarRekursion(true)) {
			return false;
		}
		ArrayList<Feldstatus> linksloesung = this.loesung;
		
		//Versuche, eine Rechts-Lösung zu finden.
		Collections.reverse(this.reihe);
		Collections.reverse(this.bedingungen);
		this.reiheLoesbarRekursion(true);
		Collections.reverse(this.reihe);
		Collections.reverse(this.bedingungen);
		Collections.reverse(this.loesung);
		ArrayList<Feldstatus> rechtsloesung = this.loesung;

		int aktuellerBlockLinksloesung = -1;
		boolean aktuellerBlockLinksloesungBegonnen = false;
		int aktuellerBlockRechtsloesung = -1;
		boolean aktuellerBlockRechtsloesungBegonnen = false;
		Feld aktuellesFeld;
		for (int position = 0; position < this.reihe.size(); position++) {
			aktuellesFeld = this.reihe.get(position);
			if (linksloesung.get(position) == Feldstatus.black) {
				if (!aktuellerBlockLinksloesungBegonnen) {
					aktuellerBlockLinksloesung++;
					aktuellerBlockLinksloesungBegonnen = true;
				}
				feld2blockLinksloesung.put(aktuellesFeld, aktuellerBlockLinksloesung);
			} else {
				aktuellerBlockLinksloesungBegonnen = false;
			}
			if (rechtsloesung.get(position) == Feldstatus.black) {
				if (!aktuellerBlockRechtsloesungBegonnen) {
					aktuellerBlockRechtsloesung++;
					aktuellerBlockRechtsloesungBegonnen = true;
				}
				feld2blockRechtsloesung.put(aktuellesFeld, aktuellerBlockRechtsloesung);
			} else {
				aktuellerBlockRechtsloesungBegonnen = false;
			}
		}
		return true;
	}
	
	/**
	 * Wenn ein Feld sowohl in der Rechtslösung als auch in der Linkslösung zum gleichen Block gehört,
	 * muss es schwarz sein. Dementsprechende Ergebnisse werden in die Reihe eingetragen.
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung für die Linkslösung enthält
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung für die Rechtslösung enthält
	 */
	private void fuelleUeberlappendeSchwarzeFelderAus(
			final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		for (Feld feld : this.reihe) {
			if (!feld.hasStatus(Feldstatus.unknown))
				continue;
			if (feld2blockLinksloesung.containsKey(feld) && feld2blockRechtsloesung.containsKey(feld)
					&& feld2blockLinksloesung.get(feld) == feld2blockRechtsloesung.get(feld)) {
				feld.setStatus(Feldstatus.black);
				this.solver.vermeldeAenderung(feld);
			}
		}
	}
	
	/**
	 * Für jeden schwarzen Block der Reihe werden auf Basis der Links- bzw. Rechtslösung 
	 * die möglichen Bedingungen ermittelt. Besitzen alle möglichen Bedingungen eine Länge
	 * kleiner oder gleich der Länge des Blocks, so wird der Block durch weiße Felder an
	 * beiden Seiten "abgeschlossen".
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung für die Linkslösung enthält
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung für die Rechtslösung enthält
	 */
	private void schliesseFertigeBloeckeMitWeissenFeldernAb(
			final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		// Zunächst werden die Blöcke der aktuellen Reihe ermittelt:
		ArrayList<ArrayList<Integer>> bloecke = new ArrayList<ArrayList<Integer>> ();
		{
			ArrayList<Integer> aktuellerBlockPositions = new ArrayList<Integer> (this.reihe.size());
			for (int reihenindex = 0; reihenindex < this.reihe.size(); reihenindex++) {
				if (this.reihe.get(reihenindex).hasStatus(Feldstatus.black)) {
					aktuellerBlockPositions.add(reihenindex);
				} else if (!aktuellerBlockPositions.isEmpty()) {
					bloecke.add(aktuellerBlockPositions);
					aktuellerBlockPositions = new ArrayList<Integer> (this.reihe.size());
				}
			}
			if (aktuellerBlockPositions.size() != 0)
			bloecke.add(aktuellerBlockPositions);
		}

		// Für alle Blöcke werden die in Frage kommenden Bedingungen und hieraus die maximal mögliche
		// Länge ermittelt
		int ersterMoeglicherBlock, letzterMoeglicherBlock, maximallaengeBlock;
		for (ArrayList<Integer> aktuellerBlockPositions : bloecke) {
			ersterMoeglicherBlock = 
					feld2blockRechtsloesung.get(this.reihe.get(aktuellerBlockPositions.get(0)));
			letzterMoeglicherBlock = 
					feld2blockLinksloesung.get(this.reihe.get(aktuellerBlockPositions.get(0)));
			maximallaengeBlock = this.bedingungen.get(ersterMoeglicherBlock);
			for (int bedingungsindex = ersterMoeglicherBlock + 1; bedingungsindex <= letzterMoeglicherBlock;
					bedingungsindex++) {
				if (this.bedingungen.get(bedingungsindex) > maximallaengeBlock)
					maximallaengeBlock = this.bedingungen.get(bedingungsindex);
			}
			
			// Ist die Länge des aktuellen Block == maximallaengeBlock, so muss der Block
			// links und rechts mit weißen Feldern abschließen:
			if (aktuellerBlockPositions.size() == maximallaengeBlock) {
				if (aktuellerBlockPositions.get(0) > 0
						&& this.reihe.get(aktuellerBlockPositions.get(0) - 1).hasStatus(Feldstatus.unknown)) {
					this.reihe.get(aktuellerBlockPositions.get(0) - 1).setStatus(Feldstatus.white);
					this.solver.vermeldeAenderung(this.reihe.get(aktuellerBlockPositions.get(0) - 1));
				}
				if (aktuellerBlockPositions.get(aktuellerBlockPositions.size() - 1) + 1 < this.reihe.size()
						&& this.reihe.get(aktuellerBlockPositions.get(aktuellerBlockPositions.size() - 1) + 1).hasStatus(Feldstatus.unknown)) {
					this.reihe.get(aktuellerBlockPositions.get(aktuellerBlockPositions.size() - 1) + 1).setStatus(Feldstatus.white);
					this.solver.vermeldeAenderung(this.reihe.get(aktuellerBlockPositions.get(aktuellerBlockPositions.size() - 1) + 1));
				}
			}
		}
	}
	
	/**
	 *  Wenn ein Feld sowohl in der Linkslösung als auch in der Rechtslösung weiß ist und
	 *  in beiden Fällen zwischen den selben Bedingungsblöcken liegt, muss es weiß sein!
	 *  Dementsprechende Ergebnisse werden in die Reihe eingetragen.
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung für die Linkslösung enthält
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung für die Rechtslösung enthält
	 */
	private void fuelleWeisseFelderAusDieInLinksUndRechtsloesungAnDerGleichenStelleStehen(
			final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		int aktuellerBlockLinksloesung = -1;
		int aktuellerBlockRechtsloesung = -1;
		for (Feld feld : this.reihe) {
			if (feld2blockLinksloesung.containsKey(feld))
				aktuellerBlockLinksloesung = feld2blockLinksloesung.get(feld);
			if (feld2blockRechtsloesung.containsKey(feld))
				aktuellerBlockRechtsloesung = feld2blockRechtsloesung.get(feld);
			if (feld.hasStatus(Feldstatus.unknown) && !feld2blockLinksloesung.containsKey(feld)
					&& !feld2blockRechtsloesung.containsKey(feld)
					&& aktuellerBlockLinksloesung == aktuellerBlockRechtsloesung) {
				feld.setStatus(Feldstatus.white);
				this.solver.vermeldeAenderung(feld);
			}
		}
	}
	
	/**
	 *  Geht alle "Lücken" (d.h. Bereiche zwischen weißen Feldern) durch. Kommen für eine Lücke
	 *  nur Blöcke in Frage, die allesamt länger als die Lücke sind, wird die Lücke
	 *  mit weißen Feldern aufgefüllt.
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung für die Linkslösung enthält
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung für die Rechtslösung enthält
	 */
	private void fuelleLueckenZwischenWeissenFeldern(
			final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		int aktuellerBlockLinksloesung = -1;
		int aktuellerBlockRechtsloesung = -1;
		boolean schwarzesFeldGefunden = false;
		ArrayList<Feld> luecke = new ArrayList<Feld>(this.reihe.size());
		for (Feld feld : this.reihe) {
			if (feld2blockLinksloesung.containsKey(feld)) {
				aktuellerBlockLinksloesung = feld2blockLinksloesung.get(feld);
				schwarzesFeldGefunden = true;
			}
			if (feld2blockRechtsloesung.containsKey(feld)) {
				aktuellerBlockRechtsloesung = feld2blockRechtsloesung.get(feld);
				schwarzesFeldGefunden = true;
			}
			if (feld.hasStatus(Feldstatus.black)) {
				schwarzesFeldGefunden = true;
				continue;
			}
			if (feld.hasStatus(Feldstatus.unknown)) {
				luecke.add(feld);
				continue;
			}
			if (feld.hasStatus(Feldstatus.white)) {
				if (schwarzesFeldGefunden == false && !luecke.isEmpty()) {
					// Maximale Länge aller in Frage kommender Blöcke ermitteln
					int minimaleBlocklaenge = Integer.MAX_VALUE;
					for (int bedingungsindex = aktuellerBlockRechtsloesung + 1;
							bedingungsindex < aktuellerBlockLinksloesung + 1; bedingungsindex++) {
						minimaleBlocklaenge = Math.min(minimaleBlocklaenge, this.bedingungen.get(bedingungsindex));
					}
					if (minimaleBlocklaenge > luecke.size()) {
						for (Feld lueckenfeld : luecke) {
							lueckenfeld.setStatus(Feldstatus.white);
							this.solver.vermeldeAenderung(lueckenfeld);
						}
					}
				}
				luecke.clear();
				schwarzesFeldGefunden = false;
			}
		}
	}
	
	/**
	 *  Sukzessive Felder Schwarz oder Weiß setzen. Wenn dadurch Reihe unlösbar, muss das jeweils
	 *  andere der korrekte Wert sein!
	 *  Achtung: Durch diese Methode werden zwar garantiert alle "sicheren" Informationen gefunden
	 *  und eingetragen, dafür ist aber die Laufzeit des Verfahrens bei sehr großen Nonogrammen
	 *  katastrophal.
	 */
	private void loesePerRekursion() {
		for (Feld feld : this.reihe) {
			if (!feld.hasStatus(Feldstatus.unknown)) {
				continue;
			}
			
			//Für Seiteneffektfreiheit müssen alle Änderungen im Anschluss revidiert werden!
			feld.setStatus(Feldstatus.black);
			if (!this.reiheLoesbarRekursion(false)) {
				feld.setStatus(Feldstatus.white);
				this.solver.vermeldeAenderung(feld);
				continue;
			}
			feld.setStatus(Feldstatus.white);
			if (!this.reiheLoesbarRekursion(false)) {
				feld.setStatus(Feldstatus.black);
				solver.vermeldeAenderung(feld);
				continue;
			}
			feld.setStatus(Feldstatus.unknown); //Für Seiteneffektfreiheit
		}
	}
	
	/**
	 * Prüft, ob die Zeile lösbar sein könnte. Das Verfahren hängt vom Parameter
	 * "Rekursionsverfahren zulässig" ab. Kann die Methode de Lösbarkeit nicht genau bestimmen,
	 * wird im Zweifel true zurückgegeben.
	 */
	boolean reiheLoesbar() {
		if (this.solver.getOptionRekursion()) {
			return this.reiheLoesbarRekursion(false);
		}
		return this.reihePlausibel();
	}
	
	/**
	 * Versucht mit Hilfe des Rekursionsverfahrens, eine Lösung für die aktuelle Reihe zu finden, indem
	 * die Reihe von links aufgefüllt wird. Wird eine Lösung gefunden, so wird diese ggfs. 
	 * in this.loesung abgelegt.
	 * Die Funktion arbeitet zwar mit der übergebenen Reihe, ist aber seiteneffektfrei.
	 * 
	 * @param loesungAblegen
	 * 		gibt an, ob eine eventuell vorhandene Lösung in this.loesung abgelegt werden soll.
	 * 
	 * @return true, wenn eine zulässige Lösung existiert.
	 */
	private boolean reiheLoesbarRekursion(final boolean loesungAblegen) {
		if (!this.reihePlausibel())
			return false;
		
		// Ermittle erstes "unknown"-Feld
		Feld unknownFeld = null;
		for (Feld feld : this.reihe) {
			if (feld.hasStatus(Feldstatus.unknown)) {
				unknownFeld = feld;
				break;
			}
		}
		
		// Wenn Reihe komplett ausgefüllt und zulässig
		if (unknownFeld == null) {
			if (loesungAblegen) {
				this.loesung = new ArrayList<Feldstatus>(this.reihe.size());
				for (Feld feld : this.reihe) {
					this.loesung.add(feld.getStatus());
				}
			}
			return true;
		}
		
		unknownFeld.setStatus(Feldstatus.black);
		if (this.reiheLoesbarRekursion(loesungAblegen)) {
			unknownFeld.setStatus(Feldstatus.unknown);		// Für Seiteneffektfreiheit
			return true;
		}
		unknownFeld.setStatus(Feldstatus.white);
		if (this.reiheLoesbarRekursion(loesungAblegen)) {
			unknownFeld.setStatus(Feldstatus.unknown);		// Für Seiteneffektfreiheit
			return true;
		}
		unknownFeld.setStatus(Feldstatus.unknown);			// Für Seiteneffektfreiheit
		return false;
	}
	
	/**
	 * Prüft die Teillösung der aktuellen Reihe anhand von Heuristiken auf Plausibilität.
	 * 
	 * @return false, wenn die übergebene Reihe offensichtlich unlösbar ist.
	 */
	private boolean reihePlausibel() {
		
		if (this.getAnzahlSchwarzerFelderReihe() > this.anzahlSchwarzerFelderBedingungen)
			return false; // Wenn mehr Felder schwarz sind als erlaubt
		if (this.getLaengeLaengsterBlockReihe() > this.laengeLaengsterBlockBedingungen)
			return false; // Wenn bereits ein zu langer Block eingetragen ist
		
		// Wir analysieren nun den Teil links des ersten "unknown"-Feldes (ersteUnknownPosition)
		// und ermitteln dort die Blöcke (bloecke) und ihre Anzahl (anzahlDerBloecke).
		int ersteUnknownPosition = this.reihe.size();
		ArrayList<Integer> bloecke = new ArrayList<Integer>(this.reihe.size()/2 + 1);
		int groesseDesAktuellenBlocks = 0;
		
		for (int position = 0; position < this.reihe.size(); position++) {
			if (this.reihe.get(position).hasStatus(Feldstatus.unknown)) {
				ersteUnknownPosition = position;
				break;
			}
			if (this.reihe.get(position).hasStatus(Feldstatus.black)) {
				groesseDesAktuellenBlocks++;
				if (position == this.reihe.size()-1) {
					bloecke.add(groesseDesAktuellenBlocks);
					groesseDesAktuellenBlocks = 0;
				}
				continue;
			}
			// Nun ist zeile.get(position).hasStatus(Feld.white)
			if (groesseDesAktuellenBlocks != 0) {
				bloecke.add(groesseDesAktuellenBlocks);
				groesseDesAktuellenBlocks = 0;
			}
		}
		// groesseDesAktuellenBlocks speichert jetzt die Größe des letzten, angefangenen Blocks
		// vor dem ersten "unknown"-Feld.
		
		if (bloecke.size() > this.bedingungen.size())
			return false;	// Wenn mehr Blöcke gefunden wurden als erlaubt
		if (bloecke.size() == this.bedingungen.size() && groesseDesAktuellenBlocks != 0)
			return false;	// Wenn mehr Blöcke gefunden wurden als erlaubt
		if (bloecke.size() < this.bedingungen.size()
				&& groesseDesAktuellenBlocks > this.bedingungen.get(bloecke.size()))
			return false;	// Wenn der letzte angefangene Block größer ist als der, der als nächstes kommen muss
		for (int position = 0; position < bloecke.size(); position++) {
			if (bloecke.get(position) != this.bedingungen.get(position))
				return false;	// Wenn einer der gefundenen Blöcke die falsche Größe hat
		}
		if (bloecke.size() < this.bedingungen.size()) {
			int benoetigterPlatzFuerRestbloecke = 0;
			for (int position = bloecke.size(); position < this.bedingungen.size(); position++) {
				benoetigterPlatzFuerRestbloecke += this.bedingungen.get(position) + 1;
			}
			benoetigterPlatzFuerRestbloecke += -groesseDesAktuellenBlocks - 1;
			if (this.reihe.size() - ersteUnknownPosition <  benoetigterPlatzFuerRestbloecke)
				return false;	// Wenn für die noch fehlenden Blöcke der Platz nicht mehr reicht
		}
		return true;	// Wenn keine offensichtlichen Fehler gefunden wurden
	}
	
	/**
	 * Gibt die aktuelle Anzahl schwarzer Felder innerhalb der Reihe zurück.
	 */
	private int getAnzahlSchwarzerFelderReihe() {
		int result = 0;
		for (Feld feld : this.reihe) {
			if (feld.hasStatus(Feldstatus.black))
				result++;
		}
		return result;
	}
	
	/**
	 * Gibt die Länge des aktuell längsten schwarzen Blocks in der Reihe zurück.
	 */
	private int getLaengeLaengsterBlockReihe() {
		int result = 0;
		int aktuelleLaenge = 0;
		for (Feld feld : this.reihe) {
			if (feld.hasStatus(Feldstatus.black)) {
				aktuelleLaenge++;
				continue;
			}
			if (aktuelleLaenge > result) {
				result = aktuelleLaenge;
			}
			aktuelleLaenge = 0;
		}
		if (aktuelleLaenge > result) {
			result = aktuelleLaenge;
		}
		return result;
	}
}