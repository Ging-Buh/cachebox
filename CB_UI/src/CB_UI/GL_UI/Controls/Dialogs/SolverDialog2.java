package CB_UI.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.SelectSolverFunction;
import CB_UI.GL_UI.Activitys.SelectSolverFunction.IFunctionResult;
import CB_UI.Solver.Functions.Function;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton;
import CB_UI_Base.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class SolverDialog2 extends ActivityBase implements OnStateChangeListener
{
	private enum pages
	{
		Nothing, Text, Function, Variable, Operator, Waypoint
	}

	private SolverBackStringListner mBackStringListner;
	private String solverString;
	private String sVar;
	private String sForm;
	private pages page;
	private ScrollBox scrollBox;
	private Button bOK, bCancel;
	private Label lblTitle;
	private float innerLeft;
	private EditTextField mVariableField;
	private Label lblGleich;
	private MultiToggleButton btnTxt;
	private MultiToggleButton btnFx;
	private MultiToggleButton btnVar;
	private MultiToggleButton btnOp;
	private MultiToggleButton btnWp;
	// Page Text
	private EditTextField mFormulaField;
	// Page Function
	private EditTextField tbFunction;
	private Button bFunction;
	private EditTextField[] tbFunctionParam = null;
	private Label[] lFunctionParam = null;
	private Button[] bFunctionParam = null;

	public interface SolverBackStringListner
	{
		public void BackString(String backString);
	}

	public SolverDialog2(String solverString)
	{
		super(ActivityRec(), "solverActivity");
		this.solverString = solverString;
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

		createVariableLines();
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

		// Buttons zur Auswahl des Dialog-Typs
		float w = innerWidth / 5;
		float x = 0;
		btnTxt = new MultiToggleButton(x, 0, w, UI_Size_Base.that.getButtonHeight(), "TXT");
		scrollBox.addChild(btnTxt);
		x += w;
		btnFx = new MultiToggleButton(x, 0, w, UI_Size_Base.that.getButtonHeight(), "f(x)");
		scrollBox.addChild(btnFx);
		x += w;
		btnVar = new MultiToggleButton(x, 0, w, UI_Size_Base.that.getButtonHeight(), "@");
		scrollBox.addChild(btnVar);
		x += w;
		btnOp = new MultiToggleButton(x, 0, w, UI_Size_Base.that.getButtonHeight(), "+-");
		scrollBox.addChild(btnOp);
		x += w;
		btnWp = new MultiToggleButton(x, 0, w, UI_Size_Base.that.getButtonHeight(), "$GC");
		scrollBox.addChild(btnWp);

		String caption = Translation.Get("TXT");
		btnTxt.setText(caption);
		btnTxt.addState(caption, Color.GRAY);
		btnTxt.addState(caption, Color.GREEN);
		btnTxt.setOnStateChangedListner(this);

		caption = Translation.Get("f(x)");
		btnFx.setText(caption);
		btnFx.addState(caption, Color.GRAY);
		btnFx.addState(caption, Color.GREEN);
		btnFx.setOnStateChangedListner(this);

		caption = Translation.Get("@");
		btnVar.setText(caption);
		btnVar.addState(caption, Color.GRAY);
		btnVar.addState(caption, Color.GREEN);
		btnVar.setOnStateChangedListner(this);

		caption = Translation.Get("+-*/");
		btnOp.setText(caption);
		btnOp.addState(caption, Color.GRAY);
		btnOp.addState(caption, Color.GREEN);
		btnOp.setOnStateChangedListner(this);

		caption = Translation.Get("$GC");
		btnWp.setText(caption);
		btnWp.addState(caption, Color.GRAY);
		btnWp.addState(caption, Color.GREEN);
		btnWp.setOnStateChangedListner(this);

	}

	private void Layout()
	{
		float y = margin + innerHeight - btnTxt.getHeight() * 4;

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
			break;
		case Text:
			mFormulaField.setY(y);
			y += mFormulaField.getHeight() + margin;
			break;
		case Variable:
			break;
		case Waypoint:
			break;
		default:
			break;
		}

		btnTxt.setY(y);
		btnFx.setY(y);
		btnVar.setY(y);
		btnOp.setY(y);
		btnWp.setY(y);
		y += btnTxt.getHeight() + margin;

		lblGleich.setY(y - lblGleich.getHalfHeight() / 2);
		y += lblGleich.getHeight() - lblGleich.getHalfHeight();

		mVariableField.setY(y);
		y += mVariableField.getHeight() + margin;

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
				// damit die Änderungen in sForm gespeichert werden
				showPage(pages.Nothing);
				String result = mVariableField.getText();
				if (result.length() > 0) result += "=";
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

		lblTitle = new Label(leftBorder + margin, this.height - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight,
				"TitleSolver");
		lblTitle.setFont(Fonts.getBig());
		float lblWidth = lblTitle.setText(Translation.Get("solver_formula")).getTextWidth();
		this.addChild(lblTitle);

	}

	@Override
	public void onStateChange(GL_View_Base v, int State)
	{
		// Statusänderung eines MultiToggleButtons
		if (State == 1)
		{
			if (v == btnTxt)
			{
				showPage(pages.Text);
			}
			if (v == btnFx)
			{
				showPage(pages.Function);
			}
			if (v == btnVar)
			{
				showPage(pages.Variable);
			}
			if (v == btnOp)
			{
				showPage(pages.Operator);
			}
			if (v == btnWp)
			{
				showPage(pages.Waypoint);
			}
		}
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
		case Function:
			hidePageFunction();
			break;
		case Variable:
			// hidePageVariable();
			break;
		case Operator:
			// hidePageOperator();
			break;
		case Waypoint:
			// hidePageWaypoint();
			break;
		default:
			break;
		}

		this.page = page;
		// set State of buttons
		btnTxt.setState(page == pages.Text ? 1 : 0);
		btnFx.setState(page == pages.Function ? 1 : 0);
		btnVar.setState(page == pages.Variable ? 1 : 0);
		btnOp.setState(page == pages.Operator ? 1 : 0);
		btnWp.setState(page == pages.Waypoint ? 1 : 0);

		switch (page)
		{
		case Text:
			showPageText();
			break;
		case Function:
			showPageFunction();
			break;
		case Variable:
			// showPageVariable();
			break;
		case Operator:
			// showPageOperator();
			break;
		case Waypoint:
			// showPageWaypoint();
			break;
		default:
			break;
		}
		Layout();
	}

	private void hidePageText()
	{
		sForm = mFormulaField.getText();
		scrollBox.removeChild(mFormulaField);
		mFormulaField = null;
	}

	private void showPageText()
	{
		mFormulaField = new EditTextField(this);
		mFormulaField.setWrapType(WrapType.SINGLELINE);
		mFormulaField.setX(innerLeft);
		mFormulaField.setWidth(innerWidth);
		mFormulaField.setText(sForm);
		mFormulaField.setZeroPos();

		scrollBox.addChild(mFormulaField);
	}

	private void showPageFunction()
	{

		tbFunction = new EditTextField(this);
		tbFunction.setText(sForm);
		scrollBox.addChild(tbFunction);

		bFunction = new Button("");
		bFunction.setText("F(x)");
		scrollBox.addChild(bFunction);

		// Funktion aufsplitten nach Funktionsname und Parameter (falls möglich!)
		String formula = sForm.trim();
		int posKlammerAuf = formula.indexOf("(");
		int posKlammerZu = formula.lastIndexOf(")");
		if ((posKlammerAuf >= 0) && (posKlammerZu > posKlammerAuf))
		{
			// gültige Formel erkannt
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
				addFunctionParamLine(i, parameters[i].trim());
			}
		}
		bFunction.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Funktionsauswahl zeigen
				SelectSolverFunction ssf = new SelectSolverFunction(new IFunctionResult()
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
							addFunctionParamLine(i, "");
						}
						Layout();
					}
				});
				GL.that.showDialog(ssf);
				return true;
			}
		});
	}

	private void addFunctionParamLine(int i, String string)
	{
		// Eingabefelder für die Parameter einfügen
		lFunctionParam[i] = new Label();
		lFunctionParam[i].setText("Parameter" + " " + i);
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
				Integer i = (Integer) v.getData();
				if (i != null)
				{
					param = tbFunctionParam[i].getText();
				}
				SolverDialog2 sd2 = new SolverDialog2(param);
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

	private void hidePageFunction()
	{
		// geänderte Formel merken
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
		// Parameter entfernen
		removeFunctionParam();
		scrollBox.removeChild(tbFunction);
		scrollBox.removeChild(bFunction);
		tbFunction = null;
		bFunction = null;
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
	}

}
