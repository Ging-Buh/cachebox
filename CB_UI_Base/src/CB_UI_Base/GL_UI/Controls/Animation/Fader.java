package CB_UI_Base.GL_UI.Controls.Animation;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.GL_Listener.GL;

/**
 * class to calculate alpha for Fade In/Out
 * 
 * @author Longri
 */
public class Fader {

    final static org.slf4j.Logger log = LoggerFactory.getLogger(Fader.class);

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
    private final String name;
    private boolean alwaysOn = false;

    /**
     * Constructor!
      */
    public Fader(String name) {
	this.name = name;
	resetFadeOut();
    }

    float lastRender = 0;
    float lastFadeValue = -1;

    /**
     * Returns the actual Fade Value
     * 
     * @return
     */
    public float getValue() {
	if (this.alwaysOn)
	    return 1f;

	calcFade();
	if (mFadeValue < 0) {
	    mFadeOut = false;
	    mFadeValue = 0;
	}
	if (mFadeValue > 1) {
	    mFadeIn = false;
	    mFadeValue = 1;
	}

	if (mFadeIn || mFadeOut) {
	    GL.that.renderOnce();
	}

	return mFadeValue;
    }

    /**
     * Returns TRUE if alpha > 0
     * 
     * @return
     */
    public boolean isVisible() {
	if (this.alwaysOn)
	    return true;
	return mVirtualVisible;
    }

    /**
     * Starts the FadeOut Animation
     */
    public void beginnFadeout() {
	if (this.alwaysOn)
	    return;
	cancelTimerToFadeOut();
	mFadeoutBeginntime = GL.that.getStateTime() * 1000;
	mFadeOut = true;
	log.debug("beginn fade out =>" + name);
	GL.that.renderOnce(true);
    }

    /**
     * Set the time to begin fade out after last resetFadeOut() call.<br>
     * default= DEFAULT_TIME_TO_FADE_OUT
     * 
     * @param time
     *            in msec
     */
    public void setTimeToFadeOut(int time) {
	mTimeToFadeOut = time;
    }

    /**
     * Set the time defines the duration of the FadeIn animation!
     * 
     * @param time
     *            in msec
     */
    public void setFadeInTime(int time) {
	mFadeInTime = time;
    }

    /**
     * Set the time defines the duration of the FadeIn animation!
     * 
     * @param time
     *            in msec
     */
    public void setFadeOutTime(int time) {
	mFadeOutTime = time;
    }

    /**
     * Set Fade to 1f;<br>
     * Restart the timer to begin FadeOut
     */
    public void resetFadeOut() {
	if (this.alwaysOn)
	    return;
	log.debug("reset fade out =>" + name);
	if (mFadeIn && !mFadeOut) {
	    mFadeIn = false;
	    mFadeValue = 1.0f;
	} else if (!mVirtualVisible) {
	    mVirtualVisible = true;
	    mFadeIn = true;
	    mFadeValue = 0f;
	}
	if (mFadeOut) {
	    mFadeOut = false;
	    mFadeValue = 1.0f;
	}

	if (!mFadeIn) {
	    mFadeIn = false;
	    mFadeValue = 1.0f;
	}

	startTimerToFadeOut();
	GL.that.renderOnce(true);
    }

    private void calcFade() {
	float calcedFadeValue = 0;
	float statetime = (GL.that.getStateTime() * 1000) - mFadeoutBeginntime;

	if (mFadeIn) {
	    calcedFadeValue = (1 + ((statetime) % this.mFadeInTime) / (this.mFadeInTime / 1000)) / 1000;

	    if (Float.isInfinite(calcedFadeValue) || Float.isNaN(calcedFadeValue))
		return;
	    if (calcedFadeValue < mFadeValue) {
		// fading finish
		mFadeValue = 1f;
		mFadeIn = false;
		mVirtualVisible = true;
		// log.debug("[" + statetime + "]finish FadeIn" + " calcvalue:" + calcedFadeValue);
	    } else {
		mFadeValue = calcedFadeValue;
		// log.debug("[" + statetime + "]fadeIn:" + mFadeValue);
	    }
	} else if (mFadeOut) {

	    calcedFadeValue = (1000 - (1 + ((statetime) % this.mFadeOutTime) / (this.mFadeOutTime / 1000))) / 1000;
	    if (Float.isInfinite(calcedFadeValue) || Float.isNaN(calcedFadeValue))
		return;
	    if (calcedFadeValue > mFadeValue) {
		// fading finish
		mFadeValue = 0f;
		mFadeOut = false;
		mVirtualVisible = false;
		// log.debug("[" + statetime + "]finish FadeOut" + " calcvalue:" + calcedFadeValue);
	    } else {
		mFadeValue = calcedFadeValue;
		// log.debug("[" + statetime + "]fadeOut:" + mFadeValue);
	    }
	}
    }

    private void startTimerToFadeOut() {
	cancelTimerToFadeOut();

	if (this.alwaysOn)
	    return;
	log.debug("Start Timer to fade out =>" + Integer.toString(mTimeToFadeOut) + name);

	mTimer = new Timer();
	TimerTask task = new TimerTask() {
	    @Override
	    public void run() {
		beginnFadeout();
	    }
	};
	mTimer.schedule(task, mTimeToFadeOut);
    }

    private void cancelTimerToFadeOut() {

	if (mTimer != null) {
	    mTimer.cancel();
	    mTimer = null;
	}
    }

    public void dispose() {
	if (mTimer != null)
	    cancelTimerToFadeOut();

    }

    /**
     * Stop the timer to FadeOut
     */
    public void stopTimer() {
	cancelTimerToFadeOut();
    }

    public void setAlwaysOn(boolean value) {
	this.alwaysOn = value;
    }
}
