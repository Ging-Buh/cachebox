package CB_Core.GL_UI.Controls.List;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

public abstract class H_ListView extends ListViewBase
{

	public H_ListView(CB_RectF rec, CharSequence Name)
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
				tmp.setX(tmp.getX() + distance);

				if (tmp.getX() > this.getMaxX() || tmp.getMaxX() < 0)
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
				tmp.dispose();
			}
			clearList.clear();
			clearList = null;
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

			// if (mPosDefault.get(i) < this.getMaxX())
			if (mPosDefault.get(i) > mAllSize)
			{
				ListViewItemBase tmp = mBaseAdapter.getView(i);
				float itemPos = mPosDefault.get(i);
				itemPos -= mPos;

				if (itemPos <= this.getMaxX())
				{
					if (!mAddeedIndexList.contains(tmp.getIndex()))
					{
						if (itemPos + tmp.getWidth() < 0) break;
						tmp.setX(itemPos);
						this.addChild(tmp);
						mAddeedIndexList.add(tmp.getIndex());
					}
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

		float mesuredWidth = 0;

		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
		{
			mesuredWidth += mBaseAdapter.getView(i).getWidth();
		}

		float countPos = this.width;
		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++)
		{
			CB_View_Base tmp = mBaseAdapter.getView(i);
			countPos -= tmp.getWidth();
			mPosDefault.add(countPos);
			mPosDefault.add(0, countPos);
		}
		mAllSize = countPos;
		mPos = countPos;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		mDraged = x - mLastTouch;
		setPos(mLastPos_onTouch - mDraged);
		return true;
	}

	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		mLastTouch = x;
		mLastPos_onTouch = mPos;
		return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
	}

}
