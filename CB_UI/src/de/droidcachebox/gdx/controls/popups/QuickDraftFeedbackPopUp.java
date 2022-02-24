package de.droidcachebox.gdx.controls.popups;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class QuickDraftFeedbackPopUp extends PopUpBase {

    int counter;
    boolean tooSmall;
    TimerTask timerTask;

    /**
     * lets the corresponding image expand and shrink after a delay for a period
     *
     * @param found if or not
     */
    public QuickDraftFeedbackPopUp(boolean found) {
        super(new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight() * 2.5f), "QuickDraftFeedbackPopUp");
        counter = 0;
        tooSmall = true;
        if (found) {
            setBackground(new SpriteDrawable(Sprites.LogIcons.get(0)));
        } else {
            setBackground(new SpriteDrawable(Sprites.LogIcons.get(1)));
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (tooSmall) {
                    if (counter < -5) {
                        tooSmall = false;
                    } else {
                        setRec(scaleCenter(0.9f));
                        counter--;
                    }
                } else {
                    if (counter > 0) {
                        tooSmall = true;
                    } else {
                        setRec(scaleCenter(1.1111f));
                        counter++;
                    }
                }
                GL.that.renderOnce();
            }
        };
    }

    @Override
    public void dispose() {
        // do not remove: the timerTask must come to an end
    }

    @Override
    public void onShow() {
        try {
            new Timer().schedule(timerTask, 40, 40);
        } catch (Exception ignored) {
        }
    }

}
