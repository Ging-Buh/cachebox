package de.droidcachebox.settings;

import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;

public class SettingsItemBase extends ListViewItemBackground {
    private static float MeasuredLabelHeight = -1;
    protected CB_Label lblName, lblDefault;
    protected boolean isDisabled = false;

    public SettingsItemBase(CB_RectF rec, int Index, String Name) {
        super(rec, Index, Name);
        initialize();

        if (MeasuredLabelHeight == -1)
            MeasuredLabelHeight = Fonts.MeasureSmall("Tg").height;

        CB_RectF LblRec = new CB_RectF(leftBorder, 0, innerWidth, this.getHalfHeight());

        lblDefault = new CB_Label(LblRec);
        lblDefault.setFont(Fonts.getSmall());
        this.addChild(lblDefault);

        LblRec.setY(MeasuredLabelHeight);
        LblRec.setHeight(this.getHeight() - MeasuredLabelHeight);

        lblName = new CB_Label(LblRec);
        lblName.setFont(Fonts.getNormal());
        this.addChild(lblName);

        this.setLongClickable(true);

    }

    public void enable() {
        isDisabled = false;
        this.clearColorFilter();
    }

    public void disable() {
        isDisabled = true;
        this.setColorFilter(COLOR.getDisableFontColor());
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        // wenn Item disabled ein Behandelt zurück schicken,
        // damit keine weiteren Abfragen durchgereicht werden.
        // Auch wenn dieses Item ein OnClickListener hat.
        if (isDisabled)
            return true;
        else
            return super.click(x, y, pointer, button);
    }

    public void setName(String name) {
        lblName.setWrappedText(name);
        lblName.setHeight(lblName.getTextHeight());
        layout();
    }

    public void setDefault(String def) {
        lblDefault.setWrappedText(def);
        lblDefault.setHeight(lblDefault.getTextHeight());
        layout();
    }

    protected void layout() {
        float asc = lblDefault.getFont().getDescent() * 2;

        lblDefault.setY(this.getBottomHeight() + asc);

        asc = lblName.getFont().getDescent();

        float a = 0;

        if (lblName.getLineCount() == 1)
            a = asc *= 2;

        lblName.setY(lblDefault.getMaxY() - (asc * 2) + a);

        this.setHeight(this.getBottomHeight() + lblDefault.getHeight() + lblName.getHeight() - asc);

    }

}