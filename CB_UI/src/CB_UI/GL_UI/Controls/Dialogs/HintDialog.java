package CB_UI.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.Dialog;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_Utils.Util.UnitFormatter;

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
		if (!GlobalCore.getSelectedCache().hasHint()) return;

		String HintFromDB = GlobalCore.getSelectedCache().getHint();

		hintTextDecoded = UnitFormatter.Rot13(HintFromDB) + "\n ";
		hintTextEncoded = HintFromDB + "\n ";

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

		CB_RectF rec = msgBox.getContentSize().getBounds();
		scrollBox = new ScrollBox(rec);

		msgBox.label = new Label("Hint"); // oder ohne Parameter aufrufen
		// damit label.Pos auf (leftBorder, bottomBorder) gesetzt wird (ev. 0,0)
		scrollBox.initRow(BOTTOMUP);
		// damit die Breite des Labels zur Bestimmung des Umbruchs gesetzt ist:
		scrollBox.addLast(msgBox.label);
		msgBox.label.setWrappedText(hintTextEncoded); // , Fonts.getBig()
		float lblHeight = msgBox.label.getTextHeight();
		// der decodierte Text wird per Default zuerst angezeigt
		msgBox.label.setWrappedText(hintTextDecoded);// , Fonts.getBig()
		float lblHeigtTextDecoded = msgBox.label.getTextHeight();
		// Falls der decodierte Text mehr Höhe benötigt, dann diese nehmen
		if (lblHeigtTextDecoded > lblHeight) lblHeight = lblHeigtTextDecoded;
		// vorsichtshalber oben und unten die margin berücksichtigen
		lblHeight = lblHeight + 2f * margin;
		// Anpassung der Label Höhe, damit der ganze Text drauf passt
		msgBox.label.setHeight(lblHeight);
		// nur der Label ist auf der Scrollbox
		scrollBox.setVirtualHeight(lblHeight);

		msgBox.addChild(scrollBox);

		msgBox.mMsgBoxClickListner = null; // todo
		msgBox.button1.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				GL_MsgBox msgBox = (GL_MsgBox) GL.that.getActDialog();
				msgBox.label.setWrappedText(UnitFormatter.Rot13(msgBox.label.getText()));
				return true;
			}
		});

		GL.that.showDialog(msgBox);

	}
}
