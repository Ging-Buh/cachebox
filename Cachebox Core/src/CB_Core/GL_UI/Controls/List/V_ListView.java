package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

public class V_ListView extends ListViewBase
{

	public V_ListView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
	}

	protected void RenderThreadSetPos(float value)
	{
		float distance = mPos - value;

		ArrayList<ListViewItemBase> clearList = new ArrayList<ListViewItemBase>();

		// alle childs verschieben
		synchronized (childs)
		{
			for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
			{
				GL_View_Base tmp = iterator.next();
				tmp.setY(tmp.getY() + distance);

				if (tmp.getY() > this.getMaxY() || tmp.getMaxY() < 0)
				{
					// Item ist nicht mehr im sichtbaren Bereich!
					clearList.add((ListViewItemBase) tmp);
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

		addVisibleItems();
		mMustSetPos = false;

	}

	protected void addVisibleItems()
	{
		if (mBaseAdapter == null) return;
		if (mPosDefault == null) calcDefaultPosList();

		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
		{
			if (!mAddeedIndexList.contains(i))
			{
				if (mPosDefault.get(i) < this.getMaxY())
				{
					ListViewItemBase tmp = mBaseAdapter.getView(i);
					float itemPos = mPosDefault.get(i);
					itemPos -= mPos;

					if (itemPos < this.getMaxY())
					{
						tmp.setY(itemPos);
						this.addChild(tmp);
						mAddeedIndexList.add(tmp.getIndex());
					}
				}
				else
					break;
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

		float minimumItemHeight = this.height;

		float countPos = this.height;

		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
		{
			CB_View_Base tmp = mBaseAdapter.getView(i);
			countPos -= tmp.getHeight() + mDividerSize;
			mPosDefault.add(countPos);

			if (tmp.getHeight() < minimumItemHeight) minimumItemHeight = tmp.getHeight();

		}
		mAllSize = countPos;
		mMaxItemCount = (int) (this.height / minimumItemHeight);
		if (mMaxItemCount < 1) mMaxItemCount = 1;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		if (!mIsDrageble) return false;
		mDraged = y - mLastTouch;
		setPos(mLastPos_onTouch - mDraged);
		return true;
	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (!mIsDrageble) return false;
		mLastTouch = y;
		mLastPos_onTouch = mPos;
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
