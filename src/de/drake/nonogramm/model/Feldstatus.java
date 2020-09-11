package de.drake.nonogramm.model;

/**
 * Enumerator, der den Status eines Nonogrammfeldes abbildet.
 * Mögliche Werte sind: "unknown", "black", "white".
 */
public enum Feldstatus {
	/**
	 * Indiziert, dass keine Informationen über dieses Feld vorliegen.
	 */
	unknown,
	
	/**
	 * Indiziert, dass dieses Feld schwarz ausgemalt werden muss.
	 */
	black,
	
	/**
	 * Indiziert, dass dieses Feld frei bleibt.
	 */
	white ;
	
	/**
	 * Gibt die Belegung eines Feldes als String aus. Hierbei wird "unknown" als Fragezeichen,
	 * "black" als Quadrat und "white" als Punkt dargestellt.
	 * 
	 * @return der erstellte String
	 */
	public String toString() {
		switch (this) {
		case unknown: return "?";
		case black: return "\u25A0";
//		case black: return "x";//TODO
		case white: return "\u00B7";
		}
		return "";
	}
}