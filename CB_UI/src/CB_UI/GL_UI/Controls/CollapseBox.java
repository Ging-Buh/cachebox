package CB_UI.GL_UI.Controls;

import java.util.Timer;
import java.util.TimerTask;

import CB_UI.GL_UI.GL_Listener.GL;
import CB_UI.Math.CB_RectF;

public class CollapseBox extends Box
{
	private CollapseBox that;

	private float maxHeight = -1;
	private boolean collapse = false;

	private float mAnimationTarget = 0;
	private Timer mAnimationTimer;
	private long ANIMATION_TICK = 50;
	private boolean collapseAnimation = false;

	private animatetHeightChangedListner listner;

	public interface animatetHeightChangedListner
	{
		public void animatedHeightChanged(float Height);
	}

	public CollapseBox(CB_RectF rec, String Name)
	{
		super(rec, Name);
		maxHeight = rec.getHeight();
		that = this;
	}

	public void Toggle()
	{
		if (collapse)
		{
			expand();
		}
		else
		{
			collapse();
		}
	}

	public void collapse()
	{
		if (collapse) return;
		collapseAnimation = true;
		animateTo(0);
	}

	public void expand()
	{
		if (!collapse) return;
		collapseAnimation = false;
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
				if ((collapseAnimation && mAnimationTarget + 1.5 > that.getHeight())
						|| (!collapseAnimation && mAnimationTarget - 1.5 < that.getHeight()))
				{
					setAnimationHeight(mAnimationTarget);
					stopTimer();
					collapse = (mAnimationTarget == 0) ? true : false;
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
		collapse = (Height == 0);
		if (listner != null) listner.animatedHeightChanged(Height);
		GL.that.renderOnce("CollapseBox.setAnimationHeight");
	}

}
