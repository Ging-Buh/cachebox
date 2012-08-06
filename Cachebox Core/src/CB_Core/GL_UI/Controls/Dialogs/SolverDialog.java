package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.EditTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.SizeF;

public class SolverDialog extends GL_MsgBox
{

	public interface SloverBackStringListner
	{
		public void BackString(String backString);
	}

	private GL_MsgBox mMsgBox;
	private EditTextField mVariableField;
	private EditTextField mFormulaField;
	private String mSolverString;
	private SloverBackStringListner mBackStringListner;

	public SolverDialog(CB_RectF rec, String name, String SolverString)
	{
		super(rec, name);
		mSolverString = SolverString;
	}

	private void initialLayout()
	{
		// Split Solver String by =
		String[] solverStrings = mSolverString.split("=", 2);
		String sVar = "";
		String sForm = "";
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
		SizeF msgBoxContentSize = mMsgBox.getContentSize();

		// initial VariableField
		float TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;
		CB_RectF rec = new CB_RectF(0, msgBoxContentSize.height - TextFieldHeight, msgBoxContentSize.width, TextFieldHeight);
		mVariableField = new EditTextField(rec, "SolverDialogTextField");
		mVariableField.setText(sVar);
		// mVariableField.setMsg("Enter formula");
		mMsgBox.addChild(mVariableField);

		rec = new CB_RectF(0, msgBoxContentSize.height - TextFieldHeight * 2, msgBoxContentSize.width, TextFieldHeight);
		Label lbGleich = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "=");
		lbGleich.setFont(Fonts.getNormal());
		lbGleich.setText("=");
		setBackground(SpriteCache.ListBack);
		mMsgBox.addChild(lbGleich);

		// initial FormulaField
		rec = new CB_RectF(0, msgBoxContentSize.height - TextFieldHeight * 3, msgBoxContentSize.width, TextFieldHeight);
		mFormulaField = new EditTextField(rec, "SolverDialogTextField");
		mFormulaField.setText(sForm);
		// mFormulaField.setMsg("Enter formula");

		mMsgBox.addChild(mFormulaField);

		button3.setText(GlobalCore.Translations.Get("close"));
		button1.setText(GlobalCore.Translations.Get("Ok"));

		button1.setOnClickListener(OnOkClickListner);

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
		GL_Listener.glListener.showDialog(mMsgBox);

	}

}
