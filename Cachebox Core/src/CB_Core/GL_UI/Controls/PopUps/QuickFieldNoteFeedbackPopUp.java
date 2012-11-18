package CB_Core.GL_UI.Controls.PopUps;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class QuickFieldNoteFeedbackPopUp extends PopUp_Base
{

	public QuickFieldNoteFeedbackPopUp(boolean found)
	{
		super(new CB_RectF(0, 0, UiSizes.getButtonWidth() * 2.5f, UiSizes.getButtonWidth() * 2.5f), "QuickFieldnoteFeedback");

		if (found)
		{
			setBackground(new SpriteDrawable(SpriteCache.LogIcons.get(0)));
		}
		else
		{
			setBackground(new SpriteDrawable(SpriteCache.LogIcons.get(1)));
		}

		AnimateTimer = new Timer();

		AnimateTimer.schedule(AnimateTimertask, 40, 40);

	}

	@Override
	public void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public void dispose()
	{
		setBackground(null);
		if (AnimateTimer != null) AnimateTimer.cancel();
		AnimateTimer = null;
		super.dispose();
	}

	Timer AnimateTimer;
	int counter = 0;
	boolean toSmall = true;
	TimerTask AnimateTimertask = new TimerTask()
	{
		@Override
		public void run()
		{
			if (toSmall)
			{
				if (counter < -5)
				{
					toSmall = false;
				}
				else
				{
					QuickFieldNoteFeedbackPopUp.this.setRec(QuickFieldNoteFeedbackPopUp.this.ScaleCenter(0.9f));
					counter--;
				}
			}
			else
			{
				if (counter > 0)
				{
					toSmall = true;
				}
				else
				{
					QuickFieldNoteFeedbackPopUp.this.setRec(QuickFieldNoteFeedbackPopUp.this.ScaleCenter(1.1111f));
					counter++;
				}
			}
			GL.that.renderOnce("WaitRotateAni");

		}
	};

}
