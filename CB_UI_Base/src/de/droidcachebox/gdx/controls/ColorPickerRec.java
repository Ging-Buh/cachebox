package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.Color;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.graphics.ColorDrawable;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;

public class ColorPickerRec extends CB_View_Base {

    private HSV_Color mColor = new HSV_Color(Color.YELLOW);

    public ColorPickerRec(CB_RectF rec, String Name) {
        super(rec, Name);
        colorChanged();
    }

    public void setHue(float hue) {
        mColor.setHue(hue);
        colorChanged();
    }

    public void setColor(HSV_Color color) {
        mColor = color;
    }

    public void setColor(Color color) {
        mColor = new HSV_Color(color);
    }

    private void colorChanged() {
        GL.that.RunOnGL(() -> setBackground(new ColorDrawable(mColor)));

    }
}
