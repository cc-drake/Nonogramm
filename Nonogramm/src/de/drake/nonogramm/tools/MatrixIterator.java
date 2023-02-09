package de.drake.nonogramm.tools;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator f�r Matrizen.
 * 
 * @param <ContentClass>
 * 		Inhalt der Matrix, z.B. Integer.
 */
public class MatrixIterator<ContentClass> implements Iterator<ContentClass> {

	/**
	 * Die Matrix, �ber die iteriert wird.
	 */
	private Matrix<ContentClass> matrix;
	
	/**
	 * Der Index der aktuellen Zeile, �ber die iteriert wird.
	 */
	private int aktuelleZeile = -1;
	
	/**
	 * Der Index der aktuellen Spalte, �ber die iteriert wird.
	 */
	private int aktuelleSpalte;
	
	/**
	 * Erzeugt einen neuen MatrixIterator.
	 * 
	 * @param matrix
	 * 		Die Matrix, �ber die iteriert werden soll.
	 */
	public MatrixIterator(final Matrix<ContentClass> matrix) {
		this.matrix = matrix;
	}
	
	/**
	 * Gibt zur�ck, ob noch weitere Matrixfelder vorhanden sind.
	 */
	public boolean hasNext() {
		if (this.matrix.getHoehe() == 0)
			return false;
		if (this.aktuelleZeile > -1) {
			if (this.aktuelleSpalte < this.matrix.getZeilenbreite(this.aktuelleZeile) - 1)
				return true;
			if (this.aktuelleZeile == this.matrix.getHoehe() - 1)
				return false;
			if (this.matrix.getBreite() > 0)
				return true;
		}
		
		//pr�fen, ob es in den unteren Zeilen noch nichtleere Zeilen gibt
		for (int zeile = this.aktuelleZeile + 1; zeile < this.matrix.getHoehe(); zeile++) {
			if (this.matrix.getZeilenbreite(zeile) > 0)
				return true;
		}
		return false;
	}

	/**
	 * Gibt das n�chste Element der Iteration zur�ck.
	 * 
	 * @throws NoSuchElementException
	 * 		wird geworfen, wenn die Iteration am Ende der Matrix angelangt ist.
	 */
	public ContentClass next() throws NoSuchElementException {
		if (this.aktuelleZeile == -1
				|| this.aktuelleSpalte == this.matrix.getZeilenbreite(this.aktuelleZeile) - 1) {
			this.aktuelleZeile++;
			this.aktuelleSpalte = 0;
		} else {
			this.aktuelleSpalte++;
		}
		for (int zeile = this.aktuelleZeile; zeile < this.matrix.getHoehe(); zeile++) {
			try {
				return matrix.get(zeile, this.aktuelleSpalte);
			} catch (IndexOutOfBoundsException e) {
			}
		}
		throw new NoSuchElementException();
	}
}