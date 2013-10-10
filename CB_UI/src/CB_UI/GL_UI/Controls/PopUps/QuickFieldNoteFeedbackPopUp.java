package CB_UI.GL_UI.Controls.PopUps;

import java.util.Timer;
import java.util.TimerTask;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.PopUps.PopUp_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class QuickFieldNoteFeedbackPopUp extends PopUp_Base
{

	public QuickFieldNoteFeedbackPopUp(boolean found)
	{
		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 2.5f, UI_Size_Base.that.getButtonWidth() * 2.5f),
				"QuickFieldnoteFeedback");

		if (found)
		{
			setBackground(new SpriteDrawable(SpriteCacheBase.LogIcons.get(0)));
		}
		else
		{
			setBackground(new SpriteDrawable(SpriteCacheBase.LogIcons.get(1)));
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
