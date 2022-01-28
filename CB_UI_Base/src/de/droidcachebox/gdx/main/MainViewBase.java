package de.droidcachebox.gdx.main;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;

public class MainViewBase extends CB_View_Base {
    public static MainViewBase mainView;

    public MainViewBase(CB_RectF rec) {
        super(rec, "MainView");
        mainView = this;
    }

    public void requestLayout() {
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return true;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        return true;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return true;
    }

}
