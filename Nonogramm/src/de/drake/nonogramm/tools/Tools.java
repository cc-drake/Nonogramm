package de.drake.nonogramm.tools;

import java.util.ArrayList;

/**
 * Beinhaltet statische Methoden zur Unterstützung.
 */
public class Tools {
	
	/**
	 * Erzeugt eine neue boolsche ArrayList mit dem vordefinierten Wert.
	 * 
	 * @param size
	 * 		die Länge der ArrayList
	 * @param value
	 * 		der Default-Wert, der in der neuen ArrayList eingetragen werden soll
	 */
	public static ArrayList<Boolean> newBooleanArrayList(final int size, final boolean value) {
		ArrayList<Boolean> result = new ArrayList<Boolean>(size);
		for (int index = 0; index < size; index++) {
			result.add(value);
		}
		return result;
	}
	
	/**
	 * Prüft, ob kein Element einer boolschen Liste true ist (d.h. ob
	 * alle Elemente == false sind).
	 * 
	 * @param liste
	 * 		die boolsche Liste
	 * 
	 * @return true, wenn alle Elemente == false
	 */
	public static boolean keine(final ArrayList<Boolean> liste) {
		for (boolean eintrag : liste) {
			if (eintrag == true) return false;
		}
		return true;
	}
	
	/**
	 * Bildet die Summe aller Elemente einer Integer-ArrayList.
	 * 
	 * @param liste
	 * 		die Integer-ArrayList
	 */
	public static int summe(final ArrayList<Integer> liste) {
		int summe = 0;
		for (int index = 0; index < liste.size(); index++) {
			summe += liste.get(index);
		}
		return summe;
	}
}