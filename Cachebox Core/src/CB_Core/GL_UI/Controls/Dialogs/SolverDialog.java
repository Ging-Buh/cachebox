package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MultiToggleButton;
import CB_Core.GL_UI.Controls.MultiToggleButton.OnStateChangeListener;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Color;

public class SolverDialog extends GL_MsgBox implements OnStateChangeListener
{
	private enum pages
	{
		Text, Function, Variable, Operator, Waypoint
	}

	private boolean ignoreStateChange = false;
	private MultiToggleButton btnTxt;
	private MultiToggleButton btnFx;
	private MultiToggleButton btnVar;
	private MultiToggleButton btnOp;
	private MultiToggleButton btnWp;
	private SizeF msgBoxContentSize;
	private float TextFieldHeight;
	private pages page;
	private String sVar;
	private String sForm;
	private float startY;
	// Controls for TextView
	private EditWrapedTextField mFormulaField;

	// Controls for FormulaView
	// Controls for VariableView
	private EditWrapedTextField tbVariable;
	private Button bVariable;

	// Controls for OperatorView
	// Controls for WaypointView

	public interface SloverBackStringListner
	{
		public void BackString(String backString);
	}

	private GL_MsgBox mMsgBox;
	private EditWrapedTextField mVariableField;
	private String mSolverString;
	private SloverBackStringListner mBackStringListner;

	public SolverDialog(CB_RectF rec, String name, String SolverString)
	{
		super(rec, name);
		mSolverString = SolverString;
		ignoreStateChange = false;
		page = pages.Text;
	}

	private void initialLayout()
	{
		// Split Solver String by =
		String[] solverStrings = mSolverString.split("=", 2);
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

		// initial MsgBox
		mMsgBox = new GL_MsgBox(new Size((int) width, (int) height), "MsgBox");
		mMsgBox.setTitle(GlobalCore.Translations.Get("solver_formula"));

		setButtonCaptions(mMsgBox, MessageBoxButtons.OKCancel);
		msgBoxContentSize = mMsgBox.getContentSize();
		// initial VariableField
		TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

		float y = msgBoxContentSize.height - TextFieldHeight;

		CB_RectF rec = new CB_RectF(0, y, msgBoxContentSize.width, TextFieldHeight);
		mVariableField = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextField");
		mVariableField.setText(sVar);
		// mVariableField.setMsg("Enter formula");
		mMsgBox.addChild(mVariableField);
		y -= TextFieldHeight;

		rec = new CB_RectF(0, y, msgBoxContentSize.width, TextFieldHeight);
		Label lbGleich = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "=");
		lbGleich.setFont(Fonts.getNormal());
		lbGleich.setText("=");
		setBackground(SpriteCache.ListBack);
		mMsgBox.addChild(lbGleich);
		y -= TextFieldHeight;

		// Buttons zur Auswahl des Dialog-Typs
		float w = msgBoxContentSize.width / 5;
		float x = 0;
		btnTxt = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "TXT");
		mMsgBox.addChild(btnTxt);
		x += w;
		btnFx = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "f(x)");
		mMsgBox.addChild(btnFx);
		x += w;
		btnVar = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "@");
		mMsgBox.addChild(btnVar);
		x += w;
		btnOp = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "+-");
		mMsgBox.addChild(btnOp);
		x += w;
		btnWp = new MultiToggleButton(x, y, w, UiSizes.getButtonHeight(), "$GC");
		mMsgBox.addChild(btnWp);
		y -= UiSizes.getButtonHeight();

		// startposition for further controls
		this.startY = y;

		String caption = GlobalCore.Translations.Get("TXT");
		btnTxt.setText(caption);
		btnTxt.addState(caption, Color.GRAY);
		btnTxt.addState(caption, Color.GREEN);
		btnTxt.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("f(x)");
		btnFx.setText(caption);
		btnFx.addState(caption, Color.GRAY);
		btnFx.addState(caption, Color.GREEN);
		btnFx.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("@");
		btnVar.setText(caption);
		btnVar.addState(caption, Color.GRAY);
		btnVar.addState(caption, Color.GREEN);
		btnVar.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("+-*/");
		btnOp.setText(caption);
		btnOp.addState(caption, Color.GRAY);
		btnOp.addState(caption, Color.GREEN);
		btnOp.setOnStateChangedListner(this);

		caption = GlobalCore.Translations.Get("$GC");
		btnWp.setText(caption);
		btnWp.addState(caption, Color.GRAY);
		btnWp.addState(caption, Color.GREEN);
		btnWp.setOnStateChangedListner(this);

		button3.setText(GlobalCore.Translations.Get("close"));
		button1.setText(GlobalCore.Translations.Get("Ok"));

		button1.setOnClickListener(OnOkClickListner);
		showPageText();
	}

	private OnClickListener OnOkClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			String result = mVariableField.getText();
			if (result.length() > 0) result += "=";
			result += mFormulaField.getText();
			if (mBackStringListner != null) mBackStringListner.BackString(result);
			mMsgBox.close();
			mMsgBox.dispose();
			return true;
		}
	};

	public void show(SloverBackStringListner listner)
	{
		mBackStringListner = listner;
		initialLayout();
		GL.that.showDialog(mMsgBox);

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
		// set State of buttons
		if (page != pages.Text)
		{
			btnTxt.setState(0);
		}
		if (page != pages.Function)
		{
			btnFx.setState(0);
		}
		if (page != pages.Variable)
		{
			btnVar.setState(0);
		}
		if (page != pages.Operator)
		{
			btnOp.setState(0);
		}
		if (page != pages.Waypoint)
		{
			btnWp.setState(0);
		}

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
			hidePageVariable();
			break;
		case Operator:
			hidePageOperator();
			break;
		case Waypoint:
			hidePageWaypoint();
			break;
		}
		switch (page)
		{
		case Text:
			showPageText();
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
		}

	}

	private void hidePageWaypoint()
	{
		// TODO Auto-generated method stub

	}

	private void hidePageOperator()
	{
		// TODO Auto-generated method stub

	}

	private void hidePageVariable()
	{
		mMsgBox.removeChild(tbVariable);
		mMsgBox.removeChild(bVariable);
		tbVariable = null;
		bVariable = null;

	}

	private void hidePageFunction()
	{
		// TODO Auto-generated method stub

	}

	private void hidePageText()
	{
		mMsgBox.removeChild(mFormulaField);
		mFormulaField = null;
	}

	private void showPageWaypoint()
	{
		// TODO Auto-generated method stub

	}

	private void showPageOperator()
	{
		// TODO Auto-generated method stub

	}

	private void showPageVariable()
	{
		// initial VariableField
		float y = startY;
		CB_RectF rec = new CB_RectF(0, y, msgBoxContentSize.width - TextFieldHeight * 2, TextFieldHeight);
		tbVariable = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextField");
		tbVariable.setText(sForm);
		mMsgBox.addChild(tbVariable);
		bVariable = new Button(msgBoxContentSize.width - TextFieldHeight * 2, y, TextFieldHeight * 2, TextFieldHeight,
				"SolverDialogBtnVariable");
		bVariable.setText("Var");
		mMsgBox.addChild(bVariable);
		y -= TextFieldHeight;
	}

	private void showPageFunction()
	{
		// TODO Auto-generated method stub

	}

	private void showPageText()
	{
		// initial FormulaField
		float y = startY;
		CB_RectF rec = new CB_RectF(0, y, msgBoxContentSize.width, TextFieldHeight);
		mFormulaField = new EditWrapedTextField(this, rec, EditWrapedTextField.TextFieldType.SingleLine, "SolverDialogTextField");
		mFormulaField.setText(sForm);
		mMsgBox.addChild(mFormulaField);
		y -= TextFieldHeight;
	}

}
