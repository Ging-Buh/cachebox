package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

public class H_ListView extends ListViewBase
{

	public H_ListView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
	}

	protected void RenderThreadSetPos(float value, boolean Kinetic)
	{
		float distance = mPos - value;

		ArrayList<ListViewItemBase> clearList = new ArrayList<ListViewItemBase>();

		// alle childs verschieben
		synchronized (childs)
		{
			for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
			{
				GL_View_Base tmp = iterator.next();
				if (mReloadItems)
				{
					clearList.add((ListViewItemBase) tmp);
				}
				else
				{
					tmp.setX(tmp.getX() + distance);

					if (tmp.getX() > this.getMaxX() || tmp.getMaxX() < 0)
					{
						// Item ist nicht mehr im sichtbaren Bereich!
						clearList.add((ListViewItemBase) tmp);
					}
				}
			}
		}

		// afräumen
		if (clearList.size() > 0)
		{
			for (Iterator<ListViewItemBase> iterator = clearList.iterator(); iterator.hasNext();)
			{
				ListViewItemBase tmp = iterator.next();
				mAddeedIndexList.remove((Object) tmp.getIndex());
				// Logger.LogCat("Remove: " + tmp.getName());
				this.removeChild(tmp);
				if (mCanDispose) tmp.dispose();
			}
			clearList.clear();
			clearList = null;

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

		}

		mPos = value;

		addVisibleItems(Kinetic);
		mMustSetPos = false;

	}

	protected void addVisibleItems(boolean Kinetic)
	{
		if (mBaseAdapter == null) return;
		if (mPosDefault == null) calcDefaultPosList();

		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
		{
			if (!mAddeedIndexList.contains(i))
			{

				ListViewItemBase tmp = mBaseAdapter.getView(i);

				// if (mPosDefault.get(i) < this.getMaxX())
				if (mPosDefault.get(i) + tmp.getWidth() - mPos > 0)
				{

					float itemPos = mPosDefault.get(i);
					itemPos -= mPos;

					if (itemPos <= this.getWidth())
					{
						tmp.setX(itemPos);
						// Logger.LogCat("Add: " + tmp.getName());
						if (i == mSelectedIndex)
						{
							tmp.isSelected = true;
							tmp.resetInitial();
						}
						this.addChild(tmp);
						mAddeedIndexList.add(tmp.getIndex());
					}
					else
						break;
				}
			}

			// RenderRequest
			GL_Listener.glListener.renderOnce(this);
		}
	}

	protected void calcDefaultPosList()
	{
		if (mPosDefault != null)
		{
			mPosDefault.clear();
			mPosDefault = null;
		}

		mPosDefault = new ArrayList<Float>();

		float countPos = this.width;
		minimumItemSize = this.width;
		for (int i = 0; i < mBaseAdapter.getCount(); i++)
		{
			float itemWidth = mBaseAdapter.getItemSize(i);
			countPos -= itemWidth + mDividerSize;
			mPosDefault.add(countPos);
			mPosDefault.add(0, countPos);
			if (itemWidth < minimumItemSize) minimumItemSize = itemWidth;
		}
		mAllSize = countPos - mDividerSize;
		mPos = countPos - mDividerSize;
		mMaxItemCount = (int) (this.width / minimumItemSize);
		if (mMaxItemCount < 1) mMaxItemCount = 1;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (!mIsDrageble) return false;
		mDraged = x - mLastTouch;
		float sollPos = mLastPos_onTouch - mDraged;
		float toMuch = 0;
		if (sollPos - firstItemSize > 0 || sollPos < mAllSize)
		{
			if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < mAllSize)
			{
				if (KineticPan) GL_Listener.glListener.StopKinetic(x, y, pointer, true);
				return true;
			}

			if (sollPos - firstItemSize > 0)
			{
				toMuch = 0 - sollPos + firstItemSize;
				toMuch /= 2;
			}
			else if (sollPos < mAllSize)
			{
				toMuch = mAllSize - sollPos;
				toMuch /= 2;
			}
		}

		setListPos(sollPos + toMuch, KineticPan);
		return true;
	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		super.onTouchDown(x, y, pointer, button);
		if (!mIsDrageble) return true;
		mLastTouch = x;
		mLastPos_onTouch = mPos;
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
