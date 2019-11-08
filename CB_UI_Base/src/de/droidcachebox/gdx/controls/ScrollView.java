package de.droidcachebox.gdx.controls;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;

public class ScrollView extends CB_View_Base {

    public ScrollView(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public ScrollView(CB_RectF cb_RectF, GL_View_Base Parent, String Name) {
        super(cb_RectF, Parent, Name);
    }

}
