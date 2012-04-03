package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ListViewBase extends CB_View_Base
{
	protected float mLastDragedDistance = 0;
	private float mAnimationTarget = 0;
	private Timer mAnimationTimer;
	private long ANIMATION_TICK = 50;
	protected Boolean mBottomAnimation = false;
	protected int mSelectedIndex = -1;
	protected float firstItemSize = -1;
	protected float lastItemSize = -1;

	protected boolean isTouch = false;

	/**
	 * Wen True, können die Items verschoben werden
	 */
	protected Boolean mIsDrageble = true;

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

	protected int mLastIndex = 0;

	/**
	 * Die Anzahl der Items, welche gleichzeitig dargestellt werden kann, wenn alle Items so Groß sind wie das kleinste Item in der List.
	 */
	protected int mMaxItemCount = 1;

	protected float minimumItemSize = 0;

	/**
	 * Komplette Breite oder Höhe aller Items
	 */
	protected float mAllSize = 0f;

	/**
	 * Abstand zwichen zwei Items
	 */
	protected float mDividerSize = 2f;

	protected boolean mMustSetPosKinetic = false;
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

		mAddeedIndexList.clear();
		if (mCanDispose)
		{
			synchronized (childs)
			{
				for (GL_View_Base v : childs)
				{
					v.dispose();
				}
			}
		}
		this.removeChilds();

		if (mBaseAdapter != null)
		{
			calcDefaultPosList();

			// Items neu laden
			reloadItems();

			// set first and Last Item Size
			firstItemSize = mBaseAdapter.getItemSize(0);
			lastItemSize = mBaseAdapter.getItemSize(mBaseAdapter.getCount() - 1);
		}

	}

	/**
	 * Stelt den Abstand zwichen zwei Items ein
	 * 
	 * @param value
	 */
	public void setDividerSize(float value)
	{
		mDividerSize = value;
		calcDefaultPosList();

		// Items neu laden
		reloadItems();

	}

	protected boolean mReloadItems = false;

	private void reloadItems()
	{
		mReloadItems = true;

		// Position setzen, damit die items neu geladen werden
		setListPos(mPos, false);
	}

	/**
	 * Setzt die ListView in in den unDrageble Modus
	 */
	public void setUndragable()
	{
		mIsDrageble = false;
	}

	/**
	 * Setzt die ListView in in den Drageble Modus
	 */
	public void setDragable()
	{
		mIsDrageble = false;
	}

	public void setDisposeFlag(Boolean CanDispose)
	{
		mCanDispose = CanDispose;
	}

	protected void setListPos(float value, boolean Kinetic)
	{
		mMustSetPosValue = value;
		mMustSetPos = true;
		mMustSetPosKinetic = Kinetic;
		GL_Listener.glListener.renderOnce(this);
	}

	protected abstract void RenderThreadSetPos(float value, boolean Kinetic);

	/**
	 * added die sichtbaren Items als Child und speichert den Index in einer Liste, damit das Item nicht ein zweites mal hinzugefügt wird.
	 * Wenn Kinetic == True werden mehr Items geladen, damit beim schnellen Scrollen die Items schon erstellt sind, bevor sie in den
	 * sichtbaren Bereich kommen.
	 * 
	 * @param Kinetic
	 */
	protected abstract void addVisibleItems(boolean Kinetic);

	/**
	 * Fragt die Höhen aller Items ab und speichert die damit berechneten Positonen ab.
	 */
	protected abstract void calcDefaultPosList();

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isTouch = false;
		chkSlideBack();
		return true;
	}

	// Debug FontCaches
	BitmapFontCache dPosy;
	BitmapFontCache dDraged;
	BitmapFontCache dFirstIndex;
	BitmapFontCache dChildCount;
	BitmapFontCache dFPS;

	@Override
	protected void render(SpriteBatch batch)
	{

		if (mMustSetPos) RenderThreadSetPos(mMustSetPosValue, mMustSetPosKinetic);

	}

	NinePatch debugBack = new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_first"), 8, 8, 8, 8);

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{
		super.renderChilds(batch, parentInfo);

		if (!debug) return;
		// schreibe Debug
		if (dPosy == null)
		{
			dPosy = new BitmapFontCache(Fonts.getSmall());
			dDraged = new BitmapFontCache(Fonts.getSmall());
			dFirstIndex = new BitmapFontCache(Fonts.getSmall());
			dChildCount = new BitmapFontCache(Fonts.getSmall());
			dFPS = new BitmapFontCache(Fonts.getSmall());
		}

		dFPS.setText("FPS:  " + Gdx.graphics.getFramesPerSecond(), 220, 140);

		dChildCount.setText("ChildCount: " + childs.size(), 220, 115);

		dPosy.setText("PosY= " + mPos, 220, 100);
		dDraged.setText("Draged " + mLastDragedDistance, 220, 85);
		dFirstIndex.setText("Index " + mFirstIndex + "-" + mLastIndex, 220, 70);

		batch.begin();

		debugBack.draw(batch, 210, 50, 150, 100);

		dPosy.draw(batch);
		dDraged.draw(batch);
		dFirstIndex.draw(batch);
		dChildCount.draw(batch);
		dFPS.draw(batch);

		batch.end();
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

	public abstract boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan);

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// isTouch = true;
		return true;
	}

	protected void startAnimationtoTop()
	{
		if (mBaseAdapter == null) return;
		mBottomAnimation = false;
		scrollTo(0);
	}

	private void startAnimationToBottom()
	{
		if (mBaseAdapter == null) return;
		mBottomAnimation = true;
		scrollTo(mAllSize);
	}

	protected void scrollTo(float Pos)
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
					// Logger.LogCat("Animation Snapin");
					setListPos(mAnimationTarget, true);
					stopTimer();
					return;
				}

				setListPos(newPos, true);
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

	public float getDividerHeight()
	{
		return mDividerSize;
	}

	protected void setSelection(int i)
	{
		if (mSelectedIndex != i && i > 0)
		{
			mSelectedIndex = i;

			// alle Items löschen, damit das Selection flag neu gesetzt werden kann.
			reloadItems();
		}

	}

	protected int getLastVisiblePosition()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	protected int getFirstVisiblePosition()
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
