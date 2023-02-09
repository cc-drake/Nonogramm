package de.drake.nonogramm.tools;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Repräsentation von verallgemeinerten Matrizen. Hierbei können die Zeilen der Matrix auch
 * unterschiedliche Längen besitzen.
 * @param <ContentClass> Klasse der Einträge, z.B. Integer
 */
public class Matrix<ContentClass> implements Iterable<ContentClass> {
	
	// Instanzattribute
	
	/**
	 * Speichert den Inhalt der Matrix
	 */
	private ArrayList<ArrayList<ContentClass>> array;
	
	/**
	 * Speichert die Höhe der Matrix
	 */
	private int hoehe;
	
	/**
	 * Speichert die Breite der Matrix. Im Falle ungleicher Zeilenlängen wird die kleinste Zeilenlänge genommen.
	 */
	private int breite;
	
	// Konstruktoren
	
	/**
	 * Konstruktor zum Erzeugen einer Matrix einer bestimmten Größe. Die Matrix
	 * wird mit "null" initialisiert.
	 * 
	 * @param hoehe
	 * 		die Anzahl der Zeilen
	 * @param breite
	 * 		die Anzahl der Spalten
	 */
	public Matrix(final int hoehe, final int breite) {
		this(hoehe, breite, null);
	}
	
	/**
	 * Konstruktor zum Erzeugen und Initialisieren einer Matrix
	 * einer bestimmten Größe mit einem festen Wert
	 * 
	 * @param hoehe
	 * 		die Anzahl der Zeilen
	 * @param breite
	 * 		die Anzahl der Spalten
	 * @param wert
	 * 		der Wert, mit dem die Matrix initialisiert werden soll
	 */
	public Matrix(final int hoehe, final int breite, final ContentClass wert) {
		this.hoehe = hoehe;
		this.breite = breite;
		this.array = new ArrayList<ArrayList<ContentClass>>(hoehe);
		for (int zeile = 0; zeile < hoehe; zeile++) {
			this.array.add(new ArrayList<ContentClass>(breite));
			while (this.getZeilenbreite(zeile) < breite) {
				this.array.get(zeile).add(wert);
			}
		}
	}
	
	/**
	 * Konstruktor zum Erzeugen und Initialisieren einer Matrix
	 * aus einer ArrayList-Struktur
	 * 
	 * @param arrayList
	 * 		die ArrayList, aus der die Matrix gebaut werden soll
	 */
	public Matrix(final ArrayList<ArrayList<ContentClass>> arrayList) {
		this.array = new ArrayList<ArrayList<ContentClass>>(arrayList);
		this.hoehe = arrayList.size();
		if (this.hoehe == 0) {
			this.breite = 0;
		} else {
			this.breite = this.array.get(0).size();
			for (int zeile = 1; zeile < this.hoehe; zeile++) {
				if (this.array.get(zeile).size() < this.breite)
					this.breite = this.array.get(zeile).size();
			}
		}
	}

	/**
	 * Copy-Konstruktor zum Erzeugen und Initialisieren einer Kopie der übergebenen Matrix.
	 * Der Inhalt der Matrix wird hierbei nicht dupliziert, Seiteneffekte beachten!
	 * 
	 * @param matrix
	 * 		die zu kopierende Matrix
	 */
	public Matrix(final Matrix<ContentClass> matrix) {
		this.hoehe = matrix.hoehe;
		this.breite = matrix.breite;
		this.array = new ArrayList<ArrayList<ContentClass>>(matrix.hoehe);
		for (int zeile = 0; zeile < hoehe; zeile++) {
			this.array.add(new ArrayList<ContentClass>(matrix.getZeile(zeile)));
		}
	}
	
	
	// Object-Methoden
	
	/**
	 * Erstellt eine Kopie eines Matrix-Objektes.
	 * 
	 * @return die Kopie als Object
	 */
	public Object clone() {
		return new Matrix<ContentClass>(this);
	}
	
	/**
	 * Stellt eine Matrix als String dar.
	 * 
	 * @return der erstellte String
	 */
	public String toString() {
		return this.array.toString();
	}
	
	/**
	 * Prüft die Matrix auf Gleichheit zu einem Object
	 * 
	 * @param matrixObject
	 * 		das Objekt, mit dem die Matrix verglichen werden soll
	 * 
	 * @return true, wenn das übergebene Objekt eine Matrix mit identischem Inhalt ist
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(final Object matrixObject) {
		if (matrixObject.getClass().toString().contains("Matrix")) {
			return this.equals(((Matrix<ContentClass>) matrixObject));
		}
		return false;
	}
	
	/**
	 * Prüft zwei Matrizen auf Gleichheit.
	 * 
	 * @param matrix
	 * 		die Matrix, die mit der aktuellen Matrix verglichen werden soll
	 * 
	 * @return true, wenn beide Matrizen die gleichen Objekte beinhalten
	 */
	public boolean equals(final Matrix<ContentClass> matrix) {
		if (this.hoehe != matrix.hoehe)
			return false;
		for (int zeile = 0; zeile < this.hoehe; zeile++) {
			if (!this.getZeile(zeile).equals(matrix.getZeile(zeile))) {
				return false;
			}
		}
		return true;
	}
	
	
	// get/set-Methoden
	
	/**
	 * get-Methode für die Höhe der Matrix
	 * 
	 * @return die Höhe der Matrix
	 */
	public int getHoehe() {
		return this.hoehe;
	}
	
	/**
	 * get-Methode für die Breite der Matrix. Im Falle ungleicher Zeilenlängen
	 * wird die kleinste Zeilenlänge genommen.
	 * 
	 * @return die Breite der Matrix
	 */
	public int getBreite() {
		return this.breite;
	}
	
	/**
	 * get-Methode für die Breite einer ausgewählten Zeile der Matrix
	 * 
	 * @param zeile
	 * 		Index der Zeile, deren Breite angefordert wird
	 * 		
	 * @return die Breite der angeforderten Zeile
	 */
	public int getZeilenbreite(final int zeile) {
		return this.array.get(zeile).size();
	}
	
	/**
	 * get-Methode für einen ausgewählten Eintrag der Matrix
	 * 
	 * @param zeile
	 * 		Zeilenindex des angeforderten Eintrags
	 * @param spalte
	 * 		Spaltenindex des angeforderten Eintrags
	 * 		
	 * @return der Eintrag der Matrix an der Stelle (zeile, spalte)
	 */
	public ContentClass get(final int zeile, final int spalte) {
		return this.array.get(zeile).get(spalte);
	}
	
	/**
	 * set-Methode für einen ausgewählten Eintrag der Matrix
	 * 
	 * @param zeile
	 * 		Zeilennummer des zu setzenden Eintrags
	 * @param spalte
	 * 		Spaltennummer des zu setzenden Eintrags
	 * @param wert
	 * 		der Wert, auf den der Eintrag gesetzt werden soll
	 */
	public void set(final int zeile, final int spalte, final ContentClass wert) {
		this.array.get(zeile).set(spalte, wert);
	}
	
	/**
	 * get-Methode für eine ausgewählte Zeile der Matrix
	 * 
	 * @param zeile
	 * 		Index der angeforderten Zeile
	 * 		
	 * @return Die angeforderte Zeile. Seiteneffekte beachten!
	 */
	public ArrayList<ContentClass> getZeile(final int zeile) {
		ArrayList<ContentClass> copy = new ArrayList<ContentClass>(this.array.get(zeile));
		return copy;
	}
	
	/**
	 * get-Methode für eine ausgewählte Spalte der Matrix
	 * 
	 * @param spalte
	 * 		Index der angeforderten Spalte
	 * 		
	 * @return Die angeforderte Spalte. Seiteneffekte beachten!
	 */
	public ArrayList<ContentClass> getSpalte(final int spalte) {
		ArrayList<ContentClass> copy = new ArrayList<ContentClass>(this.hoehe);
		for (int zeile = 0; zeile < this.hoehe; zeile++) {
			if (spalte < this.getZeilenbreite(zeile))
			copy.add(this.get(zeile, spalte));
		}
		return copy;
	}
	
	/**
	 * Erstellt eine Integer-Matrix aus einem String. Hierbei werden als Eingabeformate sowohl
	 * [[1,2],[3],[]], {{1,2},{3},{}} als auch 1,2/3/ akzeptiert und Whitespaces ignoriert.
	 * 
	 * @param string
	 * 		der String, der die Matrix enthält
	 * 
	 * @throws IllegalArgumentException
	 * 		wird geworfen, wenn die Matrix im String nicht erkannt werden kann
	 * 
	 * @return die erzeugte Integer-Matrix
	 */
	public static Matrix<Integer> toIntMatrix(final String string) throws IllegalArgumentException {
		
		// Whitespaces entfernen
		String arrayString = "";
		Scanner scanner = new Scanner(string);
		while (scanner.hasNext()) {
			arrayString += scanner.next();
		}
		scanner.close();
		
		// Wenn das Format {{1,2},{3},{}} ist...
		if (arrayString.startsWith("{{") && arrayString.endsWith("}}")) {
			// Konvertiere zur 1,2/3/-Darstellung
			arrayString = arrayString.substring(2, arrayString.length()-2);
			arrayString = arrayString.replace("},{", "/");
		}
		
		// Wenn das Format [[12,2],[3],[]] ist...
		else if (arrayString.startsWith("[[") && arrayString.endsWith("]]")) {
			// Konvertiere zur 1,2/3/-Darstellung
			arrayString = arrayString.substring(2, arrayString.length()-2);
			arrayString = arrayString.replace("],[", "/");
		}
			
		// Nun ist das Format 12,2/3/.
		// Damit leere Zeilen nicht übersehen werden, fügen wir einige Kommas hinzu...
		arrayString = arrayString.replace("/", ",/,");
		// ...und löschen doppelte wieder weg
		arrayString = arrayString.replace(",,", ",");
		
		ArrayList<ArrayList<Integer>> array = new ArrayList<ArrayList<Integer>>();
		Scanner hauptscanner = new Scanner(arrayString);
		hauptscanner.useDelimiter("/");
		int aktuelleZeile = 0;
		while (hauptscanner.hasNext()) {
			array.add(new ArrayList<Integer>());
			String zeile = hauptscanner.next();
			Scanner zeilenscanner = new Scanner(zeile);
			zeilenscanner.useDelimiter(",");
			while (zeilenscanner.hasNext()) {
				Scanner intScanner = new Scanner(zeilenscanner.next());
				if (!intScanner.hasNextInt()) {
					intScanner.close();
					throw new IllegalArgumentException("Die Eingabe lässt sich nicht als"
							+ " Matrix interpretieren");
				}
				array.get(aktuelleZeile).add(intScanner.nextInt());
				intScanner.close();
			}
			zeilenscanner.close();
			aktuelleZeile++;
		}
		hauptscanner.close();
		return new Matrix<Integer>(array);
	}

	/**
	 * Erzeugt einen Iterator, mit dem über die Matrix iteriert werden kann.
	 */
	public MatrixIterator<ContentClass> iterator() {
		return new MatrixIterator<ContentClass>(this);
	}
}