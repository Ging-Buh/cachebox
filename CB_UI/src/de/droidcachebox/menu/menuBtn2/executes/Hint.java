package de.droidcachebox.menu.menuBtn2.executes;

import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.UnitFormatter;

public class Hint extends ButtonDialog {
    public Hint(String hint) {
        super(hint, Translation.get("hint"), MsgBoxButton.OKCancel, MsgBoxIcon.None);
        buttonClickHandler = (btnNumber, data) -> true;
        setButtonText("decode", null, "close");
        btnLeftPositive.setClickHandler((view, x, y, pointer, button) -> {
            setMessage(UnitFormatter.Rot13(msgLbl.getText()));
            return true;
        });
    }

}
