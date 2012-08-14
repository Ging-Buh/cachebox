package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

public class V_ListView extends ListViewBase
{

	private int mVisibleItemCount = 0;

	public V_ListView(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	ArrayList<ListViewItemBase> clearList = new ArrayList<ListViewItemBase>();

	protected void RenderThreadSetPos(float value, boolean Kinetic)
	{
		float distance = mPos - value;
		mLastDragedDistance = distance;
		clearList.clear();

		// alle childs verschieben

		if (mReloadItems)
		{
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
		}
		else
		{
			synchronized (childs)
			{
				for (GL_View_Base tmp : childs)
				{
					tmp.setY(tmp.getY() + distance);

					if (!isTouch)
					{
						if (tmp.getY() > this.getMaxY() || tmp.getMaxY() < 0)
						{
							// Item ist nicht mehr im sichtbaren Bereich!
							clearList.add((ListViewItemBase) tmp);
						}
					}
				}
			}
		}

		mReloadItems = false;

		// afräumen
		if (clearList.size() > 0)
		{
			for (Iterator<ListViewItemBase> iterator = clearList.iterator(); iterator.hasNext();)
			{
				ListViewItemBase tmp = iterator.next();
				mAddeedIndexList.remove((Object) tmp.getIndex());
				// Logger.LogCat("Remove Item " + tmp.getIndex());
				this.removeChild(tmp);
				if (mCanDispose) tmp.dispose();
			}
			clearList.clear();
		}

		// setze First Index, damit nicht alle Items durchlaufen werden müssen
		Collections.sort(mAddeedIndexList);
		if (mAddeedIndexList.size() > 0)
		{
			mFirstIndex = mAddeedIndexList.get(0) - mMaxItemCount;
			if (mFirstIndex < 0) mFirstIndex = 0;
		}
		else
		{
			mFirstIndex = 0;
		}

		if (mLastIndex == mFirstIndex) mFirstIndex = 0;

		mPos = value;

		// addVisibleItems();
		addVisibleItemsThread(Kinetic);
		mMustSetPos = false;
		mMustSetPosKinetic = false;

	}

	protected void addVisibleItemsThread(final boolean Kinetic)
	{
		Thread th = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				addVisibleItems(Kinetic);
			}
		});

		th.run();
	}

	/**
	 * Wenn Kinetic == True werden mehr Items geladen, damit beim schnellen Scrollen die Items schon erstellt sind, bevor sie in den
	 * sichtbaren Bereich kommen.
	 * 
	 * @param Kinetic
	 */
	protected void addVisibleItems(boolean Kinetic)
	{
		if (mBaseAdapter == null) return;
		if (mPosDefault == null) calcDefaultPosList();
		ArrayList<Float> tmpPosDefault;
		synchronized (mPosDefault)
		{
			tmpPosDefault = (ArrayList<Float>) mPosDefault.clone();
		}
		synchronized (mBaseAdapter)
		{

			for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
			{
				if (!mAddeedIndexList.contains(i))
				{
					if (tmpPosDefault.size() - 1 < i) return;

					float itemPos = tmpPosDefault.get(i);
					itemPos -= mPos;

					if (itemPos < this.getMaxY() && itemPos + mBaseAdapter.getItemSize(i) > -(mMaxItemCount * minimumItemSize))
					{
						ListViewItemBase tmp = mBaseAdapter.getView(i);
						if (tmp != null)
						{
							tmp.setY(itemPos);
							if (i == mSelectedIndex)
							{
								tmp.isSelected = true;
								tmp.resetInitial();
							}
							this.addChild(tmp);
						}

						// Logger.LogCat("Add Item " + i);
						mAddeedIndexList.add(i);
					}

					else if (itemPos + mBaseAdapter.getItemSize(i) < -(mMaxItemCount * minimumItemSize))
					{
						mLastIndex = i;
						break;
					}

				}

				// RenderRequest
				GL_Listener.glListener.renderOnce(this.getName() + " addVisibleItems");

				if (selectionchanged)
				{
					if (this.isDrageble())
					{
						if (!(getFirstVisiblePosition() < mSelectedIndex && getLastVisiblePosition() > mSelectedIndex)) scrollToItem(mSelectedIndex);
					}
					else
					{
						scrollTo(0);
					}
					selectionchanged = false;
				}
			}
		}
	}

	protected void calcDefaultPosList()
	{
		if (mBaseAdapter == null) return; // can´t calc

		if (mPosDefault != null)
		{
			mPosDefault.clear();
			mPosDefault = null;
		}

		mPosDefault = new ArrayList<Float>();

		mVisibleItemCount = 0;
		minimumItemSize = this.height;

		float countPos = this.height - mDividerSize;

		mAllSize = 0;
		if (hasInvisibleItems)
		{
			for (int i = 0; i < mBaseAdapter.getCount(); i++)
			{
				float itemHeight = 0;
				ListViewItemBase item = mBaseAdapter.getView(i);
				if (item != null && item.isVisible() && item.getHeight() > 0)
				{
					itemHeight = mBaseAdapter.getItemSize(i);
					countPos -= itemHeight + mDividerSize;
					if (itemHeight < minimumItemSize) minimumItemSize = itemHeight;
					mAllSize += itemHeight + mDividerSize;
					mVisibleItemCount++;
				}

				mPosDefault.add(countPos);

			}
		}
		else
		{
			for (int i = 0; i < mBaseAdapter.getCount(); i++)
			{
				float itemHeight = mBaseAdapter.getItemSize(i);
				countPos -= itemHeight + mDividerSize;
				if (itemHeight < minimumItemSize) minimumItemSize = itemHeight;
				mVisibleItemCount++;
				mAllSize += itemHeight + mDividerSize;
				mPosDefault.add(countPos);

			}
		}

		mcalcAllSizeBase = countPos - mDividerSize;
		mMaxItemCount = (int) (this.height / minimumItemSize);
		if (mMaxItemCount < 1) mMaxItemCount = 1;

	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (!mIsDrageble) return false;
		mDraged = y - mLastTouch;

		float sollPos = mLastPos_onTouch - mDraged;
		float toMuch = 0;
		if (sollPos - firstItemSize > 0 || sollPos < mcalcAllSizeBase)
		{
			if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < mcalcAllSizeBase)
			{
				if (KineticPan) GL_Listener.glListener.StopKinetic(x, y, pointer, true);
				return true;
			}

			if (sollPos - firstItemSize > 0)
			{
				toMuch = 0 - sollPos + firstItemSize;
				toMuch /= 2;
			}
			else if (sollPos < mcalcAllSizeBase)
			{
				toMuch = mcalcAllSizeBase - sollPos;
				toMuch /= 2;
			}
		}

		setListPos(sollPos + toMuch, KineticPan);
		return true;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// super.onTouchDown(x, y, pointer, button);
		if (!mIsDrageble) return true;
		mLastTouch = y;
		mLastPos_onTouch = mPos;
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

	// @Override
	// protected void startAnimationtoTop()
	// {
	// if (mBaseAdapter == null) return;
	// mBottomAnimation = false;
	// scrollTo(mBaseAdapter.getItemSize(0));
	// }

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		reloadItems();
	}

	@Override
	public void notifyDataSetChanged()
	{
		calcDefaultPosList();
		reloadItems();

		if (mAllSize > this.height)
		{
			this.setDragable();
		}
		else
		{
			this.setUndragable();
		}

		if (mBaseAdapter != null && mBaseAdapter.getCount() <= mSelectedIndex) setSelection(mBaseAdapter.getCount() - 1);

	}

}
