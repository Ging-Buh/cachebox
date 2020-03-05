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
package de.droidcachebox.gdx.controls.list;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class V_ListView extends ListViewBase {

    AtomicBoolean isInCalculation = new AtomicBoolean(false);

    public V_ListView(CB_RectF rec, String name) {
        super(rec, name);
    }

    public V_ListView(CB_RectF rec, GL_View_Base parent, String name) {
        super(rec, parent, name);
    }

    @Override
    protected void renderThreadSetPos(float value, boolean Kinetic) {
        synchronized (childs) {
            currentPosition = value;
            clearList.clear();

            // alle childs verschieben

            if (mReloadItems) {
                addedIndexList.clear();
                if (mCanDispose) {
                    synchronized (childs) {
                        for (int i = 0, n = childs.size(); i < n; i++) {
                            childs.get(i).dispose();
                        }
                    }
                }
                removeChilds();
            } else {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    ListViewItemBase tmp = (ListViewItemBase) childs.get(i);
                    float itemPos = mPosDefault.get(tmp.getIndex());
                    itemPos -= currentPosition;
                    tmp.setY(itemPos);

                    if (!isTouch) {
                        if (tmp.getY() > getMaxY() || tmp.getMaxY() < 0) {
                            // Item ist nicht mehr im sichtbaren Bereich!
                            clearList.add(tmp);
                        }
                    }
                }

            }

            mReloadItems = false;

            // aufräumen
            if (clearList.size() > 0) {
                synchronized (addedIndexList) {
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
                }
            }
        }

        // setze First Index, damit nicht alle Items durchlaufen werden müssen
        synchronized (addedIndexList) {
            addedIndexList.sort();
            if (addedIndexList.size() > 0) {
                firstIndex = addedIndexList.get(0) - maxNumberOfVisibleItems;
                if (firstIndex < 0)
                    firstIndex = 0;
            } else {
                firstIndex = 0;
            }

            if (lastIndex == firstIndex)
                firstIndex = 0;

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
     * @param kinetic ?
     */
    @Override
    protected void addVisibleItems(boolean kinetic) {

        try {

            if (adapter == null) return;
            if (isDisposed()) return;
            if (mPosDefault == null)
                calculateItemPosition();

            final float workPos = currentPosition;

            synchronized (childs) {
                int ende = adapter.getCount();
                for (int i = firstIndex; i < ende; i++) {
                    if (!addedIndexList.contains(i)) {
                        if (mPosDefault.size() - 1 < i)
                            return;

                        float itemPos = mPosDefault.get(i);
                        itemPos -= workPos;

                        if (itemPos < getMaxY() && itemPos + adapter.getItemSize(i) > -(maxNumberOfVisibleItems * minimumItemSize)) {
                            ListViewItemBase tmp = adapter.getView(i);
                            if (tmp != null) {
                                tmp.setY(itemPos);
                                if (i == selectedIndex) {
                                    tmp.isSelected = true;
                                    tmp.resetIsInitialized();
                                }
                                addChild(tmp);
                            }

                            // Log.debug(log, "Add Item " + i);
                            addedIndexList.add(i);
                        } else {
                            try {
                                if (adapter == null) {
                                    Log.err("V_ListView", new Exception("unexpected adapter = null"));
                                    return;
                                }
                                float itemSize = adapter.getItemSize(i);
                                if (itemPos + itemSize < -(maxNumberOfVisibleItems * minimumItemSize)) {
                                    lastIndex = i;
                                    break;
                                }
                            } catch (Exception ex) {
                                Log.err("V_ListView", ex);
                                break;
                            }
                        }

                    }

                    // RenderRequest
                    GL.that.renderOnce();

                    if (selectionChanged) {
                        Point lastAndFirst = getFirstAndLastVisibleIndex();

                        //						if (lastAndFirst.y == -1)
                        //						{
                        //							scrollTo(0);
                        //							selectionchanged = false;
                        //							return;
                        //						}

                        if (isDraggable()) {
                            if (!(lastAndFirst.x < selectedIndex && lastAndFirst.y > selectedIndex))
                                scrollToItem(selectedIndex);
                        } else {
                            scrollTo(0);
                        }
                        selectionChanged = false;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void calculateItemPosition() {
        if (isDisposed() || isInCalculation.get()) {
            return;
        }
        isInCalculation.set(true);
        if (adapter == null) {
            isInCalculation.set(false);
            return; // can't calc
        }

        try {
            if (mPosDefault != null) {
                mPosDefault.clear();
            } else {
                mPosDefault = new CB_List<>();
            }

            minimumItemSize = getHeight();

            float countPos = getHeight() - dividerSize;

            allSize = 0;
            if (hasInvisibleItems) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    float itemHeight;
                    ListViewItemBase item = adapter.getView(i);
                    if (item != null && item.isVisible() && item.getHeight() > 0) {
                        itemHeight = adapter.getItemSize(i);
                        countPos -= itemHeight + dividerSize;
                        if (itemHeight < minimumItemSize)
                            minimumItemSize = itemHeight;
                        allSize += itemHeight + dividerSize;
                    }

                    mPosDefault.add(countPos + itemPosOffset);

                }
            } else {
                for (int i = 0; i < adapter.getCount(); i++) {
                    float itemHeight = adapter.getItemSize(i);
                    countPos -= itemHeight + dividerSize;
                    if (itemHeight < minimumItemSize)
                        minimumItemSize = itemHeight;
                    allSize += itemHeight + dividerSize;
                    mPosDefault.add(countPos + itemPosOffset);

                }
            }

            calculateAllSizeBase = countPos - dividerSize;
            maxNumberOfVisibleItems = (int) (getHeight() / minimumItemSize);
            if (maxNumberOfVisibleItems < 1)
                maxNumberOfVisibleItems = 1;

            if (allSize > getHeight()) {
                setDraggable();
            } else {
                setUnDraggable();
            }
        } catch (Exception ex) {
            Log.err("V_ListView", ex);
        }

        isInCalculation.set(false);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        if (!isDraggable)
            return false;
        dragged = y - lastTouchInMoveDirection;

        float sollPos = mLastPos_onTouch - dragged;
        float toMuch = 0;
        if (sollPos - firstItemSize > 0 || sollPos < calculateAllSizeBase) {
            if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < calculateAllSizeBase) {
                if (KineticPan)
                    GL_Input.that.StopKinetic(x, y, pointer, true);
                return true;
            }

            if (sollPos - firstItemSize > 0) {
                toMuch = 0 - sollPos + firstItemSize;
                toMuch /= 2;
            } else if (sollPos < calculateAllSizeBase) {
                toMuch = calculateAllSizeBase - sollPos;
                toMuch /= 2;
            }
        }

        setListPos(sollPos + toMuch, KineticPan);
        return false;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        // super.onTouchDown(x, y, pointer, button);
        if (!isDraggable)
            return true;
        lastTouchInMoveDirection = y;
        mLastPos_onTouch = currentPosition;
        return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelösst wird.
    }

    @Override
    protected void skinIsChanged() {
        reloadItems();
    }

    @Override
    public void notifyDataSetChanged() {
        calculateItemPosition();
        reloadItems();
        if (adapter != null && adapter.getCount() <= selectedIndex)
            setSelection(adapter.getCount() - 1);
    }

    @Override
    protected float getListViewLength() {
        return getHeight();
    }

}
