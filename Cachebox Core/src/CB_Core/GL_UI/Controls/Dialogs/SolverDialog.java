package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.TextField;
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
	private TextField mTextField;
	private String mSolverString;
	private SloverBackStringListner mBackStringListner;

	public SolverDialog(CB_RectF rec, String name, String SolverString)
	{
		super(rec, name);
		mSolverString = SolverString;
	}

	private void initialLayout()
	{

		// initial MsgBox
		mMsgBox = new GL_MsgBox(new Size((int) width, (int) height), "MsgBox");
		mMsgBox.setTitle(GlobalCore.Translations.Get("solver_formula"));

		setButtonCaptions(mMsgBox, MessageBoxButtons.OKCancel);
		SizeF msgBoxContentSize = mMsgBox.getContentSize();

		// initial TextField
		float TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;
		CB_RectF rec = new CB_RectF(0, msgBoxContentSize.height - TextFieldHeight, msgBoxContentSize.width, TextFieldHeight);
		mTextField = new TextField(rec, "SolverDialogTextField");
		mTextField.setText(mSolverString);
		mTextField.setMsg("Enter formula");

		mMsgBox.addChild(mTextField);

		button3.setText(GlobalCore.Translations.Get("close"));
		button1.setText(GlobalCore.Translations.Get("Ok"));

		button1.setOnClickListener(OnOkClickListner);

	}

	private OnClickListener OnOkClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			if (mBackStringListner != null) mBackStringListner.BackString(mTextField.getText());
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
