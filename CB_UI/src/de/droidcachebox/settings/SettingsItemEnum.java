package de.droidcachebox.settings;

import de.droidcachebox.gdx.controls.Spinner;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

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
