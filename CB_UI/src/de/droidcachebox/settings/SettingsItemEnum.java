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
        addChild(spinner);
    }

    public Spinner getSpinner() {
        return spinner;
    }

    @Override
    protected void layout() {
        spinner.setY(getBottomHeight() + (lblDefault.getFont().getDescent() * 2));
        lblDefault.setY(spinner.getMaxY());
        lblName.setY(lblDefault.getMaxY());
        setHeight(getHeight() + spinner.getHeight());
    }

}
