package de.droidcachebox.gdx.controls.popups;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

import java.util.Timer;
import java.util.TimerTask;

public class QuickDraftFeedbackPopUp extends PopUp_Base {

    Timer AnimateTimer;
    int counter;
    boolean toSmall;
    TimerTask AnimateTimertask;

    public QuickDraftFeedbackPopUp(boolean found) {
        super(new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight() * 2.5f), "QuickDraftFeedbackPopUp");
        counter = 0;
        toSmall = true;
        if (found) {
            setBackground(new SpriteDrawable(Sprites.LogIcons.get(0)));
        } else {
            setBackground(new SpriteDrawable(Sprites.LogIcons.get(1)));
        }

        AnimateTimer = new Timer();

        AnimateTimertask = new TimerTask() {
            @Override
            public void run() {

                if (isDisposed()) {
                    GL.that.renderOnce();
                    return;
                }

                if (toSmall) {
                    if (counter < -5) {
                        toSmall = false;
                    } else {
                        setRec(scaleCenter(0.9f));
                        counter--;
                    }
                } else {
                    if (counter > 0) {
                        toSmall = true;
                    } else {
                        setRec(scaleCenter(1.1111f));
                        counter++;
                    }
                }
                GL.that.renderOnce();

            }
        };
        try {
            AnimateTimer.schedule(AnimateTimertask, 40, 40);
        } catch (Exception e) {
        }
    }

    @Override
    public void dispose() {
        setBackground(null);
        if (AnimateTimer != null)
            AnimateTimer.cancel();
        AnimateTimer = null;
        super.dispose();
    }

}
