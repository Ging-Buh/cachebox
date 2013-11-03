package CB_UI_Base.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.runOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Log.Logger;
import CB_Utils.Math.Point;
import CB_Utils.Util.MoveableList;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ListViewBase extends CB_View_Base implements IScrollbarParent
{
	protected Scrollbar scrollbar;
	protected MoveableList<GL_View_Base> noListChilds = new MoveableList<GL_View_Base>();
	protected float mLastDragedDistance = 0;
	private float mAnimationTarget = 0;
	private Timer mAnimationTimer;
	private final long ANIMATION_TICK = 50;
	protected Boolean mBottomAnimation = false;
	protected int mSelectedIndex = -1;
	protected float firstItemSize = -1;
	protected float lastItemSize = -1;
	protected boolean hasInvisibleItems = false;
	protected boolean isTouch = false;

	protected ArrayList<runOnGL> runOnGL_List = new ArrayList<runOnGL>();
	protected ArrayList<runOnGL> runOnGL_ListWaitpool = new ArrayList<runOnGL>();
	protected AtomicBoolean isWorkOnRunOnGL = new AtomicBoolean(false);

	/**
	 * Return With for horizontal and Height for vertical ListView
	 * 
	 * @return
	 */
	protected abstract float getListViewLength();

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

	public interface IListPosChanged
	{
		public void ListPosChanged();
	}

	private final ArrayList<IListPosChanged> EventHandlerList = new ArrayList<IListPosChanged>();

	public void addListPosChangedEventHandler(IListPosChanged handler)
	{
		if (!EventHandlerList.contains(handler)) EventHandlerList.add(handler);
	}

	public void RunIfListInitial(runOnGL run)
	{

		// if in progress put into pool
		if (isWorkOnRunOnGL.get())
		{
			runOnGL_ListWaitpool.add(run);
			GL.that.renderOnce("RunIfListInitial called");
			return;
		}
		synchronized (runOnGL_List)
		{
			runOnGL_List.add(run);
		}

		GL.that.renderOnce("RunIfListInitial called");
	}

	protected void callListPosChangedEvent()
	{
		for (IListPosChanged handler : EventHandlerList)
		{
			if (handler != null) handler.ListPosChanged();
		}
	}

	public ArrayList<Float> getItemPosList()
	{
		return mPosDefault;
	}

	@Override
	public float getAllListSize()
	{
		return mAllSize;
	}

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
		this.setClickable(true);
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
		// Logger.DEBUG("SetListPos Relod Items");
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

	@Override
	public void setListPos(float value)
	{
		this.setListPos(value, false);
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
	public void onResized(CB_RectF rec)
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
			try
			{
				if (emptyMsg == null && mEmptyMsg != null)
				{
					emptyMsg = new BitmapFontCache(Fonts.getBig());
					TextBounds bounds = emptyMsg.setWrappedText(mEmptyMsg, 0, 0, this.width);
					emptyMsg.setPosition(this.halfWidth - (bounds.width / 2), this.halfHeight - (bounds.height / 2));
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (emptyMsg != null) emptyMsg.draw(batch, 0.5f);
		}
		else
		{
			try
			{
				super.render(batch);
				if (mMustSetPos)
				{
					RenderThreadSetPos(mMustSetPosValue, mMustSetPosKinetic);
				}
				else
				{
					// Run WorkPool
					isWorkOnRunOnGL.set(true);
					synchronized (runOnGL_List)
					{
						if (runOnGL_List.size() > 0)
						{
							for (runOnGL run : runOnGL_List)
							{
								if (run != null) run.run();
							}

							runOnGL_List.clear();
						}
					}
					isWorkOnRunOnGL.set(false);
					// work RunOnGlPool
					synchronized (runOnGL_ListWaitpool)
					{
						if (runOnGL_ListWaitpool != null && runOnGL_ListWaitpool.size() > 0)
						{
							if (runOnGL_ListWaitpool.size() > 0)
							{
								for (runOnGL run : runOnGL_ListWaitpool)
								{
									if (run != null) run.run();
								}

								runOnGL_ListWaitpool.clear();
							}

						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	@Override
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{
		super.renderChilds(batch, parentInfo);

	}

	/**
	 * Überprüft ob die Liste oben oder unten Platz hat und lösst eine Animation aus, in der die Liste auf die erste oder letzte Position
	 * scrollt.
	 */
	@Override
	public void chkSlideBack()
	{
		// Logger.LogCat("chkSlideBack()");
		if (!mIsDrageble)
		{
			startAnimationtoTop();
			return;
		}
		if (mPos > 0) startAnimationtoTop();
		else if (mPos < mcalcAllSizeBase) startAnimationToBottom();
	}

	@Override
	public boolean isDragable()
	{
		return mIsDrageble;
	}

	@Override
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

	protected void startAnimationToBottom()
	{
		if (mBaseAdapter == null) return;
		mBottomAnimation = true;
		scrollTo(mcalcAllSizeBase);
	}

	public void scrollToItem(int i)
	{
		if (mPosDefault == null || mBaseAdapter == null) return;

		// if (i < getMaxItemCount()) i = getMaxItemCount();

		Point lastAndFirst = getFirstAndLastVisibleIndex();

		if (lastAndFirst.y == -1)
		{
			setListPos(0);
			return;
		}

		float versatz = (i < lastAndFirst.y) ? -getListViewLength() + this.mBaseAdapter.getItemSize(i) : 0;

		Logger.DEBUG("SetListPos -> ScrollTO Item [" + i + "]");

		try
		{
			if (i >= 0 && i < mPosDefault.size())
			{
				setListPos(mPosDefault.get(i) + versatz, false);
			}
			else
			{
				setListPos(mPosDefault.get(mPosDefault.size() - 1), false);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void scrollTo(float Pos)
	{

		// Logger.LogCat("Scroll TO:" + Pos);

		mAnimationTarget = Pos;
		stopTimer();

		mAnimationTimer = new Timer();
		try
		{
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

						setListPos(mAnimationTarget, true);
						stopTimer();
						return;
					}

					// Logger.DEBUG("Set Animatet ListPos");
					setListPos(newPos, true);
				}

			}, 0, ANIMATION_TICK);
		}
		catch (Exception e)
		{

			e.printStackTrace();
		}
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

	/**
	 * Returns a Point<br>
	 * x= first full visible Index<br>
	 * y= last full visible Index<br>
	 * 
	 * @return
	 */
	public Point getFirstAndLastVisibleIndex()
	{
		Point ret = new Point();
		synchronized (childs)
		{

			ArrayList<ListViewItemBase> visibleList = new ArrayList<ListViewItemBase>();

			for (GL_View_Base v : childs)
			{
				if (v instanceof ListViewItemBase)
				{
					visibleList.add(((ListViewItemBase) v));
				}
			}

			if (visibleList.isEmpty())
			{
				ret.x = -1;
				ret.y = -1;
				return ret;
			}

			Collections.sort(visibleList);
			boolean foundFirstVisible = false;
			int lastFoundedVisible = 0;
			for (ListViewItemBase lv : visibleList)
			{
				if (this.ThisWorldRec.contains(lv.ThisWorldRec))
				{
					if (!foundFirstVisible) ret.x = lv.getIndex();
					foundFirstVisible = true;
					lastFoundedVisible = lv.getIndex();
				}
				else
				{
					if (!foundFirstVisible)
					{
						continue;
					}
					ret.y = lastFoundedVisible;
					break;
				}
			}

		}

		// Logger.LogCat("getLastVisiblePosition = " + ret);

		return ret;
	}

	// public int getFirstVisiblePosition()
	// {
	// if (mBaseAdapter == null) return 0;
	// synchronized (childs)
	// {
	//
	// ArrayList<ListViewItemBase> visibleList = new ArrayList<ListViewItemBase>();
	//
	// for (GL_View_Base v : childs)
	// {
	// if (v instanceof ListViewItemBase)
	// {
	// visibleList.add(((ListViewItemBase) v));
	// }
	// }
	//
	// Collections.sort(visibleList);
	//
	// for (ListViewItemBase lv : visibleList)
	// {
	// if (this.contains(lv))
	// {
	// return lv.getIndex();
	// }
	//
	// }
	// }
	//
	// return mBaseAdapter.getCount();
	// }

	/**
	 * Gibt die Anzahl der Items, welche gleichzeitig dargestellt werden können, wenn alle Items so Groß sind wie das kleinste Item in der
	 * List, zurück.
	 */
	public int getMaxItemCount()
	{
		return mMaxItemCount;
	}

	public abstract void notifyDataSetChanged();

	@Override
	public float getScrollPos()
	{
		return mPos;
	}

	@Override
	public CB_View_Base getView()
	{
		return this;
	}

	@Override
	public float getFirstItemSize()
	{
		return firstItemSize;
	}

	@Override
	public float getLasstItemSize()
	{
		return lastItemSize;
	}

}
