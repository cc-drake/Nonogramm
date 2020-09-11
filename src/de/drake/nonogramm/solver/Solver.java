package de.drake.nonogramm.solver;

import java.util.ArrayList;

import de.drake.nonogramm.model.Feld;
import de.drake.nonogramm.model.Feldstatus;
import de.drake.nonogramm.model.Nonogramm;
import de.drake.nonogramm.tools.Tools;

/**
 * Bereitstellung von Lösungsverfahren zu Nonogrammen
 */
public class Solver {
	
	/**
	 * Das Nonogramm, welches durch den Solver gelöst werden soll.
	 */
	private Nonogramm nonogramm;

	/**
	 * Gibt an, in welchen Zeilen seit der letzten Bearbeitung Änderungen stattgefunden haben.
	 */
	private ArrayList<Boolean> zeileIstInteressant;
	
	/**
	 * Gibt an, in welchen Spalten seit der letzten Bearbeitung Änderungen stattgefunden haben.
	 */
	private ArrayList<Boolean> spalteIstInteressant;
	
	/**
	 * Option, ob die Verwendung des Rekursionsverfahrens zur Lösung von Zeilen/Spalten zulässig ist.
	 */
	private boolean optionRekursion;
	
	/**
	 * Modul zur Lösung von Reihen (d.h. Nonogrammzeilen oder -spalten).
	 */
	private ReihenSolver reihensolver = new ReihenSolver(this);
	
	/**
	 * Erzeugt und initialisiert einen Solver zur Lösung eines Nonogramms.
	 * 
	 * @param nonogramm
	 * 		Das Nonogramm, das vom Solver gelöst werden soll
	 * @param optionRekursion
	 * 		Gibt an, ob die Option "Rekursionsverfahren zulässig" gesetzt werden soll.
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
	 * Erzeugt und initialisiert einen Solver zur Lösung eines Nonogramms.
	 * 
	 * @param nonogramm
	 * 		Das Nonogramm, das vom Solver gelöst werden soll
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
	 * Löst das Nonogramm soweit wie möglich ohne Verwendung des Trial-And-Error-Verfahrens.
	 */
	public void loeseEinfach() {
		while (!this.nonogramm.istVollstaendig() && !(Tools.keine(this.zeileIstInteressant)
				&& Tools.keine(this.spalteIstInteressant)) && this.alleZeilenUndSpaltenPlausibel()) {
			this.bearbeiteZeilen();
			this.bearbeiteSpalten();
		}
	}
	
	/**
	 * Löst das Nonogramm so weit wie möglich mit Berücksichtigung des Trial-And-Error-Verfahrens.
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
	 * Füllt alle Zeilen so weit wie möglich aus.
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
	 * Füllt alle Spalten so weit wie möglich aus.
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
	 * Erweitert die aktuelle Teillösung des Nonogramms mit Hilfe des Trial-And-Error-Verfahrens.
	 * 
	 * @return Liefert false, wenn eine Erweiterung der Teillösung nicht möglich war
	 * (z.B. weil Nonogramm nicht eindeutig lösbar war oder Fehler enthielt).
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
			// Wenn Lösung Fehler beinhaltet...
			if (!trialSolver.alleZeilenUndSpaltenPlausibel()) {
				feld.setStatus(Feldstatus.white);
				this.vermeldeAenderung(feld);
				return true;
			}
			// Wenn Lösung vollständig und ohne Fehler ist...
			if (trialnonogramm.istVollstaendig()) {
				this.nonogramm.uebernehme(trialnonogramm);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Prüft, ob sämtliche Zeilen und Spalten des Nonogramms plausibel sind. 
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
	 * Informiert den Solver, dass an einem Feld eine Änderung stattgefunden hat und die betroffene
	 * Zeile bzw. Spalte neu geprüft werden kann.
	 */
	void vermeldeAenderung(final Feld feld) {
		this.zeileIstInteressant.set(feld.getZeile(), true);
		this.spalteIstInteressant.set(feld.getSpalte(), true);
	}

	/**
	 * Ändert die Option "Rekursionsverfahren zulässig".
	 * 
	 * @param isAllowed
	 * 		Gibt an, ob das Verfahren zuläsig ist.
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
	 * Gibt den Status der Option "Rekursionsverfahren zulässig" zurück.
	 */
	boolean getOptionRekursion() {
		return this.optionRekursion;
	}
}