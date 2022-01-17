package de.droidcachebox.gdx.activities;

import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.translation.Translation;

public class InputString extends ActivityBase {
    // possibly use the StringInputBox instead
    private CB_Button btnOK;
    private CB_Button btnCancel;
    private ScrollBox scrollBox;
    private EditTextField edtResult;
    private String title;

    public InputString(String title) {
        // output of a title is not yet implemented in ActivityBase
        super(title);
        this.title = title;
        createControls(false);
    }

    public InputString(String title, boolean oneLine) {
        // output of a title is not yet implemented in ActivityBase
        super(title);
        this.title = title;
        createControls(true);
    }

    private void createControls(boolean oneLine) {
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUp);
        this.addNext(btnOK);
        this.addLast(btnCancel);
        scrollBox = new ScrollBox(0, getAvailableHeight());
        scrollBox.setBackground(this.getBackground());
        this.addLast(scrollBox);
        Box box = new Box(scrollBox.getInnerWidth(), 0); // height will be adjusted after containing all controls
        scrollBox.addChild(box);

        CB_Label lblResult = new CB_Label(Translation.get(title));
        lblResult.setWidth(Fonts.Measure(lblResult.getText()).width);
        box.addLast(lblResult, FIXED);
        if (oneLine) {
            edtResult = new EditTextField(this, "edtResult");
        }
        else {
            edtResult = new EditTextField(this, "edtResult").setWrapType(WrapType.WRAPPED);
            edtResult.setHeight(getHeight() / 2);
        }
        box.addLast(edtResult);

        box.adjustHeight();
        scrollBox.setVirtualHeight(box.getHeight());

        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            btnOK.disable();
            if (edtResult.getText().length() > 0)
                callBack(edtResult.getText());
            finish();
            return true;
        });

        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            // no callBack
            finish();
            return true;
        });

    }

    public void callBack(String result) {
    }

}
