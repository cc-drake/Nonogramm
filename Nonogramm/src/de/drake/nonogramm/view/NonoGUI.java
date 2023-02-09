package de.drake.nonogramm.view;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

import de.drake.nonogramm.model.*;
import de.drake.nonogramm.solver.Solver;
import de.drake.nonogramm.tools.Matrix;

/**
 * Stellt eine GUI für die Lösung von Nonogrammen zur Verfügung.
 */
class NonoGUI extends JFrame {
	
	// Attribute
	
	/**
	 * Die serialVersionUID für NonoGUI
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Das aktuelle Nonogramm, das gelöst werden soll
	 */
	Nonogramm nonogramm;
	
	/**
	 * Der Solver zur Lösung des Nonogramms
	 */
	Solver solver;
	
	/**
	 * Die TextArea, in dem die linken Nebenbedingungen des Nonogrammes stehen
	 */
	TextArea linkeBedingungenTextArea;
	
	/**
	 * Die TextArea, in dem die oberen Nebenbedingungen des Nonogrammes stehen
	 */
	TextArea obereBedingungenTextArea;
	
	/**
	 * Die TextArea, in die das aktuelle Nonogramm sowie vereinzelte Textmeldungen ausgegeben werden können
	 */
	TextArea ausgabeTextArea;
	
	/**
	 * Das Textfield, in dem die Nummer des aktuellen www.rozov.de-Nonogrammes steht
	 */
	TextField rozovNummerTextField;
	
	/**
	 * Die CheckBox, in der die Option "Rekursionsverfahren zulässig" angegeben ist
	 */
	JCheckBox optionRekursion;
	
	// Main-Methode
	
	/**
	 * Startet die GUI.
	 */
	public static void main(String[] arg) {
		NonoGUI gui = new NonoGUI("Nonogramm");
		gui.setVisible(true);
	}

	// Konstruktoren
	
	/**
	 * Konstruktor zum Erzeugen eines GUI-Fensters
	 * 
	 * @param titel
	 * 		der Titel des GUI-Fensters
	 */
	NonoGUI(final String titel) {
		super(titel);
		this.setSize(800,600);
		this.setLocation(100,60);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Panel west = new Panel();
		west.setPreferredSize(new Dimension(150,150));
		west.setLayout(new GridLayout(5,1));
		this.add(west, BorderLayout.WEST);
		
			Panel linkeBedingungenPanel = new Panel();
			linkeBedingungenPanel.setLayout(new BorderLayout());
			west.add(linkeBedingungenPanel);
			Label linkeBedingungenLabel = new Label("Linke Bedingungen:");
			linkeBedingungenPanel.add(linkeBedingungenLabel, BorderLayout.NORTH);
			this.linkeBedingungenTextArea = new TextArea();
			linkeBedingungenPanel.add(this.linkeBedingungenTextArea, BorderLayout.CENTER);
			
			Panel obereBedingungenPanel = new Panel();
			obereBedingungenPanel.setLayout(new BorderLayout());
			west.add(obereBedingungenPanel);
			Label obereBedingungenLabel = new Label("Obere Bedingungen:");
			obereBedingungenPanel.add(obereBedingungenLabel, BorderLayout.NORTH);
			this.obereBedingungenTextArea = new TextArea();
			obereBedingungenPanel.add(this.obereBedingungenTextArea, BorderLayout.CENTER);
			
			Panel generateFromArrayPanel = new Panel();
			generateFromArrayPanel.setLayout(new FlowLayout());
			west.add(generateFromArrayPanel);
			Button generateFromArray = new Button("Erstelle Nonogramm");
			generateFromArrayPanel.add(generateFromArray);
			
			Panel loadFromRozovPanel = new Panel();
			loadFromRozovPanel.setLayout(new BorderLayout());
			west.add(loadFromRozovPanel);
			Panel loadFromRozovContentPanel = new Panel();
			loadFromRozovContentPanel.setLayout(new BorderLayout());
			loadFromRozovPanel.add(loadFromRozovContentPanel, BorderLayout.NORTH);
			Label loadFromRozovLabel = new Label("Rozov.de-Nummer:");
			loadFromRozovContentPanel.add(loadFromRozovLabel, BorderLayout.NORTH);
			Panel rozovNummerPanel = new Panel();
			rozovNummerPanel.setLayout(new FlowLayout());
			loadFromRozovContentPanel.add(rozovNummerPanel, BorderLayout.CENTER);
			Button rozovNummerMinus = new Button("-");
			rozovNummerMinus.setPreferredSize(new Dimension(23,23));
			rozovNummerPanel.add(rozovNummerMinus);
			this.rozovNummerTextField = new TextField();
			this.rozovNummerTextField.setColumns(4);
			rozovNummerPanel.add(rozovNummerTextField);
			Button rozovNummerPlus = new Button("+");
			rozovNummerPlus.setPreferredSize(new Dimension(23,23));
			rozovNummerPanel.add(rozovNummerPlus);			
			Panel loadFromRozovButtonPanel = new Panel();
			loadFromRozovButtonPanel.setLayout(new FlowLayout());
			loadFromRozovContentPanel.add(loadFromRozovButtonPanel, BorderLayout.SOUTH);
			Button loadFromRozov = new Button("Lade Nonogramm");
			loadFromRozovButtonPanel.add(loadFromRozov);
			
			Panel optionsPanel = new Panel();
			optionsPanel.setLayout(new FlowLayout());
			west.add(optionsPanel);
			this.optionRekursion = new JCheckBox("Rekursionsverfahren");
			this.optionRekursion.setSelected(true);
			this.optionRekursion.addItemListener(new optionRekursionListener(this,
					this.optionRekursion));
			optionsPanel.add(this.optionRekursion);
			
		Panel south = new Panel();
		south.setLayout(new FlowLayout());
		this.add(south, BorderLayout.SOUTH);
		
			Button zuruecksetzen = new Button("Zurücksetzen");
			south.add(zuruecksetzen);
			
			Button bearbeiteZeilen = new Button("Bearbeite Zeilen");
			south.add(bearbeiteZeilen);
		
			Button bearbeiteSpalten = new Button("Bearbeite Spalten");
			south.add(bearbeiteSpalten);
			
			Button trialAndError = new Button("Trial & Error");
			south.add(trialAndError);
			
			Button loeseEinfach = new Button("Löse \"per Hand\"");
			south.add(loeseEinfach);

			Button loese = new Button("Löse komplett");
			south.add(loese);
			
			Button rozovRobot = new Button("rozov-Eintrag");
			south.add(rozovRobot);
			
		this.ausgabeTextArea = new TextArea();
		this.add(this.ausgabeTextArea, BorderLayout.CENTER);
		this.ausgabeTextArea.setFont(new Font("Courier", Font.PLAIN, 14));
		this.ausgabeTextArea.setEditable(false);
		
		this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		generateFromArray.addActionListener(new GenerateEvent(this));
		loadFromRozov.addActionListener(new LoadEvent(this));
		this.rozovNummerTextField.addActionListener(new LoadEvent(this));
		rozovNummerMinus.addActionListener(new rozovNummerPlusMinusEvent(-1, this));
		rozovNummerPlus.addActionListener(new rozovNummerPlusMinusEvent(1, this));
		zuruecksetzen.addActionListener(new GenerateEvent(this));
		bearbeiteZeilen.addActionListener(new BearbeiteZeilenEvent(this));
		bearbeiteSpalten.addActionListener(new BearbeiteSpaltenEvent(this));
		trialAndError.addActionListener(new TrialAndErrorEvent(this));
		loeseEinfach.addActionListener(new LoeseEinfachEvent(this));
		loese.addActionListener(new LoeseEvent(this));
		rozovRobot.addActionListener(new RozovRobotEvent(this));
	}
}

/**
 * Event, welches ein Nonogramm aus gegebenen Nebenbedingungen initialisiert
 */
class GenerateEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	GenerateEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}
	
	public void actionPerformed(ActionEvent e) {
		String linkeBedingungenString = nonoGUI.linkeBedingungenTextArea.getText();
		String obereBedingungenString = nonoGUI.obereBedingungenTextArea.getText();
		Matrix<Integer> linkeBedingungen, obereBedingungen;
		try {
			linkeBedingungen = Matrix.toIntMatrix(linkeBedingungenString);
		} catch (IllegalArgumentException exception) {
			nonoGUI.ausgabeTextArea.setText("Erstellung fehlgeschlagen.\nBitte Eingabeformat der linken Nebenbedingungen überprüfen!");
			nonoGUI.nonogramm = null;
			nonoGUI.solver = null;
			return;
		}
		try {
			obereBedingungen = Matrix.toIntMatrix(obereBedingungenString);
		} catch (IllegalArgumentException exception) {
			nonoGUI.ausgabeTextArea.setText("Erstellung fehlgeschlagen.\nBitte Eingabeformat der oberen Nebenbedingungen überprüfen!");
			nonoGUI.nonogramm = null;
			nonoGUI.solver = null;
			return;
		}
		try {
			nonoGUI.nonogramm = new Nonogramm(linkeBedingungen, obereBedingungen);
			nonoGUI.solver = new Solver(nonoGUI.nonogramm, this.nonoGUI.optionRekursion.isSelected());
		} catch (IllegalArgumentException error) {
			nonoGUI.ausgabeTextArea.setText("Erstellung fehlgeschlagen.\nDie Nebenbedingungen passen nicht zueinander!\n"
					+ error.getMessage());
			nonoGUI.nonogramm = null;
			nonoGUI.solver = null;
			return;
		}
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}

/**
 * Event, welches ein Nonogramm von www.rozov.de lädt und initialisiert
 */
class LoadEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	LoadEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}
	
	public void actionPerformed(ActionEvent e) {
		String rozovNummerString = nonoGUI.rozovNummerTextField.getText();
		char[] rozovNummerChar = rozovNummerString.toCharArray();
		if (rozovNummerString.equals("")) {
			nonoGUI.ausgabeTextArea.setText("Bitte die Nummer des Nonogramms auf www.rozov.de eingeben.");
			nonoGUI.nonogramm = null;
			nonoGUI.solver = null;
			return;
		}
		int rozovNummer = 0;
		for (char character : rozovNummerChar) {
			if (character < '0' || character > '9') {
				nonoGUI.ausgabeTextArea.setText("Ungültige Nummer.");
				nonoGUI.nonogramm = null;
				nonoGUI.solver = null;
				return;
			}
			rozovNummer = 10*rozovNummer + character - '0';
		}
		Scanner scanner;

		try {
			scanner = new Scanner(new URL("http://rozov.de/nonogram/spiel.php?num=" + rozovNummer).openStream());
		} catch (IOException error){
			nonoGUI.ausgabeTextArea.setText("Fehler beim Zugriff auf http://rozov.de/nonogram/spiel.php?num=" + rozovNummer);
			nonoGUI.nonogramm = null;
			nonoGUI.solver = null;
			return;
		}
		scanner.useDelimiter("\"");
		scanner.findWithinHorizon("n = new MainNonoPanel",1000000);
		scanner.next();
		nonoGUI.obereBedingungenTextArea.setText(scanner.next());
		scanner.next();
		nonoGUI.linkeBedingungenTextArea.setText(scanner.next());
        scanner.close();
        GenerateEvent generate = new GenerateEvent(nonoGUI);
        generate.actionPerformed(e);
	}
}

/**
 * Event, welches das Rozov-Nonogramm mit der akutuellen Nummer+-1 lädt und initialisiert
 */
class rozovNummerPlusMinusEvent implements ActionListener {
	NonoGUI nonoGUI;
	int richtung;
	
	rozovNummerPlusMinusEvent(final int richtung, final NonoGUI nonoGUI) {
		this.richtung = richtung;
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		String rozovNummerString = nonoGUI.rozovNummerTextField.getText();
		char[] rozovNummerChar = rozovNummerString.toCharArray();
		if (rozovNummerString.equals("")) {
			return;
		}
		int rozovNummer = 0;
		for (char character : rozovNummerChar) {
			if (character < '0' || character > '9') {
				return;
			}
			rozovNummer = 10*rozovNummer + character - '0';
		}
		nonoGUI.rozovNummerTextField.setText("" + (rozovNummer + richtung));
        LoadEvent load = new LoadEvent(nonoGUI);
        load.actionPerformed(e);
	}
}

/**
 * Event, welches die Zeilen des Nonogramms bearbeitet
 */
class BearbeiteZeilenEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	BearbeiteZeilenEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		if (nonoGUI.nonogramm == null) {
			nonoGUI.ausgabeTextArea.setText("Bitte erst ein Nonogramm generieren!");
			return;
		}
		nonoGUI.ausgabeTextArea.setText("Bitte warten...");
		nonoGUI.solver.bearbeiteZeilen();
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}

/**
 * Event, welches die Spalten des Nonogramms bearbeitet
 */
class BearbeiteSpaltenEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	BearbeiteSpaltenEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		if (nonoGUI.nonogramm == null) {
			nonoGUI.ausgabeTextArea.setText("Bitte erst ein Nonogramm generieren!");
			return;
		}
		nonoGUI.ausgabeTextArea.setText("Bitte warten...");
		nonoGUI.solver.bearbeiteSpalten();
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}

/**
 * Event, welches das Trial-And-Error-Verfahren auf das Nonogramm anwendet
 */
class TrialAndErrorEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	TrialAndErrorEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		if (nonoGUI.nonogramm == null) {
			nonoGUI.ausgabeTextArea.setText("Bitte erst ein Nonogramm generieren!");
			return;
		}
		nonoGUI.ausgabeTextArea.setText("Bitte warten...");
		nonoGUI.solver.trialAndError();
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}

/**
 * Event, welches versucht, das Nonogramm ohne Hilfe des Trial-And-Error-Verfahrens zu lösen
 */
class LoeseEinfachEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	LoeseEinfachEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		if (nonoGUI.nonogramm == null) {
			nonoGUI.ausgabeTextArea.setText("Bitte erst ein Nonogramm generieren!");
			return;
		}
		nonoGUI.ausgabeTextArea.setText("Bitte warten...");
		nonoGUI.solver.loeseEinfach();
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}

/**
 * Event, welches das Nonogramm löst
 */
class LoeseEvent implements ActionListener {
	NonoGUI nonoGUI;
	
	LoeseEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		if (nonoGUI.nonogramm == null) {
			nonoGUI.ausgabeTextArea.setText("Bitte erst ein Nonogramm generieren!");
			return;
		}
		nonoGUI.ausgabeTextArea.setText("Bitte warten...");
		nonoGUI.solver.loese();
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}

/**
 * Event, welches das Ergebnis eines Nonogramms auf www.rozov.de einträgt
 */
class RozovRobotEvent implements ActionListener {
	NonoGUI nonoGUI;
	Robot robot;
	int positionLinksObenX;
	int positionLinksObenY;
	int positionRechtsUntenX;
	int positionRechtsUntenY;
	
	
	RozovRobotEvent(final NonoGUI nonoGUI) {
		this.nonoGUI = nonoGUI;
	}

	public void actionPerformed(ActionEvent e) {
		
		if (nonoGUI.nonogramm == null) {
			nonoGUI.ausgabeTextArea.setText("Bitte erst ein Nonogramm generieren!");
			return;
		}
		try {
			this.robot = new Robot();
		} catch (AWTException e1) {
			nonoGUI.ausgabeTextArea.setText("Fehler beim Start des Robots");
			return;
		}
		nonoGUI.ausgabeTextArea.setText("Bitte Maus in 5 Sekunden ins linke obere" +
				" Feld des Nonogramms bewegen");
		this.robot.delay(5000);
		this.robot.mousePress(InputEvent.BUTTON1_MASK);
		this.robot.mouseRelease(InputEvent.BUTTON1_MASK);
		this.positionLinksObenX = MouseInfo.getPointerInfo().getLocation().x;
		this.positionLinksObenY = MouseInfo.getPointerInfo().getLocation().y;
		this.robot.delay(20);
		this.robot.mousePress(InputEvent.BUTTON1_MASK);
		this.robot.mouseRelease(InputEvent.BUTTON1_MASK);

		nonoGUI.ausgabeTextArea.setText("Links oben: " + this.positionLinksObenX
				+ ", " + this.positionLinksObenY + "\n"
				+ "Bitte Maus in 5 Sekunden ins rechte, untere" +
		" Feld des Nonogramms bewegen");
		this.robot.delay(5000);
		this.positionRechtsUntenX = MouseInfo.getPointerInfo().getLocation().x;
		this.positionRechtsUntenY = MouseInfo.getPointerInfo().getLocation().y;
		nonoGUI.ausgabeTextArea.setText("Links oben: " + this.positionLinksObenX
				+ ", " + this.positionLinksObenY + "\n"
				+ "Rechts unten: " + this.positionRechtsUntenX
				+ ", " + this.positionRechtsUntenY);
		this.robot.setAutoDelay(1);
		int neuePositionX, neuePositionY, button;
		for (int x = 0; x < nonoGUI.nonogramm.getHoehe(); x++)
			for (int y = 0; y < nonoGUI.nonogramm.getBreite(); y++) {
				if (this.nonoGUI.nonogramm.hasStatus(x, y, Feldstatus.unknown))
						continue;
				if (this.nonoGUI.nonogramm.hasStatus(x, y, Feldstatus.white)) {
					button = InputEvent.BUTTON3_MASK;
				} else {
					button = InputEvent.BUTTON1_MASK;
				}
				neuePositionX = this.positionLinksObenX
						+ (int) ((positionRechtsUntenX-this.positionLinksObenX) * y / (nonoGUI.nonogramm.getBreite()-1));
				neuePositionY = this.positionLinksObenY
				+ (int) ((positionRechtsUntenY-this.positionLinksObenY) * x / (nonoGUI.nonogramm.getHoehe()-1));
				this.robot.mouseMove(neuePositionX, neuePositionY);
				this.robot.mousePress(button);
				this.robot.mouseRelease(button);
			}
		nonoGUI.ausgabeTextArea.setText(nonoGUI.nonogramm.toString());
	}
}
	
/**
 * Event, welches das Ergebnis eines Nonogramms auf www.rozov.de einträgt
 */
class optionRekursionListener implements ItemListener {
	
	/**
	 * Die GUI
	 */
	private NonoGUI nonoGUI;
	
	/**
	 * Die Checkbox, die den Status der Option anzeigt
	 */
	private JCheckBox jCheckBox;
	
	/**
	 * Erzeugt einen neuen optionRekursionListener.
	 * 
	 * @param jCheckBox
	 * 		Die Checkbox, die den Status der Option "Rekursionsverfahren" anzeigt
	 * @param nonoGUI
	 * 		Die GUI
	 */
	optionRekursionListener(final NonoGUI nonoGUI, final JCheckBox jCheckBox) {
		this.nonoGUI = nonoGUI;
		this.jCheckBox = jCheckBox;
	}

	/**
	 * Wird ausgelöst, wenn die Option aktiviert oder deaktiviert wird.
	 */
	public void itemStateChanged(ItemEvent arg0) {
		if (this.nonoGUI.solver != null)
			this.nonoGUI.solver.setOptionRekursion(this.jCheckBox.isSelected());
	}
}