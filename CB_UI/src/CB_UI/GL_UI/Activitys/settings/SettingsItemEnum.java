package CB_UI.GL_UI.Activitys.settings;

import CB_UI_Base.GL_UI.Controls.Spinner;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;

public class SettingsItemEnum extends SettingsItemBase {

    Spinner spinner;

    public SettingsItemEnum(CB_RectF rec, int Index, String Name) {
        super(rec, Index, Name);

        spinner = new Spinner(lblDefault, Name, null, null);

        spinner.setHeight(UiSizes.getInstance().getButtonHeight());

        this.addChild(spinner);

    }

    public Spinner getSpinner() {
        return spinner;
    }

    @Override
    protected void layout() {
        lblDefault.setHeight(spinner.getHeight());
        super.layout();
        spinner.setY(lblDefault.getY() + UiSizes.getInstance().getMargin());

    }

}
