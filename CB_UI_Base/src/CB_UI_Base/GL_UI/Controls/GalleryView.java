/* 
 * Copyright (C) 2015 team-cachebox.de
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
package CB_UI_Base.GL_UI.Controls;

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.List.H_ListView;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Lists.CB_List;

/**
 * 
 * @author Longri
 *
 */
public class GalleryView extends H_ListView {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GalleryView.class);

    private boolean showSelectedItemCenter = false;

    public GalleryView(CB_RectF rec, String Name) {
	super(rec, Name);
	mCanDispose = false;
    }

    public void showSelectedItemCenter(boolean value) {
	showSelectedItemCenter = value;
    }

    @Override
    protected void calcDefaultPosList() {
	if (mPosDefault != null) {
	    mPosDefault.clear();
	} else {
	    mPosDefault = new CB_List<Float>();
	}

	float countPos = this.getWidth();
	minimumItemSize = this.getWidth();
	mAllSize = 0;
	for (int i = 0; i < mBaseAdapter.getCount(); i++) {
	    float itemWidth = mBaseAdapter.getItemSize(i);
	    countPos -= itemWidth + mDividerSize;
	    mPosDefault.add(0, countPos - mItemPosOffset);
	    mAllSize += itemWidth + mDividerSize;

	    if (itemWidth < minimumItemSize)
		minimumItemSize = itemWidth;
	}
	mcalcAllSizeBase = countPos - mDividerSize;
	mPos = countPos - mDividerSize;
	mMaxItemCount = (int) (this.getWidth() / minimumItemSize);
	if (mMaxItemCount < 1)
	    mMaxItemCount = 1;
    }

    @Override
    public void chkSlideBack() {
	log.debug("chkSlideBack()");

	if (showSelectedItemCenter) {

	    //search item at center and set as selected

	    float itemWidth = mBaseAdapter.getItemSize(0);

	    for (int i = 0, n = mPosDefault.size() - 1; i < n; i++) {
		final float pos1 = mPosDefault.get(i) - this.getHalfWidth() + (itemWidth / 2);
		final float pos2 = mPosDefault.get(i + 1) - this.getHalfWidth() + (itemWidth / 2);

		if (mPos > pos1 && mPos < pos2 || mPos < 0 || mPos > mAllSize + itemWidth) {
		    //search max Div 
		    final float div1 = Math.abs(mPos - pos1);
		    final float div2 = Math.abs(mPos - pos2);
		    final int idx = i;

		    if (div1 <= div2) {
			//			setSelection(idx);
			mBaseAdapter.getView(idx).click(0, 0, 0, 0);
			scrollItemToCenter(idx);
		    } else {
			//			setSelection(idx + 1);
			mBaseAdapter.getView(idx + 1).click(0, 0, 0, 0);
			scrollItemToCenter(idx + 1);
		    }
		    break;
		}
	    }
	    return;
	}

	if (!mIsDraggable) {
	    startAnimationToBottom();

	} else {
	    lastPos = mcalcAllSizeBase;

	    if (mPos > 0) {
		startAnimationtoTop();
		snapIn(mBaseAdapter.getCount() - 1);
	    } else if (mPos < mcalcAllSizeBase) {
		startAnimationToBottom();
		snapIn(0);
	    }
	}

	//SnapIN?

	for (int i = 0, n = mPosDefault.size() - 1; i < n; i++) {
	    final float pos1 = mPosDefault.get(i);
	    final float pos2 = mPosDefault.get(i + 1);

	    if (mPos > pos1 && mPos < pos2) {
		//search max Div 

		final float div1 = Math.abs(mPos - pos1);
		final float div2 = Math.abs(mPos - pos2);
		final int idx = i;
		GL.that.RunOnGL(new IRunOnGL() {

		    @Override
		    public void run() {

			if (div1 <= div2) {
			    //Snap to 1
			    mBottomAnimation = false;
			    GalleryView.this.scrollTo(pos1);
			    log.debug("SnapIn first " + pos1);
			    snapIn(idx);
			} else {
			    //Snap to 2
			    mBottomAnimation = true;
			    GalleryView.this.scrollTo(pos2);
			    log.debug("SnapIn second " + pos2);
			    snapIn(idx + 1);
			}
		    }
		});
		break;
	    }
	}
    }

    public void reloadItemsNow() {
	this.removeChildsDirekt();
	this.mAddeedIndexList.clear();
	this.addVisibleItems(true);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
	if (!mIsDraggable)
	    return false;
	mDragged = x - mLastTouch;
	float sollPos = mLastPos_onTouch - mDragged;
	float toMuch = 0;
	if (sollPos - firstItemSize > 0 || sollPos < mcalcAllSizeBase) {
	    if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < mcalcAllSizeBase) {
		if (KineticPan)
		    GL.that.StopKinetic(x, y, pointer, true);
		return true;
	    }

	    //	    if (sollPos - firstItemSize > 0) {
	    //		toMuch = 0 - sollPos + firstItemSize;
	    //		toMuch /= 2;
	    //	    } else if (sollPos < mcalcAllSizeBase) {
	    //		toMuch = mcalcAllSizeBase - sollPos;
	    //		toMuch /= 2;
	    //	    }
	}

	if (toMuch != 0)
	    log.debug("tomuch" + toMuch);
	setListPos(sollPos + toMuch, KineticPan);
	return true;
    }

    @Override
    public void onResized(CB_RectF rec) {
	// don't call super.onResized()
	// this will change posList

	// Items neu laden
	//	calcDefaultPosList();
	//	mMustSetPos = true;
    }

    public void snapIn(int index) {

    }

    @Override
    public void setSelection(int i) {
	super.setSelection(i);

	//scrollTo(mPosDefault.get(i));
    }

    @Override
    protected void scrollToSelectedItem() {
	//	if (this.isDragable()) {
	//	    Point lastAndFirst = getFirstAndLastVisibleIndex();
	//	    if (!(lastAndFirst.x < mSelectedIndex && lastAndFirst.y > mSelectedIndex))
	//		scrollToItem(mSelectedIndex);
	//	} else {
	//	    scrollTo(0);
	//	}
	//	selectionchanged = false;
    }

    public void scrollItemToCenter(int idx) {
	if (idx < 0)
	    return;
	float defaultpos = mPosDefault.get(idx);
	float sollpos = defaultpos - this.getHalfWidth() + (mBaseAdapter.getItemSize(idx) / 2);
	mBottomAnimation = sollpos > mPos;
	scrollTo(sollpos);
	this.mSelectedIndex = idx;
    }

    @Override
    public ListViewItemBase getSelectedItem() {
	if (mBaseAdapter == null)
	    return null;

	//get selected idx

	int idx = 0;
	for (int n = mBaseAdapter.getCount(); idx < n; idx++) {
	    if (mPosDefault.get(idx) == mPos)
		break;
	}

	if (idx == -1)
	    return null;
	if (idx >= mBaseAdapter.getCount())
	    return null;
	return mBaseAdapter.getView(idx);
    }

}
