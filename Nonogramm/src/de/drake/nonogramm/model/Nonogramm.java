package de.drake.nonogramm.model;

import java.util.ArrayList;

import de.drake.nonogramm.tools.Matrix;
import de.drake.nonogramm.tools.MatrixIterator;
import de.drake.nonogramm.tools.Tools;

/**
 * Repräsentation von Nonogrammen
 */
public class Nonogramm implements Iterable<Feld> {

	/**
	 * Die Randbedingungen links vom Nonogramm
	 */
	private Matrix<Integer> linkeBedingungen;
	
	/**
	 * Die Randbedingungen über dem Nonogramm
	 */
	private Matrix<Integer> obereBedingungen;
	
	/**
	 * Die Hauptmatrix, die den Inhalt des Nonogramms beinhaltet
	 */
	private Matrix<Feld> feldmatrix;
	
	// Konstruktoren
	
	/**
	 * Konstruktor zum Erzeugen und Initialisieren eines leeren Nonogrammes
	 * 
	 * @param linkeBedingungen
	 * 		die Nebenbedingungen auf der linken Seite des Nonogramms
	 * @param obereBedingungen
	 * 		die Nebenbedingungen über dem Nonogramm
	 */
	public Nonogramm(final Matrix<Integer> linkeBedingungen, final Matrix<Integer> obereBedingungen)
			throws IllegalArgumentException {
		this.linkeBedingungen = linkeBedingungen;
		this.obereBedingungen = obereBedingungen;
		
		// Eingaben auf Plausibilität prüfen
		{
			int linkeBedingungenSumme = 0;
			int obereBedingungenSumme = 0;
			for (int zeilenindex = 0; zeilenindex < this.linkeBedingungen.getHoehe(); zeilenindex++) {
				ArrayList<Integer> linkeBedingung = this.linkeBedingungen.getZeile(zeilenindex);
				int linkeBedingungenZeilenSumme = Tools.summe(linkeBedingung);
				// Wenn in der Zeile nicht genug Platz für alle Blöcke ist...
				if (linkeBedingungenZeilenSumme + linkeBedingung.size() - 1 > obereBedingungen.getHoehe()) {
					throw (new IllegalArgumentException("Nonogramm nicht breit genug "
							+ "für linke Bedingungen"));
				}
				linkeBedingungenSumme += linkeBedingungenZeilenSumme;
			}
			for (int zeilenindex = 0; zeilenindex < this.obereBedingungen.getHoehe(); zeilenindex++) {
				ArrayList<Integer> obereBedingung = this.obereBedingungen.getZeile(zeilenindex);
				int obereBedingungenZeilenSumme = Tools.summe(obereBedingung);
				// Wenn in der Spalte nicht genug Platz für alle Blöcke ist...
				if (obereBedingungenZeilenSumme + obereBedingung.size() - 1 > linkeBedingungen.getHoehe()) {
					throw (new IllegalArgumentException("Nonogramm nicht hoch genug "
							+ "für obere Bedingungen"));
				}
				obereBedingungenSumme += obereBedingungenZeilenSumme;
			}
			if (linkeBedingungenSumme != obereBedingungenSumme)
				throw (new IllegalArgumentException("Summe linker Bedingungen ("
						+ linkeBedingungenSumme + ") ist ungleich der Summe oberer Bedingungen ("
								+ obereBedingungenSumme + ")"));
		}
		this.feldmatrix = new Matrix<Feld>(linkeBedingungen.getHoehe(),
				obereBedingungen.getHoehe());
		for (int zeile = 0; zeile < this.getHoehe(); zeile++) {
			for (int spalte = 0; spalte < this.getBreite(); spalte++) {
				this.feldmatrix.set(zeile, spalte, new Feld(zeile, spalte, Feldstatus.unknown));
			}
		}
	}

	/**
	 * Copy-Konstruktor zum Erzeugen und Initialisieren einer Kopie des übergebenen Nonogrammes.
	 * Hierbei ist das neue Nonogramm unabhängig vom alten, es gibt also keine Seiteneffekte.
	 * 
	 * @param nonogramm
	 * 		das zu kopierende Nonogramm
	 */
	public Nonogramm(final Nonogramm nonogramm) {
		this.linkeBedingungen = nonogramm.linkeBedingungen;
		this.obereBedingungen = nonogramm.obereBedingungen;
		this.feldmatrix = new Matrix<Feld>(nonogramm.getHoehe(), nonogramm.getBreite());
		for (int zeile = 0; zeile < this.getHoehe(); zeile++) {
			for (int spalte = 0; spalte < this.getBreite(); spalte++) {
				this.feldmatrix.set(zeile, spalte, new Feld(nonogramm.get(zeile, spalte)));
			}
		}
	}
		
	// Object-Methoden
	
	/**
	 * Methode zum Erzeugen einer Kopie des akuellen Nomogramms
	 * 
	 * @return die erstellte Kopie als Object
	 */
	public Object clone() {
		return new Nonogramm(this);
	}
	
	/**
	 * Stellt den Inhalt des Nonogramms als formatierten String dar.
	 * 
	 * @return der erzeugte String
	 */
	public String toString() {
		// Kopfsatz schreiben
		String result = "   ";
		for (int i=0; i<Math.min(10, this.feldmatrix.getBreite()); i++)
			result += i + " ";
		result += " ";
		for (int i=11; i<this.feldmatrix.getBreite(); i++,i++)
			result += i + "  ";
		result += "\n  ";
		for (int i=0; i<this.feldmatrix.getBreite(); i++)
			result += "__";
		result += "\n";
		
		//Inhalt ausgeben
		for (int zeile = 0; zeile < Math.min(10, this.feldmatrix.getHoehe()); zeile++) {
			result += zeile + " |";
			for (int spalte = 0; spalte < this.feldmatrix.getBreite(); spalte++) {
				result += this.feldmatrix.get(zeile, spalte) + " ";
			}
			result += "\n";
		}
		for (int zeile = 10; zeile < this.feldmatrix.getHoehe(); zeile++) {
			result += zeile + "|";
			for (int spalte = 0; spalte < this.feldmatrix.getBreite(); spalte++) {
				result += this.feldmatrix.get(zeile, spalte) + " ";
			}
			result += "\n";
		}
		return result;
	}
	
	/**
	 * Prüft zwei Nonogramm-Objekte daraufhin, ob ihre Teillösungen identisch sind.
	 * 
	 * @return das Objekt, das mit dem aktuellen Nonogramm verglichen werden soll
	 */
	public boolean equals(Object nonogrammObject) {
		Nonogramm nonogramm = (Nonogramm) nonogrammObject;
		return this.feldmatrix.equals(nonogramm.feldmatrix);
	}

	// Instanzmethoden
	
	/**
	 * Gibt die Höhe des Nonogramms zurück.
	 */
	public int getHoehe() {
		return this.feldmatrix.getHoehe();
	}
	
	/**
	 * Gibt die Breite des Nonogramms zurück.
	 */
	public int getBreite() {
		return this.feldmatrix.getBreite();
	}
	
	/**
	 * Gibt die linken Bedingungen zu einer Zeile zurück
	 * 
	 * @param zeilenindex
	 * 		Der Index der Zeile, deren linke Bedingungen gefragt sind. Für die erste Zeile
	 * 		ist der Index "0" zu verwenden.
	 */
	public ArrayList<Integer> getLinkeBedingungen(final int zeilenindex) {
		return this.linkeBedingungen.getZeile(zeilenindex);
	}
	
	/**
	 * Gibt die oberen Bedingungen zu einer Spalte zurück
	 * 
	 * @param spaltenindex
	 * 		Der Index der Spalte, deren obere Bedingungen gefragt sind. Für die erste Spalte
	 * 		ist der Index "0" zu verwenden.
	 */
	public ArrayList<Integer> getObereBedingungen(final int spaltenindex) {
		return this.obereBedingungen.getZeile(spaltenindex);
	}
	
	/**
	 * Gibt ein ausgewähltes Feld des Nonogramms zurück.
	 * 
	 * @param zeile
	 * 		Der Zeilenindex der angefragten Stelle
	 * @param spalte
	 * 		Der Spaltenindex der angefragten Stelle
	 */
	private Feld get(final int zeile, final int spalte) {
		return this.feldmatrix.get(zeile, spalte);
	}
	
	/**
	 * Gibt eine Zeile des Nonogramms als ArrayList zurück. Beim Bearbeiten Seiteneffekte beachten!
	 * 
	 * @param zeile
	 * 		Der Zeilenindex der angefragten Zeile
	 */
	public ArrayList<Feld> getZeile(final int zeile) {
		return this.feldmatrix.getZeile(zeile);
	}
	
	/**
	 * Gibt eine Spalte des Nonogramms als ArrayList zurück. Beim Bearbeiten Seiteneffekte beachten!
	 * 
	 * @param spalte
	 * 		Der Spaltenindex der angefragten Zeile
	 */
	public ArrayList<Feld> getSpalte(final int spalte) {
		return this.feldmatrix.getSpalte(spalte);
	}
	
	/**
	 * Trägt einen Wert an eine Stelle des Nonogramms ein
	 * 
	 * @param zeile
	 * 		Der Zeilenindex der angefragten Stelle
	 * @param spalte
	 * 		Der Spaltenindex der angefragten Stelle
	 * @param wert
	 * 		Der Wert, der einzutragen ist
	 */
	public void setStatus(final int zeile, final int spalte, final Feldstatus wert) {
		this.feldmatrix.get(zeile, spalte).setStatus(wert);
	}
	
	/**
	 * Prüft, ob ein Feld des Nonogramms den angegebenen Status besitzt
	 * 
	 * @param zeile
	 * 		Der Zeilenindex der angefragten Stelle
	 * @param spalte
	 * 		Der Spaltenindex der angefragten Stelle
	 * @param status
	 * 		Der Status, gegen den verglichen wird.
	 */
	public boolean hasStatus(final int zeile, final int spalte, final Feldstatus status) {
		return this.get(zeile, spalte).hasStatus(status);
	}
	
	/**
	 * Gibt an, ob das Nonogramm vollständig ausgefüllt ist, d.h. keine "unknown"-Einträge
	 * mehr enthält.
	 */
	public boolean istVollstaendig() {
		for (Feld feld : this) {
			if (feld.hasStatus(Feldstatus.unknown)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Übernimmt die Einträge eines anderen Nonogramms in dieses Nonogramm.
	 * 
	 * @param nonogramm
	 * 		das andere Nonogramm
	 */
	public void uebernehme(final Nonogramm nonogramm) {
		this.feldmatrix = new Matrix<Feld>(nonogramm.feldmatrix);
	}

	/**
	 * Erzeugt einen Iterator, mit dem über das Nonogramm iteriert werden kann.
	 */
	public MatrixIterator<Feld> iterator() {
		return new MatrixIterator<Feld>(this.feldmatrix);
	}
}