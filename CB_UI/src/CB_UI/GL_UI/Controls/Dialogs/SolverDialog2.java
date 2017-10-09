package CB_UI.GL_UI.Controls.Dialogs;

import java.util.TreeMap;

import com.badlogic.gdx.graphics.Color;

import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Function;
import CB_Core.Solver.Solver;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.SelectSolverFunction;
import CB_UI.GL_UI.Activitys.SelectSolverFunction.IFunctionResult;
import CB_UI.GL_UI.Controls.CoordinateButton;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.TextFieldListener;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.ChkBox;
import CB_UI_Base.GL_UI.Controls.ChkBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.HSV_Color;

public class SolverDialog2 extends ActivityBase implements OnStateChangeListener {
	private enum pages {
		Nothing, Text, Zahl, Function, Variable, Operator, Waypoint, Coordinate
	}

	private enum buttons {
		Text(pages.Text, "TXT"), Zahl(pages.Zahl, "123"), Function(pages.Function, "f(x)"), Variable(pages.Variable, "@"), Operator(pages.Operator, "+-*/"), Waypoint(pages.Waypoint, "$GC"), Coordinate(pages.Coordinate, "°");
		private pages page;
		public String description;

		private buttons(pages page, String description) {
			this.page = page;
			this.description = description;
		}

		public boolean hasDataType(DataType dataType) {
			if (dataType == DataType.None)
				return true; // wenn kein spezieller DataType angegeben ist -> alle Pages anzeigen
			if (dataType == DataType.String)
				return true; // alles kann als String zurück gegeben werden
			switch (page) {
			case Coordinate:
				return dataType == DataType.Coordinate;
			case Function:
				if (dataType != DataType.Waypoint) {
					return true;
				}
				break;
			case Nothing:
				if (dataType != DataType.Waypoint) {
					return true;
				}
				break;
			case Operator:
				return (dataType == DataType.Integer) || (dataType == DataType.Float);
			case Text:
				if (dataType != DataType.Waypoint) {
					return true;
				}
				break;
			case Variable:
				if (dataType != DataType.Waypoint) {
					return true;
				}
				break;
			case Waypoint:
				return (dataType == DataType.Coordinate) || (dataType == DataType.Waypoint);
			case Zahl:
				return (dataType == DataType.Integer) || (dataType == DataType.Float);
			default:
				break;

			}
			return false;
		}
	}

	//private final SolverDialog2BuildFormula buildFormula;
	private final TreeMap<buttons, MultiToggleButton> visibleButtons = new TreeMap<buttons, MultiToggleButton>();
	private float visibleButtonsHeight = 0;
	private ISolverBackStringListener mBackStringListener;
	private final Cache aktCache;
	private final String solverString;
	private final DataType dataType; // DataType of Parameter which must be returned
	private String sVar;
	private String sForm;
	private pages page;
	private final ScrollBox scrollBox;
	private Button bOK, bCancel;
	private Label lblTitle;
	private float innerLeft;
	private EditTextField mVariableField;
	private Button bVariableWaypoint;
	private Label lblGleich;
	// Page Text
	private EditTextField mFormulaField;
	private ChkBox cbFormulaAsText;
	private Label lFormulaAsText;
	// Page Zahl
	private final String[] lZahl = new String[] { "0", ",", "<-", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	private EditTextField tbZahl;
	private Button[] bZahl;
	// Page Function
	private EditTextField tbFunction;
	private Button bFunction;
	private EditTextField[] tbFunctionParam = null;
	private Label[] lFunctionParam = null;
	private Button[] bFunctionParam = null;
	// Page Variables
	private ChkBox[] cbVariables = null;
	private Label[] lVariables = null;
	// Page Operator
	private EditTextField[] tbOperator = null;
	private Button[] bOperator = null;
	// Page Waypoint
	private ChkBox[] cbWaypoints = null;
	private Label[] lWaypoints = null;
	// Page Coordinate
	private CoordinateButton bCoord = null;
	private final Solver solver; // Solver Object dieses Caches für die Functions, Variablen...

	public interface ISolverBackStringListener {
		public void BackString(String backString);
	}

	public SolverDialog2(Cache aktCache, Solver solver, String solverString, boolean showVariableField, DataType dataType) {
		super(ActivityRec(), "solverActivity");
		this.solver = solver;
		//this.buildFormula = null;
		this.solverString = solverString;
		this.aktCache = aktCache;
		this.dataType = dataType;
		// Split Solver String by =
		String[] solverStrings = solverString.split("=", 2);
		sVar = "";
		sForm = "";
		if (solverStrings.length == 1) {
			sForm = solverStrings[0];
		} else if (solverStrings.length > 1) {
			sVar = solverStrings[0];
			sForm = solverStrings[1];
		}

		page = pages.Nothing;
		scrollBox = new ScrollBox(ActivityRec());
		this.addChild(scrollBox);
		createOkCancelBtn();
		createTitleLine();
		scrollBox.setHeight(lblTitle.getY() - bOK.getMaxY() - margin - margin);
		scrollBox.setY(bOK.getMaxY() + margin);
		scrollBox.setBackground(this.getBackground());

		if (showVariableField) {
			createVariableLines();
		}
		createButtonsLine();

		Layout();

		// removed: now the page Number should be shown when the right side of a formula is empty but only if a left side is available
		// if (sForm.length() > 0)
		// {
		// only show special page if sForm is not empty

		if (isFunction(sForm)) {
			showPage(pages.Function);
		} else if (isCoordinate(sForm)) {
			showPage(pages.Coordinate);
		} else if (isNumber(sForm)) {
			showPage(pages.Zahl);
		} else if (isWaypoint(sForm)) {
			showPage(pages.Waypoint);
		} else if (isVariable(sForm)) {
			showPage(pages.Variable);
		} else if (dataType == DataType.Waypoint) {
			showPage(pages.Waypoint);
		} else {
			showPage(pages.Text);
		}
		// }
		// else
		// {
		// showPage(pages.Text);
		// }
	}

	private boolean isVariable(String solverString2) {
		for (String var : solver.Variablen.keySet()) {
			if (solverString2.equalsIgnoreCase(var)) {
				return true;
			}
		}
		return false;
	}

	private boolean isWaypoint(String string) {
		if (string.equals("&" + aktCache.getGcCode()))
			return true;
		for (int i = 0; i < aktCache.waypoints.size(); i++) {
			Waypoint waypoint = aktCache.waypoints.get(i);
			if (this.solverString.equals("$" + waypoint.getGcCode()))
				return true;
		}
		return false;
	}

	private boolean isNumber(String string) {
		if (this.solverString.length() == 0)
			return false;
		return isZahl(string);
	}

	private boolean isCoordinate(String string) {
		Coordinate coord = new Coordinate(string);
		return coord.isValid();
	}

	private boolean isFunction(String string) {
		// Funktion aufsplitten nach Funktionsname und Parameter (falls möglich!)
		String formula = string;
		formula.trim();
		int posKlammerAuf = formula.indexOf("(");
		int posKlammerZu = formula.lastIndexOf(")");
		if (posKlammerAuf <= 0)
			return false;
		if (posKlammerZu < posKlammerAuf)
			return false;
		if (posKlammerZu != formula.length() - 1)
			return false;
		// in eine gültigen Formel dürfen nur normale Buchstaben oder Zahlen stehen
		for (int i = 0; i < posKlammerAuf; i++) {
			char c = formula.charAt(i);
			if (!Character.isLetter(c) && !Character.isDigit(c))
				return false;
		}
		// gültige Formel erkannt anhand dem Format.
		return true;
	}

	public void show(ISolverBackStringListener listener) {
		mBackStringListener = listener;

		show();
	}

	private void createVariableLines() {
		innerLeft = margin;

		mVariableField = new EditTextField(this, this.name + " mVariableField");
		mVariableField.setX(innerLeft);
		mVariableField.setY(innerHeight - mVariableField.getHeight());
		mVariableField.setWidth(innerWidth - mVariableField.getHeight());
		scrollBox.addChild(mVariableField);
		mVariableField.setText(sVar);
		bVariableWaypoint = new Button("$GC");
		bVariableWaypoint.setX(innerLeft + innerWidth - mVariableField.getHeight());
		bVariableWaypoint.setY(innerHeight - mVariableField.getHeight());
		bVariableWaypoint.setWidth(mVariableField.getHeight());
		scrollBox.addChild(bVariableWaypoint);
		bVariableWaypoint.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button) {
				//String param = "";
				DataType type = DataType.Waypoint;
				SolverDialog2 sd2 = new SolverDialog2(aktCache, solver, mVariableField.getText(), false, type);
				sd2.show(new ISolverBackStringListener() {
					@Override
					public void BackString(String backString) {
						mVariableField.setText(backString);
					}
				});
				return true;
			}
		});

		lblGleich = new Label("=");
		lblGleich.setWidth(innerWidth);
		lblGleich.setX(innerLeft);
		lblGleich.setHAlignment(HAlignment.CENTER);
		scrollBox.addChild(lblGleich);
	}

	private void createButtonsLine() {
		innerLeft = margin;

		visibleButtons.clear();
		// Buttons zur Auswahl des Dialog-Typs
		// nur die Buttons werden angezeigt, die auch den gewünschten DataType liefern können
		for (buttons btn : buttons.values()) {
			if (btn == buttons.Operator)
				continue; // Operator erstmal noch nicht anzeigen
			if ((dataType == null) || btn.hasDataType(dataType)) {
				MultiToggleButton button = new MultiToggleButton(0, 0, 0, UI_Size_Base.that.getButtonHeight(), btn.description);
				button.addState(btn.description, new HSV_Color(Color.GRAY));
				button.addState(btn.description, new HSV_Color(Color.GREEN));
				button.setOnStateChangedListener(this);
				visibleButtons.put(btn, button);
				scrollBox.addChild(button);
				visibleButtonsHeight = button.getHeight();
			}
		}
		float w = innerWidth / visibleButtons.size();
		float x = 0;
		for (MultiToggleButton btn : visibleButtons.values()) {
			btn.setX(x);
			btn.setWidth(w);
			x += w;
		}
	}

	private void Layout() {
		float y = margin + innerHeight - visibleButtonsHeight * 4;

		switch (page) {
		case Function:
			if ((lFunctionParam != null) && (tbFunctionParam != null) && (bFunctionParam != null)) {
				for (int i = lFunctionParam.length - 1; i >= 0; i--) {
					Label l = lFunctionParam[i];
					EditTextField tb = tbFunctionParam[i];
					Button b = bFunctionParam[i];
					l.setY(y);
					l.setWidth((float) (innerWidth * 0.3));
					tb.setY(y);
					tb.setX(l.getX() + l.getWidth());
					tb.setWidth((float) (innerWidth * 0.6));
					b.setY(y);
					b.setX(tb.getX() + tb.getWidth());
					b.setWidth((float) (innerWidth * 0.1));
					y += tb.getHeight() + margin;
				}
			}
			tbFunction.setY(y);
			bFunction.setY(y);
			tbFunction.setWidth((float) (innerWidth * 0.8));
			bFunction.setWidth((float) (innerWidth * 0.2));
			bFunction.setX(tbFunction.getX() + tbFunction.getWidth());
			y += bFunction.getHeight() + margin;
			break;
		case Nothing:
			break;
		case Operator:
			tbOperator[0].setY(y);
			tbOperator[0].setWidth((float) (innerWidth * 0.9));
			bOperator[0].setY(y);
			bOperator[0].setX(tbOperator[0].getX() + tbOperator[0].getWidth());
			bOperator[0].setWidth((float) (innerWidth * 0.1));
			y += tbOperator[0].getHeight();
			tbOperator[1].setY(y);
			tbOperator[1].setWidth((float) (innerWidth * 0.9));
			bOperator[1].setY(y);
			bOperator[1].setX(tbOperator[1].getX() + tbOperator[1].getWidth());
			bOperator[1].setWidth((float) (innerWidth * 0.1));
			y += tbOperator[1].getHeight();
			break;
		case Text:
			// layout missing Variables
			// if (buildFormula != null)
			// {
			// y = buildFormula.Layout(y, innerLeft, innerWidth, margin);
			// }
			cbFormulaAsText.setY(y);
			lFormulaAsText.setY(y);
			lFormulaAsText.setX(cbFormulaAsText.getX() + cbFormulaAsText.getWidth());
			lFormulaAsText.setWidth(innerWidth - cbFormulaAsText.getWidth());
			y += cbFormulaAsText.getHeight() + margin;
			mFormulaField.setY(y);
			y += mFormulaField.getHeight() + margin;
			break;
		case Zahl:
			// 10er Tastatur
			float width = innerWidth / 3;
			for (int row = 0; row < 4; row++) {
				for (int col = 0; col < 3; col++) {
					int i = row * 3 + col;
					bZahl[i].setY(y);
					bZahl[i].setWidth(width);
					bZahl[i].setX(width * col);
				}
				y += bZahl[0].getHeight() + margin;
			}
			tbZahl.setY(y);
			tbZahl.setWidth(innerWidth);
			y += tbZahl.getHeight() + margin;
			break;
		case Variable:
			for (int i = cbVariables.length - 1; i >= 0; i--) {
				ChkBox cb = cbVariables[i];
				Label l = lVariables[i];
				cb.setY(y);
				cb.setWidth(cb.getHeight());
				l.setY(y);
				l.setX(cb.getX() + cb.getWidth());
				l.setWidth(innerHeight - cb.getWidth());
				y += l.getHeight();
			}
			break;
		case Waypoint:
			for (int i = cbWaypoints.length - 1; i >= 0; i--) {
				if ((dataType == DataType.Waypoint) && (i == 0)) {
					continue;
				}
				ChkBox cb = cbWaypoints[i];
				Label l = lWaypoints[i];
				cb.setY(y);
				cb.setWidth(cb.getHeight());
				l.setY(y);
				l.setX(cb.getX() + cb.getWidth());
				l.setWidth(innerHeight - cb.getWidth());
				y += l.getHeight();
			}
			break;
		case Coordinate:
			bCoord.setY(y);
			bCoord.setWidth(innerWidth);
			y += bCoord.getHeight() + margin;
			break;
		default:
			break;
		}

		for (MultiToggleButton mtb : visibleButtons.values()) {
			mtb.setY(y);
		}
		y += visibleButtonsHeight + margin;

		// tbGesamt.setY(y);
		// y += tbGesamt.getHeight();

		if (lblGleich != null) {
			lblGleich.setY(y - lblGleich.getHalfHeight() / 2);
			y += lblGleich.getHeight() - lblGleich.getHalfHeight();
		}
		if (bVariableWaypoint != null) {
			bVariableWaypoint.setY(y);
		}
		if (mVariableField != null) {
			mVariableField.setY(y);
			y += mVariableField.getHeight() + margin;
		}

		scrollBox.setVirtualHeight(y);

	}

	private void createOkCancelBtn() {
		bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(Translation.Get("ok"));
		bCancel.setText(Translation.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				// damit die Änderungen in sForm gespeichert werden
				saveAktPage();
				String result = "";
				if (mVariableField != null) {
					result = mVariableField.getText();
					if (result.length() > 0)
						result += "=";
				}
				result += sForm;
				finish();
				if (mBackStringListener != null){
					final String finalResult = result;
					Thread thread=new Thread(new Runnable() {
						@Override
						public void run() {
							mBackStringListener.BackString(finalResult);
						}
					});
					thread.start();
				}
				return true;
			}
		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				finish();
				return true;
			}
		});

	}

	private void createTitleLine() {
		// Title+Progressbar

		float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

		lblTitle = new Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
		lblTitle.setFont(Fonts.getBig());
		lblTitle.setText(Translation.Get("solver_formula")).getTextWidth();
		this.addChild(lblTitle);

	}

	@Override
	public void onStateChange(GL_View_Base v, int State) {

		pages newPage = null;
		// Statusänderung eines MultiToggleButtons
		if (State == 1) {
			// Werte der aktuellen Seite in den String sForm speichern
			saveAktPage();

			if (v == visibleButtons.get(buttons.Text)) {
				newPage = pages.Text;
			}
			if (v == visibleButtons.get(buttons.Zahl)) {
				if (isZahl(sForm)) {
					newPage = pages.Zahl;
				}
			}
			if (v == visibleButtons.get(buttons.Function)) {
				if ((sForm.length() == 0) || (isFunction(sForm))) {
					newPage = pages.Function;
				}
			}
			if (v == visibleButtons.get(buttons.Variable)) {
				if ((sForm.length() == 0) || isVariable(sForm)) {
					newPage = pages.Variable;
				}
			}
			if (v == visibleButtons.get(buttons.Operator)) {
				newPage = pages.Operator;
			}
			if (v == visibleButtons.get(buttons.Waypoint)) {
				if ((sForm.length() == 0) || (isWaypoint(sForm))) {
					newPage = pages.Waypoint;
				}
			}
			if (v == visibleButtons.get(buttons.Coordinate)) {
				if ((sForm.length() == 0) || (isCoordinate(sForm))) {
					newPage = pages.Coordinate;
				}
			}

		}
		if (newPage != null) {
			if (checkDataType(newPage)) {
				showPage(newPage);
			} else {
				// Button States zurück setzen
				setButtonStates();
			}
		}
		// check states of visibleButtons
		setButtonStates();
	}

	// Werte der aktuellen Seite in den String sForm speichern
	private void saveAktPage() {
		switch (this.page) {
		case Coordinate:
			savePageCoordinate();
			break;
		case Function:
			savePageFunction();
			break;
		case Nothing:
			break;
		case Operator:
			// savePageOperator();
			break;
		case Text:
			savePageText();
			break;
		case Variable:
			savePageVariable();
			break;
		case Waypoint:
			savePageWaypoint();
			break;
		case Zahl:
			savePageZahl();
			break;
		default:
			break;

		}
	}

	// überprüft für alle pages, ob der aktuell eingegebene String einen gültigen Wert für diese Page darstellt
	/*
	private void checkDataTypes() {
	for (pages p : pages.values()) {
	    checkDataType(p);
	}
	}
	*/

	private boolean checkDataType(pages p) {
		boolean valid = true;
		if (sForm.length() == 0)
			return true;
		switch (p) {
		case Coordinate:
			Coordinate c = new CoordinateGPS(sForm);
			valid = (c != null) && (c.isValid());
			break;
		case Function:
			break;
		case Nothing:
			break;
		case Operator:
			break;
		case Text:
			break;
		case Variable:
			break;
		case Waypoint:
			if (sForm.charAt(0) != '$')
				valid = false;
			break;
		case Zahl:
			if (!isZahl(sForm))
				valid = false;
			break;
		default:
			break;

		}
		return valid;

	}

	private void showPage(pages page) {
		if (page == this.page)
			return;

		// remove old controls
		switch (this.page) {
		case Text:
			hidePageText();
			break;
		case Zahl:
			hidePageZahl();
			break;
		case Function:
			hidePageFunction();
			break;
		case Variable:
			hidePageVariable();
			break;
		case Operator:
			hidePageOperator();
			break;
		case Waypoint:
			hidePageWaypoint();
			break;
		case Coordinate:
			hidePageCoordinate();
			break;
		default:
			break;
		}

		this.page = page;
		// set State of buttons
		setButtonStates();

		switch (page) {
		case Text:
			showPageText();
			break;
		case Zahl:
			showPageZahl();
			break;
		case Function:
			showPageFunction();
			break;
		case Variable:
			showPageVariable();
			break;
		case Operator:
			showPageOperator();
			break;
		case Waypoint:
			showPageWaypoint();
			break;
		case Coordinate:
			showPageCoordinate();
			break;
		default:
			break;
		}
		Layout();
	}

	private void setButtonStates() {
		if (visibleButtons.get(buttons.Text) != null)
			visibleButtons.get(buttons.Text).setState(page == pages.Text ? 1 : 0);
		if (visibleButtons.get(buttons.Zahl) != null)
			visibleButtons.get(buttons.Zahl).setState(page == pages.Zahl ? 1 : 0);
		if (visibleButtons.get(buttons.Function) != null)
			visibleButtons.get(buttons.Function).setState(page == pages.Function ? 1 : 0);
		if (visibleButtons.get(buttons.Variable) != null)
			visibleButtons.get(buttons.Variable).setState(page == pages.Variable ? 1 : 0);
		if (visibleButtons.get(buttons.Operator) != null)
			visibleButtons.get(buttons.Operator).setState(page == pages.Operator ? 1 : 0);
		if (visibleButtons.get(buttons.Waypoint) != null)
			visibleButtons.get(buttons.Waypoint).setState(page == pages.Waypoint ? 1 : 0);
		if (visibleButtons.get(buttons.Coordinate) != null)
			visibleButtons.get(buttons.Coordinate).setState(page == pages.Coordinate ? 1 : 0);
	}

	private void hidePageText() {
		// if (buildFormula != null)
		// {
		// buildFormula.removeChilds(scrollBox);
		// }
		scrollBox.removeChild(mFormulaField);
		scrollBox.removeChild(cbFormulaAsText);
		scrollBox.removeChild(lFormulaAsText);
		mFormulaField = null;
		cbFormulaAsText = null;
		lFormulaAsText = null;
	}

	private void showPageText() {
		// buildFormula = new SolverDialog2BuildFormula(sForm);

		String text = sForm;
		boolean asText = false;
		if (text.length() >= 2) {
			if ((text.charAt(0) == '"') && (text.charAt(text.length() - 1) == '"')) {
				if (text.indexOf("\"", 1) == text.length() - 1) {
					text = text.substring(1, text.length() - 1);
					asText = true;
				}
			}
		}

		mFormulaField = new EditTextField(this, this.name + " mFormulaField");
		mFormulaField.setWrapType(WrapType.SINGLELINE);
		mFormulaField.setX(innerLeft);
		mFormulaField.setWidth(innerWidth);
		mFormulaField.setText(text);
		mFormulaField.setZeroPos();
		mFormulaField.setTextFieldListener(new TextFieldListener() {
			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight) {
			}

			@Override
			public void keyTyped(EditTextFieldBase textField, char key) {
			}
		});
		cbFormulaAsText = new ChkBox("AsText");
		// cbFormulaAsText.setText("Als Text in \"\" eintragen");
		cbFormulaAsText.setChecked(asText);
		lFormulaAsText = new Label("Als Text in \"\" eintragen");
		Solver solv = new Solver(sForm);
		if (solv.Solve()) {
			if (solv.MissingVariables != null) {
				for (String mv : solv.MissingVariables.keySet()) {
					System.out.println(mv);
				}
			}
		}

		scrollBox.addChild(mFormulaField);
		scrollBox.addChild(cbFormulaAsText);
		scrollBox.addChild(lFormulaAsText);
		// buildFormula.addControls(scrollBox);
	}

	private void savePageText() {
		if (mFormulaField != null) {
			sForm = mFormulaField.getText();
		}
		if (cbFormulaAsText != null) {
			if (cbFormulaAsText.isChecked()) {
				sForm = "\"" + sForm + "\"";
			}
		}
	}

	private void showPageZahl() {
		tbZahl = new EditTextField(this.name + " tbZahl");
		tbZahl.dontShowSoftKeyBoardOnFocus(true);
		tbZahl.setText(sForm);
		scrollBox.addChild(tbZahl);
		bZahl = new Button[12];
		for (int i = 0; i < 12; i++) {
			bZahl[i] = new Button(lZahl[i]);
			bZahl[i].setData(i);
			scrollBox.addChild(bZahl[i]);
			if ((i == 1) && (dataType == DataType.Integer)) {
				// Integer erwartet -> kein Komma anzeigen
				bZahl[i].setVisible(false);
			}
			bZahl[i].setOnClickListener(new OnClickListener() {
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
					Integer i = (Integer) v.getData();
					if (i != null) {
						if (i == 2) {
							// Delete
							String text = tbZahl.getText();
							if (text.length() > 1) {
								text = text.substring(0, text.length() - 1);
							} else if (text.length() == 1) {
								text = "";
							}
							// Resultierende Zahl überprüfen
							if (isZahl(text)) {
								tbZahl.setText(text);
							}
						} else {
							String text = tbZahl.getText() + lZahl[i];
							if (isZahl(text)) {
								tbZahl.setText(text);
							}
						}
					}
					return true;
				}
			});
		}
	}

	private boolean isZahl(String text) {
		if (text.length() == 0)
			return true;
		char sep = '.';
		text = text.replace('.', sep);
		text = text.replace(',', sep);
		try {
			@SuppressWarnings("unused")
			double zahl = Double.valueOf(text);
		} catch (Exception ex) {
			// Fehler, Zahl konnte nicht interpretiert werden -> keine Änderung!!!
			return false;
		}
		return true;
	}

	private void hidePageZahl() {
		scrollBox.removeChild(tbZahl);
		tbZahl = null;
		for (int i = 0; i < 12; i++) {
			scrollBox.removeChild(bZahl[i]);
		}
		bZahl = null;
	}

	private void savePageZahl() {
		if (tbZahl != null) {
			sForm = tbZahl.getText();
		}
	}

	private void showPageFunction() {

		tbFunction = new EditTextField(this, this.name + " tbFunction");
		tbFunction.setText(sForm);
		scrollBox.addChild(tbFunction);

		bFunction = new Button("");
		bFunction.setText("F(x)");
		scrollBox.addChild(bFunction);

		// Funktion aufsplitten nach Funktionsname und Parameter (falls möglich!)
		String formula = sForm.trim();
		int posKlammerAuf = formula.indexOf("(");
		int posKlammerZu = formula.lastIndexOf(")");
		if ((posKlammerAuf >= 0) && (posKlammerZu > posKlammerAuf)) {
			// gültige Formel erkannt
			String function = formula.substring(0, posKlammerAuf);
			tbFunction.setText(function);
			String parameter = formula.substring(posKlammerAuf + 1, posKlammerZu);
			// Parameter nach ";" trennen
			String[] parameters = parameter.split(";");

			tbFunctionParam = new EditTextField[parameters.length];
			lFunctionParam = new Label[parameters.length];
			bFunctionParam = new Button[parameters.length];
			for (int i = 0; i < parameters.length; i++) {
				addFunctionParamLine(function, i, parameters[i].trim());
			}
		}
		bFunction.setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				// Funktionsauswahl zeigen
				SelectSolverFunction ssf = new SelectSolverFunction(solver, dataType, new IFunctionResult() {
					@Override
					public void selectedFunction(Function function) {
						if (function == null)
							return;
						tbFunction.setText(function.getLongLocalName());
						// evtl. vorhandene Parameter-Eingaben entfernen
						removeFunctionParam();
						tbFunctionParam = new EditTextField[function.getAnzParam()];
						lFunctionParam = new Label[function.getAnzParam()];
						bFunctionParam = new Button[function.getAnzParam()];
						for (int i = 0; i < function.getAnzParam(); i++) {
							addFunctionParamLine(function, i, "");
						}
						Layout();
					}
				});
				GL.that.showDialog(ssf);
				return true;
			}
		});
	}

	private void addFunctionParamLine(final Function function, int i, String string) {
		String paramName = "Parameter " + i;
		if (function != null) {
			paramName = Translation.Get(function.getParamName(i));
		}
		// Eingabefelder für die Parameter einfügen
		lFunctionParam[i] = new Label();
		lFunctionParam[i].setText(paramName);
		scrollBox.addChild(lFunctionParam[i]);

		tbFunctionParam[i] = new EditTextField(SolverDialog2.this, this.name + " tbFunctionParam[" + i + "]");
		tbFunctionParam[i].setText(string);
		scrollBox.addChild(tbFunctionParam[i]);

		bFunctionParam[i] = new Button("..");
		scrollBox.addChild(bFunctionParam[i]);
		bFunctionParam[i].setData(i);
		bFunctionParam[i].setOnClickListener(new OnClickListener() {
			@Override
			public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button) {
				String param = "";
				DataType type = DataType.None;
				Integer i = (Integer) v.getData();
				if (i != null) {
					param = tbFunctionParam[i].getText();
					if (function != null) {
						type = function.getParamType(i);
					}
				}
				SolverDialog2 sd2 = new SolverDialog2(aktCache, solver, param, false, type);
				sd2.show(new ISolverBackStringListener() {
					@Override
					public void BackString(String backString) {
						Integer i = (Integer) v.getData();
						if (i != null) {
							tbFunctionParam[i].setText(backString);
						}
					}
				});
				return true;
			}
		});
	}

	/*
	private DataType getFunctionDataType(String functionName) {
	Function function = solver.functions.getFunction(functionName);
	if (function == null) {
	    return DataType.None;
	} else {
	    return function.getReturnType();
	}
	}
	*/

	private void addFunctionParamLine(String functionString, int i, String string) {
		Function function = solver.functions.getFunction(functionString);
		addFunctionParamLine(function, i, string);
	}

	private void hidePageFunction() {
		// Parameter entfernen
		removeFunctionParam();
		scrollBox.removeChild(tbFunction);
		scrollBox.removeChild(bFunction);
		tbFunction = null;
		bFunction = null;
	}

	private void savePageFunction() {
		if (tbFunction == null) {
			return;
		}
		// geänderte Formel merken
		sForm = tbFunction.getText();
		if (sForm.length() > 0) {
			sForm += "(";
			if (tbFunctionParam != null) {
				for (int i = 0; i < tbFunctionParam.length; i++) {
					if (i > 0)
						sForm += "; ";
					sForm += tbFunctionParam[i].getText();
				}
			}
			sForm += ")";
		}
	}

	private void removeFunctionParam() {
		if (tbFunctionParam != null) {
			for (int i = 0; i < tbFunctionParam.length; i++) {
				scrollBox.removeChild(tbFunctionParam[i]);
			}
			tbFunctionParam = null;
		}
		if (lFunctionParam != null) {
			for (int i = 0; i < lFunctionParam.length; i++) {
				scrollBox.removeChild(lFunctionParam[i]);
			}
			lFunctionParam = null;
		}
		if (bFunctionParam != null) {
			for (int i = 0; i < bFunctionParam.length; i++) {
				scrollBox.removeChild(bFunctionParam[i]);
			}
			bFunctionParam = null;
		}
	}

	boolean doNotChangeCBVariable = false;

	private void showPageVariable() {
		cbVariables = new ChkBox[solver.Variablen.size()];
		lVariables = new Label[solver.Variablen.size()];
		int i = 0;
		for (String variable : solver.Variablen.keySet()) {
			String value = solver.Variablen.get(variable);
			cbVariables[i] = new ChkBox(variable);
			cbVariables[i].setData(variable);
			scrollBox.addChild(cbVariables[i]);
			lVariables[i] = new Label(variable + " (" + value + ")");
			scrollBox.addChild(lVariables[i]);
			cbVariables[i].setChecked(sForm.equalsIgnoreCase(variable));
			cbVariables[i].setOnCheckChangedListener(new OnCheckChangedListener() {
				@Override
				public void onCheckedChanged(ChkBox view, boolean isChecked) {
					if (doNotChangeCBVariable)
						return;
					doNotChangeCBVariable = true;
					for (ChkBox cb : cbVariables) {
						cb.setChecked(cb == view);
					}
					doNotChangeCBVariable = false;
				}
			});
			i++;
		}
	}

	private void hidePageVariable() {
		for (Label l : lVariables) {
			scrollBox.removeChild(l);
		}
		for (ChkBox cb : cbVariables) {
			scrollBox.removeChild(cb);
		}
		cbVariables = null;
		lVariables = null;
	}

	private void savePageVariable() {
		if (cbVariables == null)
			return;
		for (ChkBox cb : cbVariables) {
			if (cb.isChecked()) {
				String variable = (String) cb.getData();
				if (variable != null) {
					sForm = variable;
				}
			}
		}

	}

	private void showPageOperator() {
		tbOperator = new EditTextField[2];
		bOperator = new Button[2];
		for (int i = 0; i < 2; i++) {
			tbOperator[i] = new EditTextField(this.name + "tbOperator[" + i + "]");
			scrollBox.addChild(tbOperator[i]);
			bOperator[i] = new Button("..");
			scrollBox.addChild(bOperator[i]);
			bOperator[i].setData(tbOperator[i]);
			bOperator[i].setOnClickListener(oclSolverString);
		}
	}

	OnClickListener oclSolverString = new OnClickListener() {
		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			String param = "";
			final EditTextField tb = (EditTextField) v.getData();
			if (tb != null) {
				param = tb.getText();
			}
			SolverDialog2 sd2 = new SolverDialog2(aktCache, solver, param, false, DataType.Float);
			sd2.show(new ISolverBackStringListener() {
				@Override
				public void BackString(String backString) {
					if (tb != null) {
						tb.setText(backString);
					}
				}
			});
			return true;
		}
	};

	private void hidePageOperator() {
		sForm = tbOperator[0].getText() + "+" + tbOperator[1].getText();
		for (int i = 0; i < 2; i++) {
			scrollBox.removeChild(tbOperator[i]);
			scrollBox.removeChild(bOperator[i]);
		}
		tbOperator = null;
		bOperator = null;
	}

	private void showPageWaypoint() {
		cbWaypoints = new ChkBox[aktCache.waypoints.size() + 1];
		lWaypoints = new Label[aktCache.waypoints.size() + 1];

		for (int i = 0; i <= aktCache.waypoints.size(); i++) {
			if ((dataType == DataType.Waypoint) && (i == 0)) {
				continue;
			}
			Waypoint waypoint = null;
			String data = "";
			String description = "";
			if (i > 0) {
				waypoint = aktCache.waypoints.get(i - 1);
				data = "$" + waypoint.getGcCode();
				description = "$" + waypoint.getGcCode() + " - " + waypoint.getTitle();
			} else {
				data = "$" + aktCache.getGcCode();
				description = "$" + aktCache.getGcCode() + " - " + aktCache.getName();
			}

			cbWaypoints[i] = new ChkBox(data);
			cbWaypoints[i].setData(data);
			scrollBox.addChild(cbWaypoints[i]);
			lWaypoints[i] = new Label(description);
			scrollBox.addChild(lWaypoints[i]);
			cbWaypoints[i].setChecked(sForm.equalsIgnoreCase(data));

			cbWaypoints[i].setOnCheckChangedListener(new OnCheckChangedListener() {
				@Override
				public void onCheckedChanged(ChkBox view, boolean isChecked) {
					if (doNotChangeCBVariable)
						return;
					doNotChangeCBVariable = true;
					for (ChkBox cb : cbWaypoints) {
						cb.setChecked(cb == view);
					}
					doNotChangeCBVariable = false;
				}
			});
		}
	}

	private void hidePageWaypoint() {
		if (cbWaypoints == null)
			return;
		for (ChkBox cb : cbWaypoints) {
			scrollBox.removeChild(cb);
		}
		for (Label l : lWaypoints) {
			scrollBox.removeChild(l);
		}
		cbWaypoints = null;
		lWaypoints = null;
	}

	private void savePageWaypoint() {
		if (cbWaypoints == null)
			return;
		for (ChkBox cb : cbWaypoints) {
			if (cb == null)
				continue;
			// scrollBox.removeChild(cb);
			if (cb.isChecked()) {
				String waypoint = (String) cb.getData();
				if (waypoint != null) {
					sForm = waypoint;
				}
			}
		}
	}

	private void showPageCoordinate() {
		bCoord = new CoordinateButton("Coordinate");
		bCoord.setCoordinate(new CoordinateGPS(sForm));
		scrollBox.addChild(bCoord);
	}

	private void hidePageCoordinate() {
		scrollBox.removeChild(bCoord);
		bCoord = null;
	}

	private void savePageCoordinate() {
		if (bCoord == null)
			return;
		if (bCoord.getCoordinate().isValid()) {
			sForm = "\"" + bCoord.getCoordinate().FormatCoordinate() + "\"";
		}
	}
}
