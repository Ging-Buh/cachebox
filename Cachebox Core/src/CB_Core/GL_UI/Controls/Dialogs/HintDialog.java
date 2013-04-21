package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.utils.ColorDrawable;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.Color;

public class HintDialog extends GL_MsgBox
{

	static ScrollBox scrollBox;
	static String hintTextDecoded;
	static String hintTextEncoded;

	public HintDialog(Size size, String name)
	{
		super(size, name);

	}

	/**
	 * Zeigt den Hint des Global angewählten Caches
	 */
	public static void show()
	{
		if (GlobalCore.getSelectedCache() == null) return;
		hintTextDecoded = GlobalCore.Rot13(GlobalCore.getSelectedCache().hint);
		hintTextEncoded = GlobalCore.getSelectedCache().hint;

		Size decodedSize = calcMsgBoxSize(hintTextDecoded, true, true, false);
		Size encodedSize = calcMsgBoxSize(hintTextEncoded, true, true, false);

		Size maxTextSize = decodedSize.height > encodedSize.height ? decodedSize : encodedSize;

		GL_MsgBox msgBox = new GL_MsgBox(maxTextSize, "MsgBox");
		msgBox.setTitle(Translation.Get("hint"));
		msgBox.setButtonCaptions(MessageBoxButtons.OKCancel);
		msgBox.mMsgBoxClickListner = null;

		CB_RectF rec = msgBox.getContentSize().getBounds();
		scrollBox = new ScrollBox(rec, 100, "");

		float lblHeigt = Fonts.MeasureWrapped(hintTextDecoded, rec.getWidth()).height + (2 * margin);
		rec.setHeight(lblHeigt);

		msgBox.label = new Label(rec, "MsgBoxLabel");
		msgBox.label.setZeroPos();
		msgBox.label.setWrappedText(hintTextDecoded);

		msgBox.label.setBackground(new ColorDrawable(Color.RED));

		// label in Scrollbox verpacken
		scrollBox.addChild(msgBox.label);
		scrollBox.setInerHeight(msgBox.label.getHeight());

		msgBox.addChild(scrollBox);

		msgBox.button3.setText(Translation.Get("close"));
		msgBox.button1.setText(Translation.Get("decode"));

		msgBox.button1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_MsgBox msgBox = (GL_MsgBox) GL.that.getActDialog();
				CB_RectF rec = msgBox.getContentSize().getBounds();

				String txt = GlobalCore.Rot13(msgBox.label.getText());

				float lblHeigt = Fonts.MeasureWrapped(txt, rec.getWidth()).height + (2 * margin);
				msgBox.label.setZeroPos();
				msgBox.label.setWrappedText(txt);
				msgBox.label.setHeight(lblHeigt);
				scrollBox.setInerHeight(msgBox.label.getHeight());
				return true;
			}
		});

		GL.that.showDialog(msgBox);

	}
}
