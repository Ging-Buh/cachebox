package CB_UI.GL_UI.Controls.Dialogs;

import java.util.TreeMap;

import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Core.Solver.Functions.Function;
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
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.chkBox;
import CB_UI_Base.GL_UI.Controls.chkBox.OnCheckedChangeListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class SolverDialog2 extends ActivityBase implements OnStateChangeListener
{
	private enum pages
	{
		Nothing, Text, Zahl, Function, Variable, Operator, Waypoint, Coordinate
	}

	private enum buttons
	{
		Text(pages.Text, "TXT"), Zahl(pages.Zahl, "123"), Function(pages.Function, "f(x)"), Variable(pages.Variable, "@"), Operator(
				pages.Operator,
				"+-*/"), Waypoint(pages.Waypoint, "$GC"), Coordinate(pages.Coordinate, "�");
		private pages page;
		public String description;

		private buttons(pages page, String description)
		{
			this.page = page;
			this.description = description;
		}

		public boolean hasDataType(DataType dataType)
		{
			if (dataType == DataType.None) return true; // wenn kein spezieller DataType angegeben ist -> alle Pages anzeigen
			if (dataType == DataType.String) return true; // alles kann als String zur�ck gegeben werden
			switch (page)
			{
			case Coordinate:
				return dataType == DataType.Coordinate;
			case Function:
				return true;
			case Nothing:
				return true;
			case Operator:
				return (dataType == DataType.Integer) || (dataType == DataType.Float);
			case Text:
				return true;
			case Variable:
				return true;
			case Waypoint:
				return dataType == DataType.Coordinate;
			case Zahl:
				return (dataType == DataType.Integer) || (dataType == dataType.Float);
			default:
				break;

			}
			return true;
		}
	}

	private SolverDialog2BuildFormula buildFormula;
	private TreeMap<buttons, MultiToggleButton> visibleButtons = new TreeMap<buttons, MultiToggleButton>();
	private SolverBackStringListner mBackStringListner;
	private Cache aktCache;
	private String solverString;
	private DataType dataType; // DataType of Parameter which must be returned
	private String sVar;
	private String sForm;
	private pages page;
	private ScrollBox scrollBox;
	private Button bOK, bCancel;
	private Label lblTitle;
	private float innerLeft;
	private EditTextField mVariableField;
	private Label lblGleich;
	private EditTextField tbGesamt;
	// Page Text
	private EditTextField mFormulaField;
	// Page Zahl
	private String[] lZahl = new String[]
		{ "0", ",", "<-", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	private EditTextField tbZahl;
	private Button[] bZahl;
	// Page Function
	private EditTextField tbFunction;
	private Button bFunction;
	private EditTextField[] tbFunctionParam = null;
	private Label[] lFunctionParam = null;
	private Button[] bFunctionParam = null;
	// Page Variables
	private chkBox[] cbVariables = null;
	private Label[] lVariables = null;
	// Page Operator
	private EditTextField[] tbOperator = null;
	private Button[] bOperator = null;
	// Page Waypoint
	private chkBox[] cbWaypoints = null;
	private Label[] lWaypoints = null;
	// Page Coordinate
	private CoordinateButton bCoord = null;

	public interface SolverBackStringListner
	{
		public void BackString(String backString);
	}

	public SolverDialog2(Cache aktCache, String solverString, boolean showVariableField, DataType dataType)
	{
		super(ActivityRec(), "solverActivity");
		this.buildFormula = null;
		this.solverString = solverString;
		this.aktCache = aktCache;
		this.dataType = dataType;
		// Split Solver String by =
		String[] solverStrings = solverString.split("=", 2);
		sVar = "";
		sForm = "";
		if (solverStrings.length == 1)
		{
			sForm = solverStrings[0];
		}
		else if (solverStrings.length > 1)
		{
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

		tbGesamt = new EditTextField();
		tbGesamt.setX(innerLeft);
		tbGesamt.setWidth(innerWidth);
		tbGesamt.setText(sForm);
		// scrollBox.addChild(tbGesamt);

		if (showVariableField)
		{
			createVariableLines();
		}
		createButtonsLine();

		initialForm();

		Layout();
		showPage(pages.Text);
	}

	public void show(SolverBackStringListner listner)
	{
		mBackStringListner = listner;

		show();
	}

	private void createVariableLines()
	{
		innerLeft = margin;

		mVariableField = new EditTextField(this);
		mVariableField.setX(innerLeft);
		mVariableField.setY(innerHeight - mVariableField.getHeight());
		mVariableField.setWidth(innerWidth);
		scrollBox.addChild(mVariableField);
		mVariableField.setText(sVar);

		lblGleich = new Label("=");
		lblGleich.setWidth(innerWidth);
		lblGleich.setX(innerLeft);
		lblGleich.setHAlignment(HAlignment.CENTER);
		scrollBox.addChild(lblGleich);
	}

	private void createButtonsLine()
	{
		innerLeft = margin;

		visibleButtons.clear();
		// Buttons zur Auswahl des Dialog-Typs
		// nur die Buttons werden angezeigt, die auch den gew�nschten DataType liefern k�nnen
		for (buttons btn : buttons.values())
		{
			if (btn == buttons.Operator) continue; // Operator erstmal noch nicht anzeigen
			if ((dataType == null) || btn.hasDataType(dataType))
			{
				MultiToggleButton button = new MultiToggleButton(0, 0, 0, UI_Size_Base.that.getButtonHeight(), btn.description);
				button.addState(btn.description, Color.GRAY);
				button.addState(btn.description, Color.GREEN);
				button.setOnStateChangedListner(this);
				visibleButtons.put(btn, button);
				scrollBox.addChild(button);
			}
		}
		float w = innerWidth / visibleButtons.size();
		float x = 0;
		for (MultiToggleButton btn : visibleButtons.values())
		{
			btn.setX(x);
			btn.setWidth(w);
			x += w;
		}
	}

	private void Layout()
	{
		float y = margin + innerHeight - visibleButtons.get(buttons.Text).getHeight() * 4;

		switch (page)
		{
		case Function:
			if ((lFunctionParam != null) && (tbFunctionParam != null) && (bFunctionParam != null))
			{
				for (int i = lFunctionParam.length - 1; i >= 0; i--)
				{
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
			if (buildFormula != null)
			{
				y = buildFormula.Layout(y, innerLeft, innerWidth, margin);
			}
			mFormulaField.setY(y);
			y += mFormulaField.getHeight() + margin;
			break;
		case Zahl:
			// 10er Tastatur
			float width = innerWidth / 3;
			for (int row = 0; row < 4; row++)
			{
				for (int col = 0; col < 3; col++)
				{
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
			for (int i = cbVariables.length - 1; i >= 0; i--)
			{
				chkBox cb = cbVariables[i];
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
			for (int i = cbWaypoints.length - 1; i >= 0; i--)
			{
				chkBox cb = cbWaypoints[i];
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

		for (MultiToggleButton mtb : visibleButtons.values())
		{
			mtb.setY(y);
		}
		y += visibleButtons.get(buttons.Text).getHeight() + margin;

		// tbGesamt.setY(y);
		// y += tbGesamt.getHeight();

		if (lblGleich != null)
		{
			lblGleich.setY(y - lblGleich.getHalfHeight() / 2);
			y += lblGleich.getHeight() - lblGleich.getHalfHeight();
		}
		if (mVariableField != null)
		{
			mVariableField.setY(y);
			y += mVariableField.getHeight() + margin;
		}

		scrollBox.setVirtualHeight(y);

	}

	private void initialForm()
	{
		// TODO Auto-generated method stub

	}

	private void createOkCancelBtn()
	{
		bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(Translation.Get("ok"));
		bCancel.setText(Translation.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// damit die �nderungen in sForm gespeichert werden
				saveAktPage();
				String result = "";
				if (mVariableField != null)
				{
					result = mVariableField.getText();
					if (result.length() > 0) result += "=";
				}
				result += sForm;
				finish();
				if (mBackStringListner != null) mBackStringListner.BackString(result);
				return true;
			}
		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				finish();
				return true;
			}
		});

	}

	private void createTitleLine()
	{
		// Title+Progressbar

		float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

		lblTitle = new Label(leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin,
				lineHeight, "TitleSolver");
		lblTitle.setFont(Fonts.getBig());
		lblTitle.setText(Translation.Get("solver_formula")).getTextWidth();
		this.addChild(lblTitle);

	}

	@Override
	public void onStateChange(GL_View_Base v, int State)
	{

		pages newPage = null;
		// Status�nderung eines MultiToggleButtons
		if (State == 1)
		{
			// Werte der aktuellen Seite in den String sForm speichern
			saveAktPage();

			if (v == visibleButtons.get(buttons.Text))
			{
				newPage = pages.Text;
			}
			if (v == visibleButtons.get(buttons.Zahl))
			{
				newPage = pages.Zahl;
			}
			if (v == visibleButtons.get(buttons.Function))
			{
				newPage = pages.Function;
			}
			if (v == visibleButtons.get(buttons.Variable))
			{
				newPage = pages.Variable;
			}
			if (v == visibleButtons.get(buttons.Operator))
			{
				newPage = pages.Operator;
			}
			if (v == visibleButtons.get(buttons.Waypoint))
			{
				newPage = pages.Waypoint;
			}
			if (v == visibleButtons.get(buttons.Coordinate))
			{
				newPage = pages.Coordinate;
			}
		}
		if (newPage != null)
		{
			if (checkDataType(newPage))
			{
				showPage(newPage);
			}
			else
			{
				// Button States zur�ck setzen
				setButtonStates();
			}
		}
	}

	// Werte der aktuellen Seite in den String sForm speichern
	private void saveAktPage()
	{
		switch (this.page)
		{
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

	// �berpr�ft f�r alle pages, ob der aktuell eingegebene String einen g�ltigen Wert f�r diese Page darstellt
	private void checkDataTypes()
	{
		for (pages p : pages.values())
		{
			checkDataType(p);
		}
	}

	private boolean checkDataType(pages p)
	{
		boolean valid = true;
		if (sForm.length() == 0) return true;
		switch (p)
		{
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
			if (sForm.charAt(0) != '$') valid = false;
			break;
		case Zahl:
			if (!isZahl(sForm)) valid = false;
			break;
		default:
			break;

		}
		return valid;

	}

	private void showPage(pages page)
	{
		if (page == this.page) return;

		// remove old controls
		switch (this.page)
		{
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

		switch (page)
		{
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

	private void setButtonStates()
	{
		if (visibleButtons.get(buttons.Text) != null) visibleButtons.get(buttons.Text).setState(page == pages.Text ? 1 : 0);
		if (visibleButtons.get(buttons.Zahl) != null) visibleButtons.get(buttons.Zahl).setState(page == pages.Zahl ? 1 : 0);
		if (visibleButtons.get(buttons.Function) != null) visibleButtons.get(buttons.Function).setState(page == pages.Function ? 1 : 0);
		if (visibleButtons.get(buttons.Variable) != null) visibleButtons.get(buttons.Variable).setState(page == pages.Variable ? 1 : 0);
		if (visibleButtons.get(buttons.Operator) != null) visibleButtons.get(buttons.Operator).setState(page == pages.Operator ? 1 : 0);
		if (visibleButtons.get(buttons.Waypoint) != null) visibleButtons.get(buttons.Waypoint).setState(page == pages.Waypoint ? 1 : 0);
		if (visibleButtons.get(buttons.Coordinate) != null) visibleButtons.get(buttons.Coordinate).setState(
				page == pages.Coordinate ? 1 : 0);
	}

	private void hidePageText()
	{
		if (buildFormula != null)
		{
			buildFormula.removeChilds(scrollBox);
		}
		scrollBox.removeChild(mFormulaField);
		mFormulaField = null;
	}

	private void showPageText()
	{
		buildFormula = new SolverDialog2BuildFormula(sForm);

		mFormulaField = new EditTextField(this);
		mFormulaField.setWrapType(WrapType.SINGLELINE);
		mFormulaField.setX(innerLeft);
		mFormulaField.setWidth(innerWidth);
		mFormulaField.setText(sForm);
		mFormulaField.setZeroPos();
		mFormulaField.setTextFieldListener(new TextFieldListener()
		{
			@Override
			public void lineCountChanged(EditTextFieldBase textField, int lineCount, float textHeight)
			{
			}

			@Override
			public void keyTyped(EditTextFieldBase textField, char key)
			{
			}
		});

		Solver solv = new Solver(sForm);
		if (solv.Solve())
		{
			if (solv.MissingVariables != null)
			{
				for (String mv : solv.MissingVariables.keySet())
				{
					System.out.println(mv);
				}
			}
		}

		scrollBox.addChild(mFormulaField);
		buildFormula.addControls(scrollBox);
	}

	private void savePageText()
	{
		if (mFormulaField != null)
		{
			sForm = mFormulaField.getText();
		}
	}

	private void showPageZahl()
	{
		tbZahl = new EditTextField();
		tbZahl.dontShowSoftKeyBoardOnFocus(true);
		tbZahl.setText(sForm);
		scrollBox.addChild(tbZahl);
		bZahl = new Button[12];
		for (int i = 0; i < 12; i++)
		{
			bZahl[i] = new Button(lZahl[i]);
			bZahl[i].setData(i);
			scrollBox.addChild(bZahl[i]);
			if ((i == 1) && (dataType == DataType.Integer))
			{
				// Integer erwartet -> kein Komma anzeigen
				bZahl[i].setVisible(false);
			}
			bZahl[i].setOnClickListener(new OnClickListener()
			{
				@Override
				public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
				{
					Integer i = (Integer) v.getData();
					if (i != null)
					{
						if (i == 2)
						{
							// Delete
							String text = tbZahl.getText();
							if (text.length() > 1)
							{
								text = text.substring(0, text.length() - 1);
							}
							else if (text.length() == 1)
							{
								text = "";
							}
							// Resultierende Zahl �berpr�fen
							if (isZahl(text))
							{
								tbZahl.setText(text);
							}
						}
						else
						{
							String text = tbZahl.getText() + lZahl[i];
							if (isZahl(text))
							{
								tbZahl.setText(text);
							}
						}
					}
					return true;
				}
			});
		}
	}

	private boolean isZahl(String text)
	{
		if (text.length() == 0) return true;
		char sep = '.';
		text = text.replace('.', sep);
		text = text.replace(',', sep);
		try
		{
			@SuppressWarnings("unused")
			double zahl = Double.valueOf(text);
		}
		catch (Exception ex)
		{
			// Fehler, Zahl konnte nicht interpretiert werden -> keine �nderung!!!
			return false;
		}
		return true;
	}

	private void hidePageZahl()
	{
		scrollBox.removeChild(tbZahl);
		tbZahl = null;
		for (int i = 0; i < 12; i++)
		{
			scrollBox.removeChild(bZahl[i]);
		}
		bZahl = null;
	}

	private void savePageZahl()
	{
		if (tbZahl != null)
		{
			sForm = tbZahl.getText();
		}
	}

	private void showPageFunction()
	{

		tbFunction = new EditTextField(this);
		tbFunction.setText(sForm);
		scrollBox.addChild(tbFunction);

		bFunction = new Button("");
		bFunction.setText("F(x)");
		scrollBox.addChild(bFunction);

		// Funktion aufsplitten nach Funktionsname und Parameter (falls m�glich!)
		String formula = sForm.trim();
		int posKlammerAuf = formula.indexOf("(");
		int posKlammerZu = formula.lastIndexOf(")");
		if ((posKlammerAuf >= 0) && (posKlammerZu > posKlammerAuf))
		{
			// g�ltige Formel erkannt
			String function = formula.substring(0, posKlammerAuf);
			tbFunction.setText(function);
			String parameter = formula.substring(posKlammerAuf + 1, posKlammerZu);
			// Parameter nach ";" trennen
			String[] parameters = parameter.split(";");

			tbFunctionParam = new EditTextField[parameters.length];
			lFunctionParam = new Label[parameters.length];
			bFunctionParam = new Button[parameters.length];
			for (int i = 0; i < parameters.length; i++)
			{
				addFunctionParamLine(function, i, parameters[i].trim());
			}
		}
		bFunction.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Funktionsauswahl zeigen
				SelectSolverFunction ssf = new SelectSolverFunction(dataType, new IFunctionResult()
				{
					@Override
					public void selectedFunction(Function function)
					{
						if (function == null) return;
						tbFunction.setText(function.getLongLocalName());
						// evtl. vorhandene Parameter-Eingaben entfernen
						removeFunctionParam();
						tbFunctionParam = new EditTextField[function.getAnzParam()];
						lFunctionParam = new Label[function.getAnzParam()];
						bFunctionParam = new Button[function.getAnzParam()];
						for (int i = 0; i < function.getAnzParam(); i++)
						{
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

	private void addFunctionParamLine(final Function function, int i, String string)
	{
		String paramName = "Parameter " + i;
		if (function != null)
		{
			paramName = Translation.Get(function.getParamName(i));
		}
		// Eingabefelder f�r die Parameter einf�gen
		lFunctionParam[i] = new Label();
		lFunctionParam[i].setText(paramName);
		scrollBox.addChild(lFunctionParam[i]);

		tbFunctionParam[i] = new EditTextField(SolverDialog2.this);
		tbFunctionParam[i].setText(string);
		scrollBox.addChild(tbFunctionParam[i]);

		bFunctionParam[i] = new Button("..");
		scrollBox.addChild(bFunctionParam[i]);
		bFunctionParam[i].setData(i);
		bFunctionParam[i].setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button)
			{
				String param = "";
				DataType type = DataType.None;
				Integer i = (Integer) v.getData();
				if (i != null)
				{
					param = tbFunctionParam[i].getText();
					if (function != null)
					{
						type = function.getParamType(i);
					}
				}
				SolverDialog2 sd2 = new SolverDialog2(aktCache, param, false, type);
				sd2.show(new SolverBackStringListner()
				{
					@Override
					public void BackString(String backString)
					{
						Integer i = (Integer) v.getData();
						if (i != null)
						{
							tbFunctionParam[i].setText(backString);
						}
					}
				});
				return true;
			}
		});
	}

	private DataType getFunctionDataType(String functionName)
	{
		Function function = Solver.functions.getFunction(functionName);
		if (function == null)
		{
			return DataType.None;
		}
		else
		{
			return function.getReturnType();
		}
	}

	private void addFunctionParamLine(String functionString, int i, String string)
	{
		Function function = Solver.functions.getFunction(functionString);
		addFunctionParamLine(function, i, string);
	}

	private void hidePageFunction()
	{
		// Parameter entfernen
		removeFunctionParam();
		scrollBox.removeChild(tbFunction);
		scrollBox.removeChild(bFunction);
		tbFunction = null;
		bFunction = null;
	}

	private void savePageFunction()
	{
		if (tbFunction == null)
		{
			return;
		}
		// ge�nderte Formel merken
		sForm = tbFunction.getText();
		if (sForm.length() > 0)
		{
			sForm += "(";
			if (tbFunctionParam != null)
			{
				for (int i = 0; i < tbFunctionParam.length; i++)
				{
					if (i > 0) sForm += "; ";
					sForm += tbFunctionParam[i].getText();
				}
			}
			sForm += ")";
		}
	}

	private void removeFunctionParam()
	{
		if (tbFunctionParam != null)
		{
			for (int i = 0; i < tbFunctionParam.length; i++)
			{
				scrollBox.removeChild(tbFunctionParam[i]);
			}
			tbFunctionParam = null;
		}
		if (lFunctionParam != null)
		{
			for (int i = 0; i < lFunctionParam.length; i++)
			{
				scrollBox.removeChild(lFunctionParam[i]);
			}
			lFunctionParam = null;
		}
		if (bFunctionParam != null)
		{
			for (int i = 0; i < bFunctionParam.length; i++)
			{
				scrollBox.removeChild(bFunctionParam[i]);
			}
			bFunctionParam = null;
		}
	}

	boolean doNotChangeCBVariable = false;

	private void showPageVariable()
	{
		cbVariables = new chkBox[Solver.Variablen.size()];
		lVariables = new Label[Solver.Variablen.size()];
		int i = 0;
		for (String variable : Solver.Variablen.keySet())
		{
			String value = Solver.Variablen.get(variable);
			cbVariables[i] = new chkBox(variable);
			cbVariables[i].setData(variable);
			scrollBox.addChild(cbVariables[i]);
			lVariables[i] = new Label(variable + " (" + value + ")");
			scrollBox.addChild(lVariables[i]);
			cbVariables[i].setChecked(sForm.equalsIgnoreCase(variable));
			cbVariables[i].setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(chkBox view, boolean isChecked)
				{
					if (doNotChangeCBVariable) return;
					doNotChangeCBVariable = true;
					for (chkBox cb : cbVariables)
					{
						cb.setChecked(cb == view);
					}
					doNotChangeCBVariable = false;
				}
			});
			i++;
		}
	}

	private void hidePageVariable()
	{
		for (Label l : lVariables)
		{
			scrollBox.removeChild(l);
		}
		cbVariables = null;
		lVariables = null;
	}

	private void savePageVariable()
	{
		for (chkBox cb : cbVariables)
		{
			scrollBox.removeChild(cb);
			if (cb.isChecked())
			{
				String variable = (String) cb.getData();
				if (variable != null)
				{
					sForm = variable;
				}
			}
		}

	}

	private void showPageOperator()
	{
		tbOperator = new EditTextField[2];
		bOperator = new Button[2];
		for (int i = 0; i < 2; i++)
		{
			tbOperator[i] = new EditTextField();
			scrollBox.addChild(tbOperator[i]);
			bOperator[i] = new Button("..");
			scrollBox.addChild(bOperator[i]);
			bOperator[i].setData(tbOperator[i]);
			bOperator[i].setOnClickListener(oclSolverString);
		}
	}

	OnClickListener oclSolverString = new OnClickListener()
	{
		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			String param = "";
			final EditTextField tb = (EditTextField) v.getData();
			if (tb != null)
			{
				param = tb.getText();
			}
			SolverDialog2 sd2 = new SolverDialog2(aktCache, param, false, DataType.Float);
			sd2.show(new SolverBackStringListner()
			{
				@Override
				public void BackString(String backString)
				{
					if (tb != null)
					{
						tb.setText(backString);
					}
				}
			});
			return true;
		}
	};

	private void hidePageOperator()
	{
		sForm = tbOperator[0].getText() + "+" + tbOperator[1].getText();
		for (int i = 0; i < 2; i++)
		{
			scrollBox.removeChild(tbOperator[i]);
			scrollBox.removeChild(bOperator[i]);
		}
		tbOperator = null;
		bOperator = null;
	}

	private void showPageWaypoint()
	{
		cbWaypoints = new chkBox[aktCache.waypoints.size() + 1];
		lWaypoints = new Label[aktCache.waypoints.size() + 1];

		for (int i = 0; i <= aktCache.waypoints.size(); i++)
		{
			Waypoint waypoint = null;
			String data = "";
			String description = "";
			if (i > 0)
			{
				waypoint = (Waypoint) aktCache.waypoints.get(i - 1);
				data = "$" + waypoint.getGcCode();
				description = "$" + waypoint.getGcCode() + " - " + waypoint.getTitle();
			}
			else
			{
				data = "$" + aktCache.getGcCode();
				description = "$" + aktCache.getGcCode() + " - " + aktCache.getName();
			}

			cbWaypoints[i] = new chkBox(data);
			cbWaypoints[i].setData(data);
			scrollBox.addChild(cbWaypoints[i]);
			lWaypoints[i] = new Label(description);
			scrollBox.addChild(lWaypoints[i]);
			cbWaypoints[i].setChecked(sForm.equalsIgnoreCase(data));

			cbWaypoints[i].setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(chkBox view, boolean isChecked)
				{
					if (doNotChangeCBVariable) return;
					doNotChangeCBVariable = true;
					for (chkBox cb : cbWaypoints)
					{
						cb.setChecked(cb == view);
					}
					doNotChangeCBVariable = false;
				}
			});
		}
	}

	private void hidePageWaypoint()
	{
		for (Label l : lWaypoints)
		{
			scrollBox.removeChild(l);
		}
		cbWaypoints = null;
		lWaypoints = null;
	}

	private void savePageWaypoint()
	{
		for (chkBox cb : cbWaypoints)
		{
			scrollBox.removeChild(cb);
			if (cb.isChecked())
			{
				String waypoint = (String) cb.getData();
				if (waypoint != null)
				{
					sForm = waypoint;
				}
			}
		}
	}

	private void showPageCoordinate()
	{
		bCoord = new CoordinateButton("Coordinate");
		bCoord.setCoordinate(new CoordinateGPS(sForm));
		scrollBox.addChild(bCoord);
	}

	private void hidePageCoordinate()
	{
		scrollBox.removeChild(bCoord);
		bCoord = null;
	}

	private void savePageCoordinate()
	{
		if (bCoord.getCoordinate().isValid())
		{
			sForm = bCoord.getCoordinate().FormatCoordinate();
		}
	}
}