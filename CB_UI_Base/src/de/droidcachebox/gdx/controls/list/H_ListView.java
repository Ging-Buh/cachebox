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
package de.droidcachebox.gdx.controls.list;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

public class H_ListView extends ListViewBase {
    protected static final String log = "H_ListView";

    public H_ListView(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    @Override
    protected void renderThreadSetPos(float value, boolean Kinetic) {
        // move all childs
        synchronized (childs) {
            int n = childs.size();
            for (int i = 0; i < n; i++) {
                GL_View_Base tmp = childs.get(i);

                if (mReloadItems) {
                    clearList.add((ListViewItemBase) tmp);
                } else {
                    float itemPos = mPosDefault.get(((ListViewItemBase) tmp).getIndex());
                    itemPos = itemPos - currentPosition;
                    tmp.setX(itemPos);

                    if (tmp.getX() > getMaxX() || tmp.getMaxX() < 0) {
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
                int index = addedIndexList.indexOf(tmp.getIndex());
                if (index >= 0 && index < addedIndexList.size()) {
                    addedIndexList.remove(index);
                    // Log.debug(log, "Remove Item " + tmp.getIndex());
                    removeChild(tmp);
                    if (mCanDispose)
                        tmp.dispose();
                } else {
                    System.out.print("");
                }
            }
            clearList.clear();

            // setze First Index, damit nicht alle Items durchlaufen werden müssen
            addedIndexList.sort();

            if (addedIndexList.size() > 0) {
                firstIndex = addedIndexList.get(0) - maxNumberOfVisibleItems;
                if (firstIndex < 0)
                    firstIndex = 0;
            } else {
                firstIndex = 0;
            }

        }

        currentPosition = value;

        addVisibleItems(Kinetic);
        mMustSetPos = false;

    }

    @Override
    protected void addVisibleItems(boolean Kinetic) {
        if (adapter == null)
            return;
        if (mPosDefault == null)
            calculateItemPosition();

        for (int i = firstIndex; i < adapter.getCount(); i++) {
            if (!addedIndexList.contains(i)) {

                if (mPosDefault.size() - 1 < i || adapter.getCount() < i)
                    return;

                ListViewItemBase tmp = adapter.getView(i);

                if (tmp == null)
                    return;
                try {
                    if (mPosDefault.get(i) + tmp.getWidth() - currentPosition > 0) {

                        float itemPos = mPosDefault.get(i);
                        itemPos -= currentPosition;

                        if (itemPos <= getWidth()) {
                            tmp.setY(getHalfHeight() - tmp.getHalfHeight());// center Pos
                            tmp.setX(itemPos);
                            // Log.debug(log, "Add: " + tmp.getName());
                            if (i == selectedIndex) {
                                tmp.isSelected = true;
                                tmp.resetIsInitialized();
                            }
                            addChild(tmp);
                            addedIndexList.add(tmp.getIndex());
                        } else
                            break;
                    }
                } catch (Exception e) {
                    Log.err(log, "Thread set pos", e);
                }
            }

            // RenderRequest
            GL.that.renderOnce();
            if (selectionChanged)
                scrollToSelectedItem();

        }
    }

    protected void scrollToSelectedItem() {
        if (isDraggable()) {
            Point lastAndFirst = getFirstAndLastVisibleIndex();
            if (!(lastAndFirst.x < selectedIndex && lastAndFirst.y > selectedIndex))
                scrollToItem(selectedIndex);
        } else {
            scrollTo(0);
        }
        selectionChanged = false;
    }

    @Override
    protected void calculateItemPosition() {
        if (mPosDefault != null) {
            mPosDefault.clear();
        } else {
            mPosDefault = new CB_List<>();
        }

        float countPos = getWidth();
        minimumItemSize = getWidth();
        allSize = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            float itemWidth = adapter.getItemSize(i);
            countPos -= itemWidth + dividerSize;
            mPosDefault.add(0, countPos + itemPosOffset);
            allSize += itemWidth + dividerSize;

            if (itemWidth < minimumItemSize)
                minimumItemSize = itemWidth;
        }
        calculateAllSizeBase = countPos - dividerSize;
        currentPosition = countPos - dividerSize;
        maxNumberOfVisibleItems = (int) (getWidth() / minimumItemSize);
        if (maxNumberOfVisibleItems < 1)
            maxNumberOfVisibleItems = 1;

        if (allSize > getWidth()) {
            setDraggable();
        } else {
            setUnDraggable();
        }

    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        if (!isDraggable)
            return false;
        dragged = x - lastTouchInMoveDirection;
        float sollPos = mLastPos_onTouch - dragged;
        float tooMuch = 0;
        if (sollPos - firstItemSize > 0 || sollPos < calculateAllSizeBase) {
            if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < calculateAllSizeBase) {
                if (KineticPan)
                    GL_Input.that.StopKinetic(x, y, pointer, true);
                return true;
            }

            if (sollPos - firstItemSize > 0) {
                tooMuch = 0 - sollPos + firstItemSize;
                tooMuch /= 2;
            } else if (sollPos < calculateAllSizeBase) {
                tooMuch = calculateAllSizeBase - sollPos;
                tooMuch /= 2;
            }
        }
        setListPos(sollPos + tooMuch, KineticPan);
        return true;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        super.onTouchDown(x, y, pointer, button);
        if (!isDraggable)
            return true;
        lastTouchInMoveDirection = x;
        mLastPos_onTouch = currentPosition;
        return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgel�sst wird.
    }

    @Override
    protected void skinIsChanged() {
        reloadItems();
    }

    @Override
    public void notifyDataSetChanged() {
        calculateItemPosition();
        reloadItems();

        if (allSize > getWidth()) {
            setDraggable();
        } else {
            setUnDraggable();
        }

        if (adapter.getCount() <= selectedIndex)
            setSelection(adapter.getCount() - 1);
    }

    @Override
    public void chkSlideBack() {

        if (!isDraggable) {
            startAnimationToBottom();

        } else {
            lastPos = calculateAllSizeBase;

            if (currentPosition > 0)
                startAnimationtoTop();
            else if (currentPosition < calculateAllSizeBase)
                startAnimationToBottom();
        }

    }

    @Override
    protected float getListViewLength() {
        return getWidth();
    }

}
