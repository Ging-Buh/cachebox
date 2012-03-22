package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.Size;

public class HintDialog extends GL_MsgBox
{
	public HintDialog(Size size, String name)
	{
		super(size, name);

	}

	/**
	 * Zeigt den Hint des Global angewählten Caches
	 */
	public static void show()
	{
		if (GlobalCore.SelectedCache() == null) return;
		String hintText = GlobalCore.Rot13(GlobalCore.SelectedCache().hint);

		mMsgBoxClickListner = null;
		GL_MsgBox msgBox = new GL_MsgBox(calcMsgBoxSize(hintText, true), "MsgBox");
		msgBox.setTitle(GlobalCore.Translations.Get("hint"));
		label = new Label(msgBox.getContentSize().getBounds(), "MsgBoxLabel");
		label.setZeroPos();
		label.setWrappedText(hintText);
		msgBox.addChild(label);
		setButtonCaptions(msgBox, MessageBoxButtons.OKCancel);

		button3.setText(GlobalCore.Translations.Get("close"));
		button1.setText(GlobalCore.Translations.Get("decode"));

		button1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_MsgBox msgBox = (GL_MsgBox) GL_Listener.glListener.getActDialog();
				msgBox.setText(GlobalCore.Rot13(msgBox.getText()));
				return true;
			}
		});

		GL_Listener.glListener.showDialog(msgBox);

	}
}
