package CB_UI_Base.GL_UI.Controls.Animation;

import java.util.Timer;
import java.util.TimerTask;

import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;

/**
 * class to calculate alpha for Fade In/Out
 * 
 * @author Longri
 */
public class Fader
{
	/**
	 * Default time to start the FadeOut Animation.<br>
	 * default value: 13 sec
	 */
	public static int DEFAULT_TIME_TO_FADE_OUT = 7000;
	public static int DEFAULT_FADE_OUT_TIME = 1200;
	public static int DEFAULT_FADE_IN_TIME = 700;

	private float mFadeValue = 1f;
	private int mTimeToFadeOut = DEFAULT_TIME_TO_FADE_OUT;
	private Timer mTimer;
	private boolean mFadeOut = false;
	private boolean mFadeIn = false;
	private boolean mVirtualVisible = true;
	private float mFadeOutTime = DEFAULT_FADE_OUT_TIME;
	private float mFadeInTime = DEFAULT_FADE_IN_TIME;
	private float mFadeoutBeginntime = 0;

	/**
	 * Constructor!
	 * 
	 * @param view
	 *            how implemented this Fader
	 */
	public Fader(GL_View_Base view)
	{
		resetFadeOut();
	}

	/**
	 * Returns the actual Fade Value
	 * 
	 * @return
	 */
	public float getValue()
	{
		calcFade();
		if (mFadeValue < 0)
		{
			mFadeOut = false;
			mFadeValue = 0;
		}
		if (mFadeValue > 1)
		{
			mFadeIn = false;
			mFadeValue = 1;
		}
		if (mFadeOut || mFadeIn)
		{
			if (this.isVisible()) GL.that.renderOnce();
		}
		return mFadeValue;
	}

	/**
	 * Returns TRUE if alpha > 0
	 * 
	 * @return
	 */
	public boolean isVisible()
	{
		return mVirtualVisible;
	}

	/**
	 * Starts the FadeOut Animation
	 */
	public void beginnFadeout()
	{
		cancelTimerToFadeOut();
		mFadeoutBeginntime = GL.that.getStateTime() * 1000;
		mFadeOut = true;
		GL.that.renderOnce();
	}

	/**
	 * Set the time to begin fade out after last resetFadeOut() call.<br>
	 * default= DEFAULT_TIME_TO_FADE_OUT
	 * 
	 * @param time
	 *            in msec
	 */
	public void setTimeToFadeOut(int time)
	{
		mTimeToFadeOut = time;
	}

	/**
	 * Set the time defines the duration of the FadeIn animation!
	 * 
	 * @param time
	 *            in msec
	 */
	public void setFadeInTime(int time)
	{
		mFadeInTime = time;
	}

	/**
	 * Set the time defines the duration of the FadeIn animation!
	 * 
	 * @param time
	 *            in msec
	 */
	public void setFadeOutTime(int time)
	{
		mFadeOutTime = time;
	}

	/**
	 * Set Fade to 1f;<br>
	 * Restart the timer to begin FadeOut
	 */
	public void resetFadeOut()
	{

		// Gdx.app.debug(Tag.TAG,"Reset Fade Out");
		if (mFadeIn && !mFadeOut)
		{
			mFadeIn = false;
			mFadeValue = 1.0f;
		}
		else if (!mVirtualVisible)
		{
			mVirtualVisible = true;
			mFadeIn = true;
			mFadeValue = 0f;
		}
		if (mFadeOut)
		{
			mFadeOut = false;
			mFadeValue = 1.0f;
		}

		startTimerToFadeOut();
	}

	private void calcFade()
	{
		float calcedFadeValue = 0;
		float statetime = (GL.that.getStateTime() * 1000) - mFadeoutBeginntime;

		if (mFadeIn)
		{
			calcedFadeValue = (1 + ((statetime) % this.mFadeInTime) / (this.mFadeInTime / 1000)) / 1000;

			if (Float.isInfinite(calcedFadeValue) || Float.isNaN(calcedFadeValue)) return;
			if (calcedFadeValue < mFadeValue)
			{
				// fading finish
				mFadeValue = 1f;
				mFadeIn = false;
				mVirtualVisible = true;
				// Gdx.app.debug(Tag.TAG,"[" + statetime + "]finish FadeIn" + " calcvalue:" + calcedFadeValue);
			}
			else
			{
				mFadeValue = calcedFadeValue;
				// Gdx.app.debug(Tag.TAG,"[" + statetime + "]fadeIn:" + mFadeValue);
			}
		}
		else if (mFadeOut)
		{

			calcedFadeValue = (1000 - (1 + ((statetime) % this.mFadeOutTime) / (this.mFadeOutTime / 1000))) / 1000;
			if (Float.isInfinite(calcedFadeValue) || Float.isNaN(calcedFadeValue)) return;
			if (calcedFadeValue > mFadeValue)
			{
				// fading finish
				mFadeValue = 0f;
				mFadeOut = false;
				mVirtualVisible = false;
				// Gdx.app.debug(Tag.TAG,"[" + statetime + "]finish FadeOut" + " calcvalue:" + calcedFadeValue);
			}
			else
			{
				mFadeValue = calcedFadeValue;
				// Gdx.app.debug(Tag.TAG,"[" + statetime + "]fadeOut:" + mFadeValue);
			}
		}
	}

	private void startTimerToFadeOut()
	{
		cancelTimerToFadeOut();

		mTimer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				beginnFadeout();
			}
		};
		mTimer.schedule(task, mTimeToFadeOut);
	}

	private void cancelTimerToFadeOut()
	{

		if (mTimer != null)
		{
			mTimer.cancel();
			mTimer = null;
		}
	}

	public void dispose()
	{
		if (mTimer != null) cancelTimerToFadeOut();

	}

	/**
	 * Stop the timer to FadeOut
	 */
	public void stopTimer()
	{
		cancelTimerToFadeOut();
	}
}
