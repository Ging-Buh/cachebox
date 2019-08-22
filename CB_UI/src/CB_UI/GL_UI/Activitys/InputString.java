package CB_UI.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Fonts;

public class InputString extends ActivityBase {
    private CB_Button btnOK;
    private CB_Button btnCancel;
    private ScrollBox scrollBox;
    private EditTextField edtResult;
    private String title;

    public InputString(String title) {
        // output of a title is not yet implemented in ActivityBase
        super(ActivityBase.ActivityRec(), title);
        this.title = title;
        createControls();
    }

    private void createControls() {
        btnOK = new CB_Button(Translation.get("ok"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        this.initRow(BOTTOMUP);
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
        edtResult = new EditTextField(this, "edtResult").setWrapType(WrapType.WRAPPED);
        edtResult.setHeight(getHeight() / 2);
        box.addLast(edtResult);

        box.adjustHeight();
        scrollBox.setVirtualHeight(box.getHeight());

        btnOK.addClickHandler((v, x, y, pointer, button) -> {
            btnOK.disable();
            callBack(edtResult.getText());
            finish();
            return true;
        });

        btnCancel.addClickHandler((v, x, y, pointer, button) -> {
            callBack("");
            finish();
            return true;
        });

    }

    public void callBack(String result) {
    }

    public void doShow() {
        show();
    }

    public void doFinish() {
        finish();
    }

}
