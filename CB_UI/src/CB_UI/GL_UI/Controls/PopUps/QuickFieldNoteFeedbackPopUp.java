package CB_UI.GL_UI.Controls.PopUps;

import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.Timer;
import java.util.TimerTask;

public class QuickFieldNoteFeedbackPopUp extends PopUp_Base {

    Timer AnimateTimer;
    int counter;
    boolean toSmall;
    TimerTask AnimateTimertask;

    public QuickFieldNoteFeedbackPopUp(boolean found) {
        super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 2.5f, UI_Size_Base.that.getButtonWidth() * 2.5f), "QuickFieldNoteFeedbackPopUp");
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
                        setRec(ScaleCenter(0.9f));
                        counter--;
                    }
                } else {
                    if (counter > 0) {
                        toSmall = true;
                    } else {
                        setRec(ScaleCenter(1.1111f));
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
