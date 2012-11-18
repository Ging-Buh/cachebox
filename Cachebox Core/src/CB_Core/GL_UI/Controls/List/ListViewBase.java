package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
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
	protected boolean hasInvisibleItems = false;
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
	protected int mMaxItemCount = -1;

	protected float minimumItemSize = 0;

	protected float mcalcAllSizeBase = 0f;

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

	protected String mEmptyMsg = null;
	protected BitmapFontCache emptyMsg;

	public void setEmptyMsg(String Msg)
	{
		mEmptyMsg = Msg;
		emptyMsg = null;
		GL.that.renderOnce("ListView.setEmptyMsg");
	}

	/**
	 * Setzt ein Flag, welches angibt, ob dies ListView Invisible Items hat. Da die Berechnung der Positionen deutlich länger dauert, ist
	 * der Standard auf False gesetzt.
	 * 
	 * @param value
	 */
	public void setHasInvisibleItems(Boolean value)
	{
		hasInvisibleItems = value;
	}

	public ListViewBase(CB_RectF rec, String Name)
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
				try
				{
					for (GL_View_Base v : childs)
					{
						v.dispose();
					}
				}
				catch (ConcurrentModificationException e)
				{
					// Dann Disposen wir halt nicht, dann muss der GC ran!
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

	public void reloadItems()
	{
		mReloadItems = true;

		// Position setzen, damit die items neu geladen werden
		setListPos(mPos, false);

		GL.that.renderOnce("");

	}

	/**
	 * Setzt die ListView in in den unDrageble Modus
	 */
	public void setUndragable()
	{
		mPos = 0;
		mIsDrageble = false;
	}

	/**
	 * Setzt die ListView in in den Drageble Modus
	 */
	public void setDragable()
	{
		mIsDrageble = true;
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
		GL.that.renderOnce(this.getName() + " setListPos");
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
	public void onRezised(CB_RectF rec)
	{
		// setBaseAdapter(mBaseAdapter);

		// Items neu laden
		calcDefaultPosList();
		reloadItems();
	}

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

		if (!isInitial)
		{
			isInitial = true;
			Initial();
			return;
		}

		if (this.mBaseAdapter == null || this.mBaseAdapter.getCount() == 0)
		{
			if (emptyMsg == null && mEmptyMsg != null)
			{
				emptyMsg = new BitmapFontCache(Fonts.getBig());
				TextBounds bounds = emptyMsg.setText(mEmptyMsg, 0, 0);
				emptyMsg.setPosition(this.halfWidth - (bounds.width / 2), this.halfHeight - (bounds.height / 2));
			}
			if (emptyMsg != null) emptyMsg.draw(batch, 0.5f);
		}
		else
		{
			try
			{
				super.render(batch);
				if (mMustSetPos) RenderThreadSetPos(mMustSetPosValue, mMustSetPosKinetic);
			}
			catch (Exception e)
			{
				// e.printStackTrace();
			}
		}

	}

	NinePatch debugBack = new NinePatch(SpriteCache.getThemedSprite("listrec-first"), 8, 8, 8, 8);

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
	public void chkSlideBack()
	{
		// Logger.LogCat("chkSlideBack()");
		if (!mIsDrageble)
		{
			// startAnimationtoTop();
			return;
		}
		if (mPos > 0) startAnimationtoTop();
		else if (mPos < mcalcAllSizeBase) startAnimationToBottom();
	}

	public boolean isDrageble()
	{
		return mIsDrageble;
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
		scrollTo(mcalcAllSizeBase);
	}

	public void scrollToItem(int i)
	{
		if (mPosDefault == null) return;

		if (i < getMaxItemCount()) i = getMaxItemCount();

		if (i >= 0 && i < mPosDefault.size()) setListPos(mPosDefault.get(i), false);
	}

	public void scrollTo(float Pos)
	{

		// Logger.LogCat("Scroll TO:" + Pos);

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

	protected boolean selectionchanged = false;

	public ListViewItemBase getSelectedItem()
	{
		if (mBaseAdapter == null) return null;
		if (mSelectedIndex == -1) return null;
		if (mSelectedIndex >= mBaseAdapter.getCount()) return null;
		return mBaseAdapter.getView(mSelectedIndex);
	}

	public int getSelectedIndex()
	{
		return mSelectedIndex;
	}

	public void setSelection(int i)
	{
		if (mSelectedIndex != i && i >= 0)
		{
			selectionchanged = true;
			synchronized (childs)
			{
				for (GL_View_Base v : childs)
				{
					if (v instanceof ListViewItemBase)
					{
						if (((ListViewItemBase) v).getIndex() == mSelectedIndex)
						{
							((ListViewItemBase) v).isSelected = false;
							break;
						}
					}
				}
				mSelectedIndex = i;
				for (GL_View_Base v : childs)
				{
					if (v instanceof ListViewItemBase)
					{
						if (((ListViewItemBase) v).getIndex() == mSelectedIndex)
						{
							((ListViewItemBase) v).isSelected = true;
							break;
						}
					}
				}

				// alle Items löschen, damit das Selection flag neu gesetzt werden kann.
				if (childs.size() == 0)
				{
					reloadItems();
				}
			}
			GL.that.renderOnce(this.getName() + " setListPos");

		}

	}

	public int getLastVisiblePosition()
	{
		int ret = 0;
		boolean help = false;
		synchronized (childs)
		{
			for (GL_View_Base v : childs)
			{
				if (!help && this.contains(v)) help = true;
				if (help && !this.contains(v) && v instanceof ListViewItemBase)
				{
					ret = ((ListViewItemBase) v).getIndex();
					break;
				}
			}
		}

		// Logger.LogCat("getLastVisiblePosition = " + ret);

		return ret;
	}

	public int getFirstVisiblePosition()
	{
		if (mBaseAdapter == null) return 0;
		int ret = mBaseAdapter.getCount();
		synchronized (childs)
		{
			for (GL_View_Base v : childs)
			{
				if (this.contains(v) && v instanceof ListViewItemBase)
				{
					int i = ((ListViewItemBase) v).getIndex();

					if (i < ret) ret = i;
				}
			}

		}

		// Logger.LogCat("getFirstVisiblePosition = " + ret);

		return ret;
	}

	/**
	 * Gibt die Anzahl der Items, welche gleichzeitig dargestellt werden können, wenn alle Items so Groß sind wie das kleinste Item in der
	 * List, zurück.
	 */
	public int getMaxItemCount()
	{
		return mMaxItemCount;
	}

	public abstract void notifyDataSetChanged();

	public float getScrollPos()
	{
		return mPos;
	}

}
