package CB_UI.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_Utils.Util.UnitFormatter;

public class HintDialog extends MessageBox {

    ScrollBox scrollBox;

    public HintDialog(Size size, String hint) {
        super(size, "");
        setTitle(Translation.get("hint"));
        addButtons(Translation.get("decode"), null, Translation.get("close"));
        CB_RectF rec = getContentSize().getBounds();
        scrollBox = new ScrollBox(rec);
        label = new CB_Label("Hint"); // oder ohne Parameter aufrufen
        // damit label.Pos auf (leftBorder, bottomBorder) gesetzt wird (ev. 0,0)
        scrollBox.initRow(BOTTOMUP);
        // damit die Breite des Labels zur Bestimmung des Umbruchs gesetzt ist:
        scrollBox.addLast(label);
        label.setWrappedText(UnitFormatter.Rot13(hint)); // , Fonts.getBig()
        float lblHeight = label.getTextHeight();
        // der decodierte Text wird per Default zuerst angezeigt
        label.setWrappedText(hint);// , Fonts.getBig()
        float lblHeigtTextDecoded = label.getTextHeight();
        // Falls der decodierte Text mehr Höhe benötigt, dann diese nehmen
        if (lblHeigtTextDecoded > lblHeight)
            lblHeight = lblHeigtTextDecoded;
        // vorsichtshalber oben und unten die margin berücksichtigen
        lblHeight = lblHeight + 2f * margin;
        // Anpassung der Label Höhe, damit der ganze Text drauf passt
        label.setHeight(lblHeight);
        // nur der Label ist auf der Scrollbox
        scrollBox.setVirtualHeight(lblHeight);
        addChild(scrollBox);
        getButton(1).setOnClickListener((v, x, y, pointer, button) -> {
            setMessage(UnitFormatter.Rot13(label.getText()));
            return true;
        });
    }

    public void show() {
        GL.that.showDialog(this);
    }
}
