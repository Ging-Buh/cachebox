package de.droidcachebox.gdx.controls.popups;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class QuickDraftFeedbackPopUp extends PopUp_Base {

    int counter;
    boolean toSmall;

    public QuickDraftFeedbackPopUp(boolean found) {
        super(new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight() * 2.5f), "QuickDraftFeedbackPopUp");
        counter = 0;
        toSmall = true;
        if (found) {
            setBackground(new SpriteDrawable(Sprites.LogIcons.get(0)));
        } else {
            setBackground(new SpriteDrawable(Sprites.LogIcons.get(1)));
        }

        try {
            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {

                            if (isDisposed) {
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
                    },
                    40,
                    40);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void dispose() {
        setBackground(null);
        super.dispose();
    }

}
