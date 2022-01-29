package de.droidcachebox.gdx.controls.popups;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

/**
 * Popup based on CB_View_Base
 *
 * @author Longri
 */
public abstract class PopUp_Base extends CB_View_Base {
    public static final int SHOW_TIME_NEVER_CLOSE = -1;
    public static final int SHOW_TIME_NORMAL = 4000;
    public static final int SHOW_TIME_SHORT = 2000;

    public PopUp_Base(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public void show(int msec) {
        float x = (UiSizes.getInstance().getWindowWidth() >> 1) - getHalfWidth();
        float y = (UiSizes.getInstance().getWindowHeight() >> 1) - getHalfHeight();
        show(x, y, msec);
    }

    public void show() {
        float x = (UiSizes.getInstance().getWindowWidth() >> 1) - getHalfWidth();
        float y = (UiSizes.getInstance().getWindowHeight() >> 1) - getHalfHeight();
        show(x, y, SHOW_TIME_NORMAL);
    }

    public void show(float x, float y) {
        show(x, y, SHOW_TIME_NORMAL);
    }

    public void showNotCloseAutomaticly(float x, float y) {
        show(x, y, SHOW_TIME_NEVER_CLOSE);
    }

    public void showNotCloseAutomaticly() {
        show(this.getX(), this.getY(), SHOW_TIME_NEVER_CLOSE);
    }

    public void show(float x, float y, int msec) {
        GL.that.showPopUp(this, x, y);
        if (msec != SHOW_TIME_NEVER_CLOSE)
            startCloseTimer(msec);
    }

    public void close() {
        GL.that.closePopUp(this);
    }

    private void startCloseTimer(int msec) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                close();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, msec);

    }

}
