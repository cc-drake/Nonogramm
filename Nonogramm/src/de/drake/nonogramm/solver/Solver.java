package de.drake.nonogramm.solver;

import java.util.ArrayList;

import de.drake.nonogramm.model.Feld;
import de.drake.nonogramm.model.Feldstatus;
import de.drake.nonogramm.model.Nonogramm;
import de.drake.nonogramm.tools.Tools;

/**
 * Bereitstellung von L�sungsverfahren zu Nonogrammen
 */
public class Solver {
	
	/**
	 * Das Nonogramm, welches durch den Solver gel�st werden soll.
	 */
	private Nonogramm nonogramm;

	/**
	 * Gibt an, in welchen Zeilen seit der letzten Bearbeitung �nderungen stattgefunden haben.
	 */
	private ArrayList<Boolean> zeileIstInteressant;
	
	/**
	 * Gibt an, in welchen Spalten seit der letzten Bearbeitung �nderungen stattgefunden haben.
	 */
	private ArrayList<Boolean> spalteIstInteressant;
	
	/**
	 * Option, ob die Verwendung des Rekursionsverfahrens zur L�sung von Zeilen/Spalten zul�ssig ist.
	 */
	private boolean optionRekursion;
	
	/**
	 * Modul zur L�sung von Reihen (d.h. Nonogrammzeilen oder -spalten).
	 */
	private ReihenSolver reihensolver = new ReihenSolver(this);
	
	/**
	 * Erzeugt und initialisiert einen Solver zur L�sung eines Nonogramms.
	 * 
	 * @param nonogramm
	 * 		Das Nonogramm, das vom Solver gel�st werden soll
	 * @param optionRekursion
	 * 		Gibt an, ob die Option "Rekursionsverfahren zul�ssig" gesetzt werden soll.
	 */
	public Solver(final Nonogramm nonogramm, final boolean optionRekursion) {
		this.nonogramm = nonogramm;
		this.zeileIstInteressant = new ArrayList<Boolean>(this.nonogramm.getHoehe());
		for (int position = 0; position < this.nonogramm.getHoehe(); position++) {
			this.zeileIstInteressant.add(true);
		}
		this.spalteIstInteressant = new ArrayList<Boolean>(this.nonogramm.getBreite());
		for (int position = 0; position < this.nonogramm.getBreite(); position++) {
			this.spalteIstInteressant.add(true);
		}
		this.optionRekursion = optionRekursion;
	}
	
	/**
	 * Erzeugt und initialisiert einen Solver zur L�sung eines Nonogramms.
	 * 
	 * @param nonogramm
	 * 		Das Nonogramm, das vom Solver gel�st werden soll
	 * @param zeileIstInteressant
	 * 		Eine Liste, welche der Zeilen derzeit interessant sind
	 * @param spalteIstInteressant
	 * 		Eine Liste, welche der Spalten derzeit interessant sind
	 */
	private Solver(final Nonogramm nonogramm, final ArrayList<Boolean> zeileIstInteressant,
			final ArrayList<Boolean> spalteIstInteressant) {
		this.nonogramm = nonogramm;
		this.zeileIstInteressant = zeileIstInteressant;
		this.spalteIstInteressant = spalteIstInteressant;
	}
	
	/**
	 * L�st das Nonogramm soweit wie m�glich ohne Verwendung des Trial-And-Error-Verfahrens.
	 */
	public void loeseEinfach() {
		while (!this.nonogramm.istVollstaendig() && !(Tools.keine(this.zeileIstInteressant)
				&& Tools.keine(this.spalteIstInteressant)) && this.alleZeilenUndSpaltenPlausibel()) {
			this.bearbeiteZeilen();
			this.bearbeiteSpalten();
		}
	}
	
	/**
	 * L�st das Nonogramm so weit wie m�glich mit Ber�cksichtigung des Trial-And-Error-Verfahrens.
	 */
	public void loese() {
		while (!this.nonogramm.istVollstaendig()) {
			this.bearbeiteZeilen();
			this.bearbeiteSpalten();
			if (Tools.keine(this.zeileIstInteressant) && Tools.keine(this.spalteIstInteressant)
					&& !this.nonogramm.istVollstaendig()) {
				if (this.trialAndError() == false)
					return;
			}
		}
	}
	
	/**
	 * F�llt alle Zeilen so weit wie m�glich aus.
	 */
	public void bearbeiteZeilen() {
		for (int zeilenindex = 0; zeilenindex < this.nonogramm.getHoehe(); zeilenindex++) {
			if (!this.zeileIstInteressant.get(zeilenindex))
				continue;
			System.out.println("Bearbeite Zeile " + zeilenindex);
			this.reihensolver.set(this.nonogramm.getZeile(zeilenindex),
					this.nonogramm.getLinkeBedingungen(zeilenindex));
			this.reihensolver.bearbeiteReihe();
			this.zeileIstInteressant.set(zeilenindex, false);
		}
	}
	
	/**
	 * F�llt alle Spalten so weit wie m�glich aus.
	 */
	public void bearbeiteSpalten() {
		for (int spaltenindex = 0; spaltenindex < this.nonogramm.getBreite(); spaltenindex++) {
			if (!this.spalteIstInteressant.get(spaltenindex))
					continue;
			System.out.println("Bearbeite Spalte " + spaltenindex);
			this.reihensolver.set(this.nonogramm.getSpalte(spaltenindex),
					this.nonogramm.getObereBedingungen(spaltenindex));
			this.reihensolver.bearbeiteReihe();
			this.spalteIstInteressant.set(spaltenindex, false);
		}
	}
	
	/**
	 * Erweitert die aktuelle Teill�sung des Nonogramms mit Hilfe des Trial-And-Error-Verfahrens.
	 * 
	 * @return Liefert false, wenn eine Erweiterung der Teill�sung nicht m�glich war
	 * (z.B. weil Nonogramm nicht eindeutig l�sbar war oder Fehler enthielt).
	 */
	public boolean trialAndError() {
		for (Feld feld : this.nonogramm) {
			if (!feld.hasStatus(Feldstatus.unknown))
				continue;
			System.out.println("Versuche Feld " + feld.getZeile() + ", " + feld.getSpalte());
			Nonogramm trialnonogramm = new Nonogramm(this.nonogramm);
			trialnonogramm.setStatus(feld.getZeile(), feld.getSpalte(), Feldstatus.black);
			ArrayList<Boolean> trialZeileIstInteressant =
					Tools.newBooleanArrayList(this.nonogramm.getHoehe(), false);
			trialZeileIstInteressant.set(feld.getZeile(), true);
			ArrayList<Boolean> trialSpalteIstInteressant =
					Tools.newBooleanArrayList(this.nonogramm.getBreite(), false);
			trialSpalteIstInteressant.set(feld.getSpalte(), true);
			Solver trialSolver =
					new Solver(trialnonogramm, trialZeileIstInteressant, trialSpalteIstInteressant);
			trialSolver.loeseEinfach();
			// Wenn L�sung Fehler beinhaltet...
			if (!trialSolver.alleZeilenUndSpaltenPlausibel()) {
				feld.setStatus(Feldstatus.white);
				this.vermeldeAenderung(feld);
				return true;
			}
			// Wenn L�sung vollst�ndig und ohne Fehler ist...
			if (trialnonogramm.istVollstaendig()) {
				this.nonogramm.uebernehme(trialnonogramm);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Pr�ft, ob s�mtliche Zeilen und Spalten des Nonogramms plausibel sind. 
	 */
	private boolean alleZeilenUndSpaltenPlausibel() {
		for (int zeilenindex = 0; zeilenindex < this.nonogramm.getHoehe(); zeilenindex++) {
			this.reihensolver.set(this.nonogramm.getZeile(zeilenindex),
					this.nonogramm.getLinkeBedingungen(zeilenindex));
			if (!this.reihensolver.reiheLoesbar()) {
				return false;
			}
		}
		for (int spaltenindex = 0; spaltenindex < this.nonogramm.getBreite(); spaltenindex++) {
			this.reihensolver.set(this.nonogramm.getSpalte(spaltenindex),
					this.nonogramm.getObereBedingungen(spaltenindex));
			if (!this.reihensolver.reiheLoesbar()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Informiert den Solver, dass an einem Feld eine �nderung stattgefunden hat und die betroffene
	 * Zeile bzw. Spalte neu gepr�ft werden kann.
	 */
	void vermeldeAenderung(final Feld feld) {
		this.zeileIstInteressant.set(feld.getZeile(), true);
		this.spalteIstInteressant.set(feld.getSpalte(), true);
	}

	/**
	 * �ndert die Option "Rekursionsverfahren zul�ssig".
	 * 
	 * @param isAllowed
	 * 		Gibt an, ob das Verfahren zul�sig ist.
	 */
	public void setOptionRekursion(boolean isAllowed) {
		this.optionRekursion = isAllowed;
		if (isAllowed) {
			for (Feld feld : this.nonogramm) {
				this.vermeldeAenderung(feld);
			}
		}
	}
	
	/**
	 * Gibt den Status der Option "Rekursionsverfahren zul�ssig" zur�ck.
	 */
	boolean getOptionRekursion() {
		return this.optionRekursion;
	}
}