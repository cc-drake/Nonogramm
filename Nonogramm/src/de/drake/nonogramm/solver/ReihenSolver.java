package de.drake.nonogramm.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.drake.nonogramm.model.Feld;
import de.drake.nonogramm.model.Feldstatus;

/**
 * Kann Nonogrammzeilen (respektive -spalten) teilweise oder vollst�ndig l�sen.
 * Hierbei ist zu unterscheiden zwischen einer L�sung der Reihe (d.h. Erf�llung aller Nebenbedingungen
 * der Reihe) oder der "richtigen" L�sung der Reihe, bei der auch die restlichen Nebenbedingungen
 * des Nonogramms erf�llt werden.
 * Der ReihenSolver erlaubt es daher, Gemeinsamkeiten aller L�sungen der Reihe zu ermitteln und diese
 * im Nonogramm einzutragen.
 */
class ReihenSolver {
	
	/**
	 * Die Reihe, die aktuell bearbeitet werden soll
	 */
	private ArrayList<Feld> reihe;
	
	/**
	 * Eine m�gliche L�sung der Reihe. Wird durch die Methode this.reiheLoesbarRekursion gef�llt.
	 * Um Seiteneffekte zu vermeiden, wird die L�sung nicht mit Nonogrammfeldern, sondern lediglich
	 * mit Feldstati gef�llt.
	 */
	private ArrayList<Feldstatus> loesung = null;
	
	/**
	 * Die Nebenbedingungen der aktuellen Reihe
	 */
	private ArrayList<Integer> bedingungen;
	
	/**
	 * Der Solver, der das Nonogramm l�sen soll
	 */
	private Solver solver;
	
	/**
	 * Die Summe der schwarzen Felder aus den Bedingungen der Reihe.
	 */
	private int anzahlSchwarzerFelderBedingungen;
	
	/**
	 * Die L�nge des l�ngsten Blocks aus den Bedingungen.
	 */
	private int laengeLaengsterBlockBedingungen;
	
	/**
	 * Erzeugt einen neuen ReihenSolver.
	 * 
	 * @param solver
	 * 		Der Solver, der �ber die durchgef�hrten �nderungen informiert werden muss
	 */
	ReihenSolver(final Solver solver) {
		this.solver = solver;
	}
	
	/**
	 * Initialisiert den Reihensolver mit der �bergebenen Reihe.
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
		System.out.println("L�sungsverfahren A");
		this.fuelleUeberlappendeSchwarzeFelderAus(feld2blockLinksloesung, feld2blockRechtsloesung);
		this.schliesseFertigeBloeckeMitWeissenFeldernAb(feld2blockLinksloesung, feld2blockRechtsloesung);
		this.fuelleWeisseFelderAusDieInLinksUndRechtsloesungAnDerGleichenStelleStehen(
				feld2blockLinksloesung, feld2blockRechtsloesung);
		this.fuelleLueckenZwischenWeissenFeldern(feld2blockLinksloesung, feld2blockRechtsloesung);
		
		if (this.solver.getOptionRekursion()) {
			System.out.println("L�sungsverfahren B");
			this.loesePerRekursion();
		}
	}
	
	/**
	 * 	Zun�chst werden eine Links-L�sung und eine Rechtsl�sung ermittelt (d.h. L�sungen,
	 *  in der alle schwarzen Felder so weit wie m�glich links bzw. rechts liegen).
	 *  Auf Basis der beiden L�sungen werden die �bergebenen HashMaps bef�llt.
	 *  Die HashMaps geben dann an, im wievielten Bedingungsblock ein Feld in der jeweiligen L�sung
	 *  liegt.
	 *  F�r schwarze Felder sind die HashMap immer gef�llt und geben die niedrigste (-> Rechtsl�sung)
	 *  oder h�chste (->Linksl�sung) Bedingung f�r das Feld an.
	 *  F�r wei�e Felder ist die HashMap nie gef�llt.
	 *  F�r unknown-Felder ist die HashMap nur gef�llt, wenn die Felder in der Links- bzw.
	 *  Rechtsl�sung schwarz sind.
	 *  
	 *  @param feld2blockLinksloesung
	 *  		Die HashMap, in die die geforderte Zuordnung f�r die Linksl�sung eingetragen wird
	 *  @param feld2blockRechtsloesung
	 *  		Die HashMap, in die die geforderte Zuordnung f�r die Rechtsl�sung eingetragen wird
	 *  @return
	 *  		false, wenn die Reihe unl�sbar ist und daher keine Links- bzw. Rechtsl�sung existiert.
	 */
	private boolean erzeugeLoesungsHashMaps(final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		if (!this.reiheLoesbarRekursion(true)) {
			return false;
		}
		ArrayList<Feldstatus> linksloesung = this.loesung;
		
		//Versuche, eine Rechts-L�sung zu finden.
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
	 * Wenn ein Feld sowohl in der Rechtsl�sung als auch in der Linksl�sung zum gleichen Block geh�rt,
	 * muss es schwarz sein. Dementsprechende Ergebnisse werden in die Reihe eingetragen.
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung f�r die Linksl�sung enth�lt
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung f�r die Rechtsl�sung enth�lt
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
	 * F�r jeden schwarzen Block der Reihe werden auf Basis der Links- bzw. Rechtsl�sung 
	 * die m�glichen Bedingungen ermittelt. Besitzen alle m�glichen Bedingungen eine L�nge
	 * kleiner oder gleich der L�nge des Blocks, so wird der Block durch wei�e Felder an
	 * beiden Seiten "abgeschlossen".
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung f�r die Linksl�sung enth�lt
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung f�r die Rechtsl�sung enth�lt
	 */
	private void schliesseFertigeBloeckeMitWeissenFeldernAb(
			final HashMap<Feld, Integer> feld2blockLinksloesung,
			final HashMap<Feld, Integer> feld2blockRechtsloesung) {
		// Zun�chst werden die Bl�cke der aktuellen Reihe ermittelt:
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

		// F�r alle Bl�cke werden die in Frage kommenden Bedingungen und hieraus die maximal m�gliche
		// L�nge ermittelt
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
			
			// Ist die L�nge des aktuellen Block == maximallaengeBlock, so muss der Block
			// links und rechts mit wei�en Feldern abschlie�en:
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
	 *  Wenn ein Feld sowohl in der Linksl�sung als auch in der Rechtsl�sung wei� ist und
	 *  in beiden F�llen zwischen den selben Bedingungsbl�cken liegt, muss es wei� sein!
	 *  Dementsprechende Ergebnisse werden in die Reihe eingetragen.
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung f�r die Linksl�sung enth�lt
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung f�r die Rechtsl�sung enth�lt
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
	 *  Geht alle "L�cken" (d.h. Bereiche zwischen wei�en Feldern) durch. Kommen f�r eine L�cke
	 *  nur Bl�cke in Frage, die allesamt l�nger als die L�cke sind, wird die L�cke
	 *  mit wei�en Feldern aufgef�llt.
	 * @param feld2blockLinksloesung
	 *  		Die HashMap, die die Zuordnung f�r die Linksl�sung enth�lt
	 * @param feld2blockRechtsloesung
	 *  		Die HashMap, die die Zuordnung f�r die Rechtsl�sung enth�lt
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
					// Maximale L�nge aller in Frage kommender Bl�cke ermitteln
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
	 *  Sukzessive Felder Schwarz oder Wei� setzen. Wenn dadurch Reihe unl�sbar, muss das jeweils
	 *  andere der korrekte Wert sein!
	 *  Achtung: Durch diese Methode werden zwar garantiert alle "sicheren" Informationen gefunden
	 *  und eingetragen, daf�r ist aber die Laufzeit des Verfahrens bei sehr gro�en Nonogrammen
	 *  katastrophal.
	 */
	private void loesePerRekursion() {
		for (Feld feld : this.reihe) {
			if (!feld.hasStatus(Feldstatus.unknown)) {
				continue;
			}
			
			//F�r Seiteneffektfreiheit m�ssen alle �nderungen im Anschluss revidiert werden!
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
			feld.setStatus(Feldstatus.unknown); //F�r Seiteneffektfreiheit
		}
	}
	
	/**
	 * Pr�ft, ob die Zeile l�sbar sein k�nnte. Das Verfahren h�ngt vom Parameter
	 * "Rekursionsverfahren zul�ssig" ab. Kann die Methode de L�sbarkeit nicht genau bestimmen,
	 * wird im Zweifel true zur�ckgegeben.
	 */
	boolean reiheLoesbar() {
		if (this.solver.getOptionRekursion()) {
			return this.reiheLoesbarRekursion(false);
		}
		return this.reihePlausibel();
	}
	
	/**
	 * Versucht mit Hilfe des Rekursionsverfahrens, eine L�sung f�r die aktuelle Reihe zu finden, indem
	 * die Reihe von links aufgef�llt wird. Wird eine L�sung gefunden, so wird diese ggfs. 
	 * in this.loesung abgelegt.
	 * Die Funktion arbeitet zwar mit der �bergebenen Reihe, ist aber seiteneffektfrei.
	 * 
	 * @param loesungAblegen
	 * 		gibt an, ob eine eventuell vorhandene L�sung in this.loesung abgelegt werden soll.
	 * 
	 * @return true, wenn eine zul�ssige L�sung existiert.
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
		
		// Wenn Reihe komplett ausgef�llt und zul�ssig
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
			unknownFeld.setStatus(Feldstatus.unknown);		// F�r Seiteneffektfreiheit
			return true;
		}
		unknownFeld.setStatus(Feldstatus.white);
		if (this.reiheLoesbarRekursion(loesungAblegen)) {
			unknownFeld.setStatus(Feldstatus.unknown);		// F�r Seiteneffektfreiheit
			return true;
		}
		unknownFeld.setStatus(Feldstatus.unknown);			// F�r Seiteneffektfreiheit
		return false;
	}
	
	/**
	 * Pr�ft die Teill�sung der aktuellen Reihe anhand von Heuristiken auf Plausibilit�t.
	 * 
	 * @return false, wenn die �bergebene Reihe offensichtlich unl�sbar ist.
	 */
	private boolean reihePlausibel() {
		
		if (this.getAnzahlSchwarzerFelderReihe() > this.anzahlSchwarzerFelderBedingungen)
			return false; // Wenn mehr Felder schwarz sind als erlaubt
		if (this.getLaengeLaengsterBlockReihe() > this.laengeLaengsterBlockBedingungen)
			return false; // Wenn bereits ein zu langer Block eingetragen ist
		
		// Wir analysieren nun den Teil links des ersten "unknown"-Feldes (ersteUnknownPosition)
		// und ermitteln dort die Bl�cke (bloecke) und ihre Anzahl (anzahlDerBloecke).
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
		// groesseDesAktuellenBlocks speichert jetzt die Gr��e des letzten, angefangenen Blocks
		// vor dem ersten "unknown"-Feld.
		
		if (bloecke.size() > this.bedingungen.size())
			return false;	// Wenn mehr Bl�cke gefunden wurden als erlaubt
		if (bloecke.size() == this.bedingungen.size() && groesseDesAktuellenBlocks != 0)
			return false;	// Wenn mehr Bl�cke gefunden wurden als erlaubt
		if (bloecke.size() < this.bedingungen.size()
				&& groesseDesAktuellenBlocks > this.bedingungen.get(bloecke.size()))
			return false;	// Wenn der letzte angefangene Block gr��er ist als der, der als n�chstes kommen muss
		for (int position = 0; position < bloecke.size(); position++) {
			if (bloecke.get(position) != this.bedingungen.get(position))
				return false;	// Wenn einer der gefundenen Bl�cke die falsche Gr��e hat
		}
		if (bloecke.size() < this.bedingungen.size()) {
			int benoetigterPlatzFuerRestbloecke = 0;
			for (int position = bloecke.size(); position < this.bedingungen.size(); position++) {
				benoetigterPlatzFuerRestbloecke += this.bedingungen.get(position) + 1;
			}
			benoetigterPlatzFuerRestbloecke += -groesseDesAktuellenBlocks - 1;
			if (this.reihe.size() - ersteUnknownPosition <  benoetigterPlatzFuerRestbloecke)
				return false;	// Wenn f�r die noch fehlenden Bl�cke der Platz nicht mehr reicht
		}
		return true;	// Wenn keine offensichtlichen Fehler gefunden wurden
	}
	
	/**
	 * Gibt die aktuelle Anzahl schwarzer Felder innerhalb der Reihe zur�ck.
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
	 * Gibt die L�nge des aktuell l�ngsten schwarzen Blocks in der Reihe zur�ck.
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