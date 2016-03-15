package CB_UI.GL_UI.Controls.PopUps;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

public class QuickFieldNoteFeedbackPopUp extends PopUp_Base {

	public QuickFieldNoteFeedbackPopUp(boolean found) {
		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 2.5f, UI_Size_Base.that.getButtonWidth() * 2.5f), "QuickFieldnoteFeedback");

		if (found) {
			setBackground(new SpriteDrawable(Sprites.LogIcons.get(0)));
		} else {
			setBackground(new SpriteDrawable(Sprites.LogIcons.get(1)));
		}

		AnimateTimer = new Timer();

		AnimateTimer.schedule(AnimateTimertask, 40, 40);

	}

	@Override
	public void Initial() {
	}

	@Override
	protected void SkinIsChanged() {

	}

	@Override
	public void dispose() {
		setBackground(null);
		if (AnimateTimer != null)
			AnimateTimer.cancel();
		AnimateTimer = null;
		super.dispose();
	}

	Timer AnimateTimer;
	int counter = 0;
	boolean toSmall = true;
	TimerTask AnimateTimertask = new TimerTask() {
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
					QuickFieldNoteFeedbackPopUp.this.setRec(QuickFieldNoteFeedbackPopUp.this.ScaleCenter(0.9f));
					counter--;
				}
			} else {
				if (counter > 0) {
					toSmall = true;
				} else {
					QuickFieldNoteFeedbackPopUp.this.setRec(QuickFieldNoteFeedbackPopUp.this.ScaleCenter(1.1111f));
					counter++;
				}
			}
			GL.that.renderOnce();

		}
	};

}
