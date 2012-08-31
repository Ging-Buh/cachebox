package CB_Core.GL_UI.Controls;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;

public class CollabseBox extends Box
{
	private CollabseBox that;

	private float maxHeight = -1;
	private boolean collabse = false;

	private float mAnimationTarget = 0;
	private Timer mAnimationTimer;
	private long ANIMATION_TICK = 50;
	private boolean collabseAnimation = false;

	private animatetHeightChangedListner listner;

	public interface animatetHeightChangedListner
	{
		public void animatetHeightCanged(float Height);
	}

	public CollabseBox(CB_RectF rec, String Name)
	{
		super(rec, Name);
		maxHeight = rec.getHeight();
		that = this;
	}

	public void Toggle()
	{
		if (collabse)
		{
			expand();
		}
		else
		{
			collabse();
		}
	}

	public void collabse()
	{
		if (collabse) return;
		collabseAnimation = true;
		animateTo(0);
	}

	public void expand()
	{
		if (!collabse) return;
		collabseAnimation = false;
		animateTo(maxHeight);
	}

	public void setAnimationListner(animatetHeightChangedListner listner)
	{
		this.listner = listner;
	}

	protected void animateTo(float Height)
	{

		mAnimationTarget = Height;
		stopTimer();

		mAnimationTimer = new Timer();
		mAnimationTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				TimerMethod();
			}

			private void TimerMethod()
			{
				float newPos = that.getHeight() - ((that.getHeight() - mAnimationTarget) / 2);
				if ((collabseAnimation && mAnimationTarget + 1.5 > that.getHeight())
						|| (!collabseAnimation && mAnimationTarget - 1.5 < that.getHeight()))
				{
					setAnimationHeight(mAnimationTarget);
					stopTimer();
					collabse = (mAnimationTarget == 0) ? true : false;
					return;
				}

				setAnimationHeight(newPos);
			}

		}, 0, ANIMATION_TICK);
	}

	private void stopTimer()
	{
		if (mAnimationTimer != null)
		{
			mAnimationTimer.cancel();
			mAnimationTimer = null;
		}
	}

	@Override
	public void setHeight(float Height)
	{
		super.setHeight(Height);
		maxHeight = Height;
	}

	public void setAnimationHeight(float Height)
	{
		super.setHeight(Height);
		collabse = (Height == 0);
		if (listner != null) listner.animatetHeightCanged(Height);
		GL.that.renderOnce("CollabseBox.setAnimationHeight");
	}

}
