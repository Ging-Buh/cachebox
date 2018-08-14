package CB_UI_Base.view.settings;

import CB_UI_Base.GL_UI.Controls.Spinner;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class SettingsItemEnum extends SettingsItemBase {

    Spinner spinner;

    public SettingsItemEnum(CB_RectF rec, int Index, String Name) {
        super(rec, Index, Name);

        spinner = new Spinner(lblDefault, "", null, null);

        spinner.setHeight(UI_Size_Base.that.getButtonHeight());

        this.addChild(spinner);

    }

    public Spinner getSpinner() {
        return spinner;
    }

    @Override
    protected void layout() {
        lblDefault.setHeight(spinner.getHeight());
        super.layout();
        spinner.setY(lblDefault.getY() + UI_Size_Base.that.getMargin());

    }

}
