package de.droidcachebox.settings;

import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.FloatControl;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class SettingsItem_Audio extends SettingsItemBase {

    CB_CheckBox Check;
    FloatControl volumeControl;

    /**
     * @param rec
     * @param Index
     * @param Name
     * @param full  boolean /show with Path-Settings
     */
    public SettingsItem_Audio(CB_RectF rec, int Index, String Name, Boolean full, FloatControl.iValueChanged listener) {
        super(rec, Index, Name);

        // TODO add option for load SoundFile
        // if (full) this.setHeight(rec.getHeight() * 2);

        Check = new CB_CheckBox("");
        Check.setX(this.getWidth() - rightBorder - Check.getWidth());
        Check.setY(this.getHalfHeight() - Check.getHalfHeight());

        lblName.setWidth(lblName.getWidth() - Check.getWidth() - rightBorder);

        // remove default Label
        this.removeChild(lblDefault);
        lblDefault.dispose();
        lblDefault = null;

        // add volume control
        float ProgressHeight = (Sprites.ProgressBack.getBottomHeight() + Sprites.ProgressBack.getTopHeight()) * 1.35f;

        CB_RectF rec2 = rec.copy();

        float margin = UiSizes.getInstance().getMargin();

        rec2.setHeight(ProgressHeight);
        rec2.setWidth(this.getWidth() - Check.getWidth() - (margin * 2));
        rec2.setX(margin);
        volumeControl = new FloatControl(rec2, "", listener);
        volumeControl.setProgress(50);

        this.addChild(Check);
        this.addChild(volumeControl);

    }

    public CB_CheckBox getCheckBox() {
        return Check;
    }

    @Override
    public void setDefault(String def) {
        layout();
    }

    @Override
    protected void layout() {
        // don't call super.layout(); DefaultLabel is NULL

        Check.setY(this.getHalfHeight() - Check.getHalfHeight());

        float asc = lblName.getFont().getDescent();

        lblName.setY(this.getHeight() - lblName.getHeight() + asc);
        lblName.setX(this.getLeftWidth());
    }

    public void setVolume(int value) {
        volumeControl.setProgress(value);
    }

    public void setMuteDisabeld(boolean checked) {
        // Set VolumeControl to disabled
        volumeControl.disable(checked);
    }

    @Override
    public void disable() {
        volumeControl.disable(true);
        Check.disable();
    }

    @Override
    public void enable() {
        if (!Check.isChecked())
            volumeControl.disable(false);
        Check.enable();
    }
}
