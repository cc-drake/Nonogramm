package de.drake.nonogramm.model;

/**
 * Feld eines Nonogramms.
 */
public class Feld {
	
	/**
	 * Der Zeilenindex des Feldes.
	 */
	private int zeile;
	
	/**
	 * Der Spaltenindex des Feldes.
	 */
	private int spalte;
	
	/**
	 * Der aktuelle Status des Feldes.
	 */
	private Feldstatus feldstatus;
	
	/**
	 * Erzeugt ein neues Nonogrammfeld.
	 * 
	 * @param zeile
	 * 		Der Zeilenindex des Feldes.
	 * @param spalte
	 * 		Der Spaltenindex des Feldes.
	 * @param feldstatus
	 * 		Der initiale Status des Nonogrammfelds.
	 */
	Feld(final int zeile, final int spalte, final Feldstatus feldstatus) {
		this.zeile = zeile;
		this.spalte = spalte;
		this.feldstatus = feldstatus;
	}
	
	/**
	 * Erzeugt ein neues Nonogrammfeld auf Basis eines anderen Feldes.
	 * 
	 * @param feld
	 * 		Das Feld, was als Basis für das neue Feld dient.
	 */
	Feld(final Feld feld) {
		this.zeile = feld.zeile;
		this.spalte = feld.spalte;
		this.feldstatus = feld.feldstatus;
	}
	
	/**
	 * Gibt den Zeilenindex des Feldes zurück.
	 */
	public int getZeile() {
		return this.zeile;
	}
	
	/**
	 * Gibt den Spaltenindex des Feldes zurück.
	 */
	public int getSpalte() {
		return this.spalte;
	}
	
	/**
	 * Prüft, ob das Feld den angegebenen Status besitzt.
	 * 
	 * @param feldstatus
	 * 		der Status, gegen den verglichen wird.
	 */
	public boolean hasStatus(final Feldstatus feldstatus) {
		return (this.feldstatus.equals(feldstatus));
	}
	
	/**
	 * Gibt den Status des Feldes zurück.
	 */
	public Feldstatus getStatus() {
		return this.feldstatus;
	}
	
	/**
	 * Setzt den Status des Nonogrammfeldes auf den angegebenen Wert.
	 * 
	 * @param feldstatus
	 * 		Der neue Status des Feldes.
	 */
	public void setStatus(final Feldstatus feldstatus) {
		this.feldstatus = feldstatus;
	}
	
	/**
	 * Stellt den Status des Feldes als String dar.
	 */
	public String toString() {
//		return "(" + this.zeile + "," + this.spalte + "," + this.feldstatus.toString() + ")";//TODO
		return this.feldstatus.toString();
	}
}