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
package de.droidcachebox.gdx.controls;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.controls.list.H_ListView;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri
 */
public class GalleryView extends H_ListView {
    private static final String log = "GalleryView";

    private boolean showSelectedItemCenter = false;

    public GalleryView(CB_RectF rec, String Name) {
        super(rec, Name);
        mCanDispose = false;
    }

    public void showSelectedItemCenter(boolean value) {
        showSelectedItemCenter = value;
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
            mPosDefault.add(0, countPos - itemPosOffset);
            allSize += itemWidth + dividerSize;

            if (itemWidth < minimumItemSize)
                minimumItemSize = itemWidth;
        }
        calculateAllSizeBase = countPos - dividerSize;
        currentPosition = countPos - dividerSize;
        maxNumberOfVisibleItems = (int) (getWidth() / minimumItemSize);
        if (maxNumberOfVisibleItems < 1)
            maxNumberOfVisibleItems = 1;
    }

    @Override
    public void chkSlideBack() {

        if (showSelectedItemCenter) {
            // this implementation is not compatible with click selection

            //search item at center and set as selected

            float itemWidth = adapter.getItemSize(0);

            int n = mPosDefault.size() - 1;
            for (int i = 0; i < n; i++) {
                final float pos1 = mPosDefault.get(i) - getHalfWidth() + (itemWidth / 2);
                final float pos2 = mPosDefault.get(i + 1) - getHalfWidth() + (itemWidth / 2);

                if (currentPosition > pos1 && currentPosition < pos2 || currentPosition < 0 || currentPosition > allSize + itemWidth) {
                    //search max Div
                    final float div1 = Math.abs(currentPosition - pos1);
                    final float div2 = Math.abs(currentPosition - pos2);

                    if (div1 <= div2) {
                        // setSelection(i);
                        adapter.getView(i).click(0, 0, 0, 0);
                        scrollItemToCenter(i);
                    } else {
                        // setSelection(i + 1);
                        adapter.getView(i + 1).click(0, 0, 0, 0);
                        scrollItemToCenter(i + 1);
                    }
                    break;
                }
            }
            return;
        }

        /*
        // what does this do?
        if (mIsDraggable) {
            lastPos = mcalcAllSizeBase;
            if (mPos > 0) {
                startAnimationtoTop();
                snapIn(mBaseAdapter.getCount() - 1);
            } else if (mPos < mcalcAllSizeBase) {
                startAnimationToBottom();
                snapIn(0);
            }
        } else {
            startAnimationToBottom();
        }

         */

        //SnapIN?
        if (mPosDefault == null) return;

        for (int i = 0, n = mPosDefault.size() - 1; i < n; i++) {
            final float pos1 = mPosDefault.get(i);
            final float pos2 = mPosDefault.get(i + 1);
            if (currentPosition > pos1 && currentPosition < pos2) {
                //search max Div
                final float div1 = Math.abs(currentPosition - pos1);
                final float div2 = Math.abs(currentPosition - pos2);
                final int idx = i;
                GL.that.runOnGL(() -> {
                    if (div1 <= div2) {
                        //Snap to 1
                        bottomAnimation = false;
                        scrollTo(pos1);
                        Log.debug(log, "SnapIn first " + pos1);
                        snapIn(idx);
                    } else {
                        //Snap to 2
                        bottomAnimation = true;
                        scrollTo(pos2);
                        Log.debug(log, "SnapIn second " + pos2);
                        snapIn(idx + 1);
                    }
                });
                break;
            }
        }
    }

    public void reloadItemsNow() {
        removeChildrenDirect();
        addedIndexList.clear();
        addVisibleItems(true);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        if (!isDraggable)
            return false;
        dragged = x - lastTouchInMoveDirection;
        float sollPos = mLastPos_onTouch - dragged;
        if (sollPos - firstItemSize > 0 || sollPos < calculateAllSizeBase) {
            if (sollPos - (firstItemSize * 3) > 0 || sollPos + (lastItemSize * 3) < calculateAllSizeBase) {
                if (KineticPan)
                    GL_Input.that.StopKinetic(x, y, pointer, true);
                return true;
            }
        }
        setListPos(sollPos, KineticPan);
        return true;
    }

    @Override
    public void onResized(CB_RectF rec) {
        // don't call super.onResized()
        // this will change posList
    }

    public void snapIn(int index) {
    }

    @Override
    protected void scrollToSelectedItem() {
    }

    public void scrollItemToCenter(int idx) {
        if (idx < 0)
            return;
        float defaultpos = mPosDefault.get(idx);
        float sollpos = defaultpos - getHalfWidth() + (adapter.getItemSize(idx) / 2);
        bottomAnimation = sollpos > currentPosition;
        scrollTo(sollpos);
        selectedIndex = idx;
    }

    @Override
    public ListViewItemBase getSelectedItem() {
        if (adapter == null)
            return null;

        //get selected idx

        int idx = 0;
        for (int n = adapter.getCount(); idx < n; idx++) {
            if (mPosDefault.get(idx) > currentPosition - 50 && mPosDefault.get(idx) < currentPosition + 50)
                break;
        }

        if (idx == -1)
            return null;
        if (idx >= adapter.getCount())
            return null;
        return adapter.getView(idx);
    }
}
