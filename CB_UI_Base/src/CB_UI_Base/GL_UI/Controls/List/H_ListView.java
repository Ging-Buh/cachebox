package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Lists.CB_List;
import CB_Utils.Math.Point;

public class H_ListView extends ListViewBase
{

	public H_ListView(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	@Override
	protected void RenderThreadSetPos(float value, boolean Kinetic)
	{
		float distance = mPos - value;

		// alle childs verschieben
		synchronized (childs)
		{

			for (int i = 0, n = childs.size(); i < n; i++)
			{
				GL_View_Base tmp = childs.get(i);

				if (mReloadItems)
				{
					clearList.add((ListViewItemBase) tmp);
				}
				else
				{
					float itemPos = mPosDefault.get(((ListViewItemBase) tmp).getIndex());
					itemPos -= mPos;
					tmp.setX(itemPos);

					if (tmp.getX() > this.getMaxX() || tmp.getMaxX() < 0)
					{
						// Item ist nicht mehr im sichtbaren Bereich!
						clearList.add((ListViewItemBase) tmp);
					}
				}
			}
		}

		mReloadItems = false;

		// afräumen
		if (clearList.size() > 0)
		{
			for (int i = 0; i < clearList.size(); i++)
			{
				ListViewItemBase tmp = clearList.get(i);
				int index = mAddeedIndexList.indexOf(tmp.getIndex());
				if (index >= 0 && index < mAddeedIndexList.size())
				{
					mAddeedIndexList.remove(index);
					// Logger.LogCat("Remove Item " + tmp.getIndex());
					this.removeChild(tmp);
					if (mCanDispose) tmp.dispose();
				}
				else
				{
					System.out.print("");
				}
			}
			clearList.clear();

			// setze First Index, damit nicht alle Items durchlaufen werden müssen
			mAddeedIndexList.sort();

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

	@Override
	@SuppressWarnings("unchecked")
	protected void addVisibleItems(boolean Kinetic)
	{
		if (mBaseAdapter == null) return;
		if (mPosDefault == null) calcDefaultPosList();

		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
		{
			if (!mAddeedIndexList.contains(i))
			{

				if (mPosDefault.size() - 1 < i || mBaseAdapter.getCount() < i) return;

				ListViewItemBase tmp = mBaseAdapter.getView(i);

				if (tmp == null) return;
				try
				{
					if (mPosDefault.get(i) + tmp.getWidth() - mPos > 0)
					{

						float itemPos = mPosDefault.get(i);
						itemPos -= mPos;

						if (itemPos <= this.getWidth())
						{
							tmp.setY(this.getHalfHeight() - tmp.getHalfHeight());// center Pos
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
				catch (Exception e)
				{
					// egal
				}
			}

			// RenderRequest
			GL.that.renderOnce();

			if (selectionchanged)
			{
				if (this.isDragable())
				{
					Point lastAndFirst = getFirstAndLastVisibleIndex();
					if (!(lastAndFirst.x < mSelectedIndex && lastAndFirst.y > mSelectedIndex)) scrollToItem(mSelectedIndex);
				}
				else
				{
					scrollTo(0);
				}
				selectionchanged = false;
			}
		}
	}

	@Override
	protected void calcDefaultPosList()
	{
		if (mPosDefault != null)
		{
			mPosDefault.clear();
			mPosDefault = null;
		}

		mPosDefault = new CB_List<Float>();

		float countPos = this.getWidth();
		minimumItemSize = this.getWidth();
		mAllSize = 0;
		for (int i = 0; i < mBaseAdapter.getCount(); i++)
		{
			float itemWidth = mBaseAdapter.getItemSize(i);
			countPos -= itemWidth + mDividerSize;
			// mPosDefault.add(countPos);
			mPosDefault.add(0, countPos);
			mAllSize += itemWidth + mDividerSize;

			if (itemWidth < minimumItemSize) minimumItemSize = itemWidth;
		}
		mcalcAllSizeBase = countPos - mDividerSize;
		mPos = countPos - mDividerSize;
		mMaxItemCount = (int) (this.getWidth() / minimumItemSize);
		if (mMaxItemCount < 1) mMaxItemCount = 1;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if (!mIsDrageble) return false;
		mDraged = x - mLastTouch;
		float sollPos = mLastPos_onTouch - mDraged;
		float toMuch = 0;
		if (sollPos - firstItemSize > 0 || sollPos < mcalcAllSizeBase)
		{
			if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < mcalcAllSizeBase)
			{
				if (KineticPan) GL.that.StopKinetic(x, y, pointer, true);
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
		super.onTouchDown(x, y, pointer, button);
		if (!mIsDrageble) return true;
		mLastTouch = x;
		mLastPos_onTouch = mPos;
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

	@Override
	protected void Initial()
	{

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

		if (mAllSize > this.getWidth())
		{
			this.setDragable();
		}
		else
		{
			this.setUndragable();
		}

		if (mBaseAdapter.getCount() <= mSelectedIndex) setSelection(mBaseAdapter.getCount() - 1);
	}

	@Override
	public void chkSlideBack()
	{
		if (!mIsDrageble)
		{
			startAnimationToBottom();
			return;
		}
		if (mPos > 0) startAnimationtoTop();
		else if (mPos < mcalcAllSizeBase) startAnimationToBottom();
	}

	@Override
	protected float getListViewLength()
	{
		return getWidth();
	}

}
