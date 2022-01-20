package de.droidcachebox.gdx.controls;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.math.CB_RectF;

public class CollapseBox extends Box {
    private final CollapseBox that;
    private final long ANIMATION_TICK = 50;
    private float maxHeight = -1;
    private boolean collapse = false;
    private float mAnimationTarget = 0;
    private Timer mAnimationTimer;
    private boolean collapseAnimation = false;

    private IAnimatedHeightChangedListener listener;

    public CollapseBox(CB_RectF rec, String Name) {
        super(rec, Name);
        maxHeight = rec.getHeight();
        that = this;
    }

    public void toggle() {
        if (collapse) {
            expand();
        } else {
            collapse();
        }
    }

    public void collapse() {
        if (collapse)
            return;
        collapseAnimation = true;
        animateTo(0);
    }

    public void expand() {
        if (!collapse)
            return;
        collapseAnimation = false;
        animateTo(maxHeight);
    }

    public void setAnimationListener(IAnimatedHeightChangedListener listener) {
        this.listener = listener;
    }

    protected void animateTo(float Height) {

        mAnimationTarget = Height;
        stopTimer();

        mAnimationTimer = new Timer();
        mAnimationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

            private void TimerMethod() {
                float newPos = that.getHeight() - ((that.getHeight() - mAnimationTarget) / 2);
                if ((collapseAnimation && mAnimationTarget + 1.5 > that.getHeight()) || (!collapseAnimation && mAnimationTarget - 1.5 < that.getHeight())) {
                    setAnimationHeight(mAnimationTarget);
                    stopTimer();
                    collapse = (mAnimationTarget == 0) ? true : false;
                    return;
                }

                setAnimationHeight(newPos);
            }

        }, 0, ANIMATION_TICK);
    }

    private void stopTimer() {
        if (mAnimationTimer != null) {
            mAnimationTimer.cancel();
            mAnimationTimer = null;
        }
    }

    @Override
    public void setHeight(float Height) {
        super.setHeight(Height);
        maxHeight = Height;
    }

    public void setAnimationHeight(float Height) {
        super.setHeight(Height);
        collapse = (Height == 0);
        if (listener != null)
            listener.animatedHeightChanged(Height);
        GL.that.renderOnce();
    }

    public interface IAnimatedHeightChangedListener {
        void animatedHeightChanged(float Height);
    }

}
