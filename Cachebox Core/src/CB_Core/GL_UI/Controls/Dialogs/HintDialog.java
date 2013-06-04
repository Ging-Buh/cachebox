package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Dialog;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.TranslationEngine.Translation;

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
		hintTextDecoded = GlobalCore.Rot13(GlobalCore.getSelectedCache().hint) + "\n ";
		hintTextEncoded = GlobalCore.getSelectedCache().hint + "\n ";

		// nur damit bei mir die Box maximiert kommt und damit der Text nicht skaliert.
		// !!! gilt für alle Dialoge, da statisch definiert. Könnte es auch dort ändern.
		Dialog.margin = 5;
		Size decodedSize = calcMsgBoxSize(hintTextDecoded, true, true, false);
		Size encodedSize = calcMsgBoxSize(hintTextEncoded, true, true, false);

		Size maxTextSize = decodedSize.height > encodedSize.height ? decodedSize : encodedSize;

		GL_MsgBox msgBox = new GL_MsgBox(maxTextSize, "MsgBox");
		msgBox.setTitle(Translation.Get("hint"));
		msgBox.setButtonCaptions(MessageBoxButtons.OKCancel);
		msgBox.button3.setText(Translation.Get("close"));
		msgBox.button1.setText(Translation.Get("decode"));

		msgBox.mMsgBoxClickListner = null;

		CB_RectF rec = msgBox.getContentSize().getBounds();
		scrollBox = new ScrollBox(rec, 100, "");

		msgBox.label = new Label("Hint");
		scrollBox.initRow(BOTTOMUP); // damit label.Pos auf 0,0 gesetzt wird
		// damit die Breite des Labels zur Bestimmung des Umbruchs gesetzt ist:
		scrollBox.addLast(msgBox.label);
		msgBox.label.setWrappedText(hintTextEncoded, Fonts.getBig());
		float lblHeigt = msgBox.label.getTextHeight();
		msgBox.label.setWrappedText(hintTextDecoded, Fonts.getBig());
		float lblHeigtTextDecoded = msgBox.label.getTextHeight();
		if (lblHeigtTextDecoded > lblHeigt) lblHeigt = lblHeigtTextDecoded;
		msgBox.label.setHeight(lblHeigt); // Damit der ganze Text drauf ist
		// wenn die virtuelle Höhe > als die scrollBox Höhe - margin ist, dann wird gescrollt
		scrollBox.setVirtualHeight(lblHeigt); // nur der Label ist auf der Scrollbox

		msgBox.addChild(scrollBox);

		msgBox.button1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_MsgBox msgBox = (GL_MsgBox) GL.that.getActDialog();
				msgBox.label.setWrappedText(GlobalCore.Rot13(msgBox.label.getText()));
				return true;
			}
		});

		GL.that.showDialog(msgBox);

	}
}
