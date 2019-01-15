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
package CB_UI_Base.GL_UI.Controls.List;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_Listener.GL_Input;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Math.Point;

public class H_ListView extends ListViewBase {
    protected static final String log = "H_ListView";

    public H_ListView(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    @Override
    protected void RenderThreadSetPos(float value, boolean Kinetic) {
        // alle childs verschieben
        synchronized (childs) {

            for (int i = 0, n = childs.size(); i < n; i++) {
                GL_View_Base tmp = childs.get(i);

                if (mReloadItems) {
                    clearList.add((ListViewItemBase) tmp);
                } else {
                    float itemPos = mPosDefault.get(((ListViewItemBase) tmp).getIndex());
                    itemPos -= mPos;
                    tmp.setX(itemPos);

                    if (tmp.getX() > this.getMaxX() || tmp.getMaxX() < 0) {
                        // Item ist nicht mehr im sichtbaren Bereich!
                        clearList.add((ListViewItemBase) tmp);
                    }
                }
            }
        }

        mReloadItems = false;

        // aufräumen
        if (clearList.size() > 0) {
            for (int i = 0; i < clearList.size(); i++) {
                ListViewItemBase tmp = clearList.get(i);
                int index = mAddedIndexList.indexOf(tmp.getIndex());
                if (index >= 0 && index < mAddedIndexList.size()) {
                    mAddedIndexList.remove(index);
                    // Log.debug(log, "Remove Item " + tmp.getIndex());
                    this.removeChild(tmp);
                    if (mCanDispose)
                        tmp.dispose();
                } else {
                    System.out.print("");
                }
            }
            clearList.clear();

            // setze First Index, damit nicht alle Items durchlaufen werden müssen
            mAddedIndexList.sort();

            if (mAddedIndexList.size() > 0) {
                mFirstIndex = mAddedIndexList.get(0) - mMaxItemCount;
                if (mFirstIndex < 0)
                    mFirstIndex = 0;
            } else {
                mFirstIndex = 0;
            }

        }

        mPos = value;

        addVisibleItems(Kinetic);
        mMustSetPos = false;

    }

    @Override
    protected void addVisibleItems(boolean Kinetic) {
        if (mBaseAdapter == null)
            return;
        if (mPosDefault == null)
            calcDefaultPosList();

        for (int i = mFirstIndex; i < mBaseAdapter.getCount(); i++) {
            if (!mAddedIndexList.contains(i)) {

                if (mPosDefault.size() - 1 < i || mBaseAdapter.getCount() < i)
                    return;

                ListViewItemBase tmp = mBaseAdapter.getView(i);

                if (tmp == null)
                    return;
                try {
                    if (mPosDefault.get(i) + tmp.getWidth() - mPos > 0) {

                        float itemPos = mPosDefault.get(i);
                        itemPos -= mPos;

                        if (itemPos <= this.getWidth()) {
                            tmp.setY(this.getHalfHeight() - tmp.getHalfHeight());// center Pos
                            tmp.setX(itemPos);
                            // Log.debug(log, "Add: " + tmp.getName());
                            if (i == mSelectedIndex) {
                                tmp.isSelected = true;
                                tmp.resetInitial();
                            }
                            this.addChild(tmp);
                            mAddedIndexList.add(tmp.getIndex());
                        } else
                            break;
                    }
                } catch (Exception e) {
                    Log.err(log, "Thread set pos", e);
                }
            }

            // RenderRequest
            GL.that.renderOnce();
            if (selectionchanged)
                scrollToSelectedItem();

        }
    }

    protected void scrollToSelectedItem() {
        if (this.isDraggable()) {
            Point lastAndFirst = getFirstAndLastVisibleIndex();
            if (!(lastAndFirst.x < mSelectedIndex && lastAndFirst.y > mSelectedIndex))
                scrollToItem(mSelectedIndex);
        } else {
            scrollTo(0);
        }
        selectionchanged = false;
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
            mPosDefault.add(0, countPos + mItemPosOffset);
            mAllSize += itemWidth + mDividerSize;

            if (itemWidth < minimumItemSize)
                minimumItemSize = itemWidth;
        }
        mcalcAllSizeBase = countPos - mDividerSize;
        mPos = countPos - mDividerSize;
        mMaxItemCount = (int) (this.getWidth() / minimumItemSize);
        if (mMaxItemCount < 1)
            mMaxItemCount = 1;

        if (mAllSize > this.getWidth()) {
            this.setDraggable();
        } else {
            this.setUnDraggable();
        }

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
                    GL_Input.that.StopKinetic(x, y, pointer, true);
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

        if (toMuch != 0)
            Log.debug(log, "tomuch" + toMuch);
        setListPos(sollPos + toMuch, KineticPan);
        return true;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        super.onTouchDown(x, y, pointer, button);
        if (!mIsDraggable)
            return true;
        mLastTouch = x;
        mLastPos_onTouch = mPos;
        return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgel�sst wird.
    }

    @Override
    protected void SkinIsChanged() {
        reloadItems();
    }

    @Override
    public void notifyDataSetChanged() {
        calcDefaultPosList();
        reloadItems();

        if (mAllSize > this.getWidth()) {
            this.setDraggable();
        } else {
            this.setUnDraggable();
        }

        if (mBaseAdapter.getCount() <= mSelectedIndex)
            setSelection(mBaseAdapter.getCount() - 1);
    }

    @Override
    public void chkSlideBack() {

        if (!mIsDraggable) {
            startAnimationToBottom();

        } else {
            lastPos = mcalcAllSizeBase;

            if (mPos > 0)
                startAnimationtoTop();
            else if (mPos < mcalcAllSizeBase)
                startAnimationToBottom();
        }

    }

    @Override
    protected float getListViewLength() {
        return getWidth();
    }

}
