/* 
 * Copyright (C) 2014-2015 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.GL_UI.Controls.List;

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Lists.CB_List;
import CB_Utils.Math.Point;

public class V_ListView extends ListViewBase {

    public V_ListView(CB_RectF rec, String name) {
	super(rec, name);
    }

    public V_ListView(CB_RectF rec, GL_View_Base parent, String name) {
	super(rec, parent, name);
    }

    @Override
    protected void RenderThreadSetPos(float value, boolean Kinetic) {
	synchronized (childs) {
	    mPos = value;
	    clearList.clear();

	    // alle childs verschieben

	    if (mReloadItems) {
		mAddedIndexList.clear();
		if (mCanDispose) {
		    synchronized (childs) {
			for (int i = 0, n = childs.size(); i < n; i++) {
			    childs.get(i).dispose();
			}
		    }
		}
		this.removeChilds();
	    } else {
		for (int i = 0, n = childs.size(); i < n; i++) {
		    ListViewItemBase tmp = (ListViewItemBase) childs.get(i);
		    float itemPos = mPosDefault.get(tmp.getIndex());
		    itemPos -= mPos;
		    tmp.setY(itemPos);

		    if (!isTouch) {
			if (tmp.getY() > this.getMaxY() || tmp.getMaxY() < 0) {
			    // Item ist nicht mehr im sichtbaren Bereich!
			    clearList.add(tmp);
			}
		    }
		}

	    }

	    mReloadItems = false;

	    // afr�umen
	    if (clearList.size() > 0) {

		synchronized (mAddedIndexList) {
		    for (int i = 0; i < clearList.size(); i++) {
			ListViewItemBase tmp = clearList.get(i);
			int index = mAddedIndexList.indexOf(tmp.getIndex());
			if (index >= 0 && index < mAddedIndexList.size()) {
			    mAddedIndexList.remove(index);
			    // log.debug("Remove Item " + tmp.getIndex());
			    this.removeChild(tmp);
			    if (mCanDispose)
				tmp.dispose();
			} else {
			    System.out.print("");
			}
		    }
		    clearList.clear();
		}

	    }

	}

	// setze First Index, damit nicht alle Items durchlaufen werden m�ssen
	synchronized (mAddedIndexList) {
	    mAddedIndexList.sort();
	    if (mAddedIndexList.size() > 0) {
		mFirstIndex = mAddedIndexList.get(0) - mMaxItemCount;
		if (mFirstIndex < 0)
		    mFirstIndex = 0;
	    } else {
		mFirstIndex = 0;
	    }

	    if (mLastIndex == mFirstIndex)
		mFirstIndex = 0;

	}

	addVisibleItems(Kinetic);
	mMustSetPos = false;
	mMustSetPosKinetic = false;
	callListPosChangedEvent();
    }

    /**
     * Wenn Kinetic == True werden mehr Items geladen, damit beim schnellen Scrollen die Items schon erstellt sind, bevor sie in den
     * sichtbaren Bereich kommen.
     * 
     * @param Kinetic
     */
    @Override
    protected void addVisibleItems(boolean Kinetic) {

	try {

	    if (mBaseAdapter == null)
		return;
	    if (mPosDefault == null)
		calcDefaultPosList();

	    final float workPos = mPos;

	    synchronized (childs) {
		for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++) {
		    if (!mAddedIndexList.contains(i)) {
			if (mPosDefault.size() - 1 < i)
			    return;

			float itemPos = mPosDefault.get(i);
			itemPos -= workPos;

			if (itemPos < this.getMaxY() && itemPos + mBaseAdapter.getItemSize(i) > -(mMaxItemCount * minimumItemSize)) {
			    ListViewItemBase tmp = mBaseAdapter.getView(i);
			    if (tmp != null) {
				tmp.setY(itemPos);
				if (i == mSelectedIndex) {
				    tmp.isSelected = true;
				    tmp.resetInitial();
				}
				this.addChild(tmp);
			    }

			    // log.debug("Add Item " + i);
			    mAddedIndexList.add(i);
			} else if (itemPos + mBaseAdapter.getItemSize(i) < -(mMaxItemCount * minimumItemSize)) {
			    mLastIndex = i;
			    break;
			}

		    }

		    // RenderRequest
		    GL.that.renderOnce();

		    if (selectionchanged) {
			Point lastAndFirst = getFirstAndLastVisibleIndex();

			//						if (lastAndFirst.y == -1)
			//						{
			//							scrollTo(0);
			//							selectionchanged = false;
			//							return;
			//						}

			if (this.isDragable()) {
			    if (!(lastAndFirst.x < mSelectedIndex && lastAndFirst.y > mSelectedIndex))
				scrollToItem(mSelectedIndex);
			} else {
			    scrollTo(0);
			}
			selectionchanged = false;
		    }
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    AtomicBoolean isInCalculation = new AtomicBoolean(false);

    @Override
    public void calcDefaultPosList() {
	if (this.isDisposed() || isInCalculation.get()) {
	    return;
	}
	isInCalculation.set(true);
	if (mBaseAdapter == null) {
	    isInCalculation.set(false);
	    return; // can't calc
	}

	try {
	    if (mPosDefault != null) {
		mPosDefault.clear();
	    } else {
		mPosDefault = new CB_List<Float>();
	    }

	    minimumItemSize = this.getHeight();

	    float countPos = this.getHeight() - mDividerSize;

	    mAllSize = 0;
	    if (hasInvisibleItems) {
		for (int i = 0; i < mBaseAdapter.getCount(); i++) {
		    float itemHeight = 0;
		    ListViewItemBase item = mBaseAdapter.getView(i);
		    if (item != null && item.isVisible() && item.getHeight() > 0) {
			itemHeight = mBaseAdapter.getItemSize(i);
			countPos -= itemHeight + mDividerSize;
			if (itemHeight < minimumItemSize)
			    minimumItemSize = itemHeight;
			mAllSize += itemHeight + mDividerSize;
		    }

		    mPosDefault.add(countPos + mItemPosOffset);

		}
	    } else {
		for (int i = 0; i < mBaseAdapter.getCount(); i++) {
		    float itemHeight = mBaseAdapter.getItemSize(i);
		    countPos -= itemHeight + mDividerSize;
		    if (itemHeight < minimumItemSize)
			minimumItemSize = itemHeight;
		    mAllSize += itemHeight + mDividerSize;
		    mPosDefault.add(countPos + mItemPosOffset);

		}
	    }

	    mcalcAllSizeBase = countPos - mDividerSize;
	    mMaxItemCount = (int) (this.getHeight() / minimumItemSize);
	    if (mMaxItemCount < 1)
		mMaxItemCount = 1;

	    if (mAllSize > this.getHeight()) {
		this.setDraggable();
	    } else {
		this.setUnDraggable();
	    }
	} catch (Exception e) {
	}

	isInCalculation.set(false);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
	if (!mIsDraggable)
	    return false;
	mDragged = y - mLastTouch;

	float sollPos = mLastPos_onTouch - mDragged;
	float toMuch = 0;
	if (sollPos - firstItemSize > 0 || sollPos < mcalcAllSizeBase) {
	    if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < mcalcAllSizeBase) {
		if (KineticPan)
		    GL.that.StopKinetic(x, y, pointer, true);
		return true;
	    }

	    if (sollPos - firstItemSize > 0) {
		toMuch = 0 - sollPos + firstItemSize;
		toMuch /= 2;
	    } else if (sollPos < mcalcAllSizeBase) {
		toMuch = mcalcAllSizeBase - sollPos;
		toMuch /= 2;
	    }
	}

	setListPos(sollPos + toMuch, KineticPan);
	return false;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
	// super.onTouchDown(x, y, pointer, button);
	if (!mIsDraggable)
	    return true;
	mLastTouch = y;
	mLastPos_onTouch = mPos;
	return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelösst wird.
    }

    @Override
    protected void Initial() {

    }

    @Override
    protected void SkinIsChanged() {
	reloadItems();
    }

    @Override
    public void notifyDataSetChanged() {
	calcDefaultPosList();
	reloadItems();

	if (mBaseAdapter != null && mBaseAdapter.getCount() <= mSelectedIndex)
	    setSelection(mBaseAdapter.getCount() - 1);

    }

    @Override
    protected float getListViewLength() {
	return getHeight();
    }

}
