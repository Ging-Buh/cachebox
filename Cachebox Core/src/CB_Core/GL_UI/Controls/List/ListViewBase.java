package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ListViewBase extends CB_View_Base
{

	private float mAnimationTarget = 0;
	private Timer mAnimationTimer;
	private long ANIMATION_TICK = 50;
	private Boolean mBottomAnimation = false;

	/**
	 * Ermöglicht den Zugriff auf die Liste, welche Dargestellt werden soll.
	 */
	protected Adapter mBaseAdapter;

	/**
	 * Enthällt die Indexes, welche schon als Child exestieren.
	 */
	ArrayList<Integer> mAddeedIndexList = new ArrayList<Integer>();

	/**
	 * Aktuelle Position der Liste
	 */
	protected float mPos = 0;

	/**
	 * Der Start Index, ab dem gesucht wird, ob ein Item in den Sichtbaren Bereich geschoben wurde. Damit nicht eine Liste von 1000 Items
	 * abgefragt werden muss wenn nur die letzten 5 sichtbar sind.
	 */
	protected int mFirstIndex = 0;

	/**
	 * Die Anzahl der Items, welche gleichzeitig dargestellt werden kann, wenn alle Items so Groß sind wie das kleinste Item in der List.
	 */
	protected int mMaxItemCount = 1;

	/**
	 * Komplette Breite oder Höhe aller Items
	 */
	protected float mAllSize = 0;

	protected boolean mMustSetPos = false;
	protected float mMustSetPosValue = 0;
	protected ArrayList<Float> mPosDefault;

	/**
	 * Wenn True, werden die Items beim verlassen des sichtbaren Bereiches Disposed und auf NULL gesetzt.
	 */
	protected Boolean mCanDispose = true;

	protected int mDraged = 0;
	protected int mLastTouch = 0;
	protected float mLastPos_onTouch = 0;

	public ListViewBase(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		isClickable = true;
	}

	public void setBaseAdapter(Adapter adapter)
	{
		mBaseAdapter = adapter;
		addVisibleItems();
	}

	public void setDisposeFlag(Boolean CanDispose)
	{
		mCanDispose = CanDispose;
	}

	protected void setPos(float value)
	{
		mMustSetPosValue = value;
		mMustSetPos = true;
		GL_Listener.glListener.renderOnce(this);
	}

	protected abstract void RenderThreadSetPos(float value);

	/**
	 * added die sichtbaren Items als Child und speichert den Index in einer Liste, damit das Item nicht ein zweites mal hinzugefügt wird.
	 */
	protected abstract void addVisibleItems();

	/**
	 * Fragt die Höhen aller Items ab und speichert die damit berechneten Positonen ab.
	 */
	protected abstract void calcDefaultPosList();

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		chkSlideBack();
		return true;
	}

	// Debug FontCaches
	// BitmapFontCache dPosy;
	// BitmapFontCache dDraged;
	// BitmapFontCache dFirstIndex;

	@Override
	protected void render(SpriteBatch batch)
	{

		if (mMustSetPos) RenderThreadSetPos(mMustSetPosValue);

		// schreibe Debug
		// if (dPosy == null)
		// {
		// dPosy = new BitmapFontCache(Fonts.get11());
		// dDraged = new BitmapFontCache(Fonts.get11());
		// dFirstIndex = new BitmapFontCache(Fonts.get11());
		//
		// }
		// dPosy.setText("PosY= " + mPos, 220, 100);
		// dDraged.setText("Draged " + mDraged, 220, 85);
		// dFirstIndex.setText("First Index " + mFirstIndex, 220, 70);
		//
		// dPosy.draw(batch);
		// dDraged.draw(batch);
		// dFirstIndex.draw(batch);

	}

	/**
	 * Überprüft ob die Liste oben oder unten Platz hat und lösst eine Animation aus, in der die Liste auf die erste oder letzte Position
	 * scrollt.
	 */
	private void chkSlideBack()
	{
		if (mPos > 0) startAnimationtoTop();
		else if (mPos < mAllSize) startAnimationToBottom();
	}

	public abstract boolean onTouchDragged(int x, int y, int pointer);

	public abstract boolean onTouchDown(int x, int y, int pointer, int button);

	private void startAnimationtoTop()
	{
		mBottomAnimation = false;
		scrollTo(0);
	}

	private void startAnimationToBottom()
	{
		mBottomAnimation = true;
		scrollTo(mAllSize);
	}

	private void scrollTo(float Pos)
	{

		Logger.LogCat("Scroll TO:" + Pos);

		mAnimationTarget = Pos;
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
				float newPos = mPos - ((mPos - mAnimationTarget) / 2);
				if ((!mBottomAnimation && mAnimationTarget + 1.5 > mPos) || (mBottomAnimation && mAnimationTarget - 1.5 < mPos))
				{
					Logger.LogCat("Animation Snapin");
					setPos(mAnimationTarget);
					stopTimer();
					return;
				}

				setPos(newPos);
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

}
