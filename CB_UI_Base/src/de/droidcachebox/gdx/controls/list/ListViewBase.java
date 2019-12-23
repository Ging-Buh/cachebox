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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.MoveableList;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ListViewBase extends CB_View_Base implements IScrollbarParent {
    private static final String log = "ListViewBase";
    private final CB_List<IListPosChanged> EventHandlerList;
    private Scrollbar scrollbar;
    private MoveableList<GL_View_Base> noListChilds;
    protected Boolean mBottomAnimation;
    protected int mSelectedIndex;
    protected float firstItemSize;
    protected float lastItemSize;
    boolean hasInvisibleItems;
    boolean isTouch;
    private final CB_List<IRunOnGL> runOnGL_List;
    private final CB_List<IRunOnGL> runOnGL_ListWaitpool;
    private AtomicBoolean isWorkOnRunOnGL;
    CB_List<ListViewItemBase> clearList;
    float lastPos;
    protected float mItemPosOffset;
    /**
     * Wen True, können die Items verschoben werden
     */
    protected Boolean mIsDraggable;
    /**
     * Ermöglicht den Zugriff auf die Liste, welche Dargestellt werden soll.
     */
    protected Adapter adapter;
    /**
     * Enthällt die Indexes, welche schon als Child exestieren.
     */
    protected CB_List<Integer> mAddedIndexList;
    /**
     * Aktuelle Position der Liste
     */
    protected float mPos;
    /**
     * Der Start Index, ab dem gesucht wird, ob ein Item in den Sichtbaren Bereich geschoben wurde. Damit nicht eine Liste von 1000 Items
     * abgefragt werden muss wenn nur die letzten 5 sichtbar sind.
     */
    int mFirstIndex;
    int mLastIndex;
    /**
     * Die Anzahl der Items, welche gleichzeitig dargestellt werden kann, wenn alle Items so groß sind wie das kleinste Item in der List.
     */
    protected int mMaxItemCount;
    protected float minimumItemSize;
    protected float mcalcAllSizeBase;
    /**
     * Komplette Breite oder Höhe aller Items
     */
    protected float mAllSize;
    protected float mDividerSize;
    boolean mMustSetPosKinetic;
    protected boolean mMustSetPos;
    private float mMustSetPosValue;
    protected CB_List<Float> mPosDefault;
    /**
     * Wenn True, werden die Items beim verlassen des sichtbaren Bereiches disposed und auf NULL gesetzt.
     */
    protected boolean mCanDispose;
    protected int mDragged;
    protected int mLastTouch;
    protected float mLastPos_onTouch;
    private String mEmptyMsg;
    private BitmapFontCache emptyMsg;
    boolean mReloadItems;
    boolean selectionChanged;
    private float mAnimationTarget;
    private Timer mAnimationTimer;

    ListViewBase(CB_RectF rec, String Name) {
        super(rec, Name);
        setClickable(true);
        EventHandlerList = new CB_List<>();
        noListChilds = new MoveableList<>();
        runOnGL_List = new CB_List<>();
        runOnGL_ListWaitpool = new CB_List<>();
        isWorkOnRunOnGL = new AtomicBoolean(false);
        clearList = new CB_List<>();
        lastPos = 0;
        mBottomAnimation = false;
        mSelectedIndex = -1;
        firstItemSize = -1;
        lastItemSize = -1;
        hasInvisibleItems = false;
        isTouch = false;
        mItemPosOffset = 0;
        mIsDraggable = true;
        mAddedIndexList = new CB_List<>();
        mPos = 0;
        mFirstIndex = 0;
        mLastIndex = 0;
        mMaxItemCount = -1;
        minimumItemSize = 0;
        mcalcAllSizeBase = 0f;
        mAllSize = 0f;
        mDividerSize = 2f;
        mMustSetPosKinetic = false;
        mMustSetPos = false;
        mMustSetPosValue = 0;
        mCanDispose = true;
        mDragged = 0;
        mLastTouch = 0;
        mLastPos_onTouch = 0;
        mEmptyMsg = "";
        mReloadItems = false;
        selectionChanged = false;
        mAnimationTarget = 0;
    }

    ListViewBase(CB_RectF rec, GL_View_Base parent, String name) {
        super(rec, parent, name);
        this.setClickable(true);
        EventHandlerList = new CB_List<>();
        noListChilds = new MoveableList<>();
        runOnGL_List = new CB_List<>();
        runOnGL_ListWaitpool = new CB_List<>();
        isWorkOnRunOnGL = new AtomicBoolean(false);
        clearList = new CB_List<>();
        lastPos = 0;
        mBottomAnimation = false;
        mSelectedIndex = -1;
        firstItemSize = -1;
        lastItemSize = -1;
        hasInvisibleItems = false;
        isTouch = false;
        mItemPosOffset = 0;
        mIsDraggable = true;
        mAddedIndexList = new CB_List<>();
        mPos = 0;
        mFirstIndex = 0;
        mLastIndex = 0;
        mMaxItemCount = -1;
        minimumItemSize = 0;
        mcalcAllSizeBase = 0f;
        mAllSize = 0f;
        mDividerSize = 2f;
        mMustSetPosKinetic = false;
        mMustSetPos = false;
        mMustSetPosValue = 0;
        mCanDispose = true;
        mDragged = 0;
        mLastTouch = 0;
        mLastPos_onTouch = 0;
        mEmptyMsg = null;
        mReloadItems = false;
        selectionChanged = false;
        mAnimationTarget = 0;
    }

    /**
     * Return With for horizontal and Height for vertical ListView
     *
     */
    protected abstract float getListViewLength();

    public void addListPosChangedEventHandler(IListPosChanged handler) {
        if (!EventHandlerList.contains(handler))
            EventHandlerList.add(handler);
    }

    public void runIfListInitial(IRunOnGL run) {

        // if in progress put into pool
        if (isWorkOnRunOnGL.get()) {
            runOnGL_ListWaitpool.add(run);
            GL.that.renderOnce();
            return;
        }
        synchronized (runOnGL_List) {
            runOnGL_List.add(run);
        }

        GL.that.renderOnce();
    }

    void callListPosChangedEvent() {
        for (int i = 0, n = EventHandlerList.size(); i < n; i++) {
            IListPosChanged handler = EventHandlerList.get(i);
            if (handler != null)
                handler.ListPosChanged();
        }
    }

    @Override
    public float getAllListSize() {
        return mAllSize;
    }

    @Override
    public void setListPos(float value) {
        this.setListPos(value, false);
    }

    public void setEmptyMsg(String Msg) {
        mEmptyMsg = Msg;
        emptyMsg = null;
        GL.that.renderOnce();
    }

    /**
     * Setzt ein Flag, welches angibt, ob dies ListView Invisible Items hat. Da die Berechnung der Positionen deutlich länger dauert, ist
     * der Standard auf False gesetzt.
     *
     */
    protected void setHasInvisibleItems() {
        hasInvisibleItems = true;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;

        if (mAddedIndexList != null)
            mAddedIndexList.clear();
        if (mCanDispose) {
            synchronized (childs) {
                try {
                    for (int i = 0, n = childs.size(); i < n; i++) {
                        if (i >= childs.size())
                            break;
                        childs.get(i).dispose();
                    }
                } catch (Exception ignored) {
                }
            }
        }
        this.removeChilds();

        if (this.adapter != null) {
            calcDefaultPosList();
            reloadItems();
            firstItemSize = this.adapter.getItemSize(0);
            lastItemSize = this.adapter.getItemSize(this.adapter.getCount() - 1);
        }

    }

    /**
     * distance between 2 items
     *
     */
    public void setDividerSize(float value) {
        mDividerSize = value;
        calcDefaultPosList();

        // Items neu laden
        reloadItems();

    }

    public void reloadItems() {
        mReloadItems = true;

        // Position setzen, damit die items neu geladen werden
        setListPos(mPos, false);
        // Log.debug(log, "SetListPos Relod Items");
        GL.that.renderOnce();

    }

    /**
     * Setzt die ListView in in den UnDraggable Modus
     */
    public void setUnDraggable() {
        mPos = 0;
        mIsDraggable = false;
    }

    public void setDraggable() {
        mIsDraggable = true;
    }

    public void setDisposeFlag(boolean canDispose) {
        mCanDispose = canDispose;
    }

    protected void setListPos(float value, boolean Kinetic) {
        mMustSetPosValue = value;
        mMustSetPos = true;
        mMustSetPosKinetic = Kinetic;
        GL.that.renderOnce();
    }

    protected abstract void renderThreadSetPos(float value, boolean Kinetic);

    /**
     * added die sichtbaren Items als Child und speichert den Index in einer Liste, damit das Item nicht ein zweites mal hinzugefügt wird.
     * Wenn Kinetic == True werden mehr Items geladen, damit beim schnellen Scrollen die Items schon erstellt sind, bevor sie in den
     * sichtbaren Bereich kommen.
     *
     */
    protected abstract void addVisibleItems(boolean Kinetic);

    /**
     * Fragt die Höhen aller Items ab und speichert die damit berechneten Positonen ab.
     */
    protected abstract void calcDefaultPosList();

    @Override
    public void onResized(CB_RectF rec) {
        // Items neu laden
        calcDefaultPosList();
        mMustSetPos = true;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        isTouch = false;
        chkSlideBack();
        return true;
    }

    @Override
    protected void render(Batch batch) {

        if (!isInitialized) {
            isInitialized = true;
            initialize();
            // return; why? after initializing it should be possible to render
        }

        if (this.childs.size() == 0 && (this.adapter == null || this.adapter.getCount() == 0)) {
            try {
                if (emptyMsg == null && mEmptyMsg != null) {
                    emptyMsg = new BitmapFontCache(Fonts.getBig());
                    GlyphLayout bounds = emptyMsg.setText(mEmptyMsg, 0f, 0f, this.getWidth(), Align.left, true);
                    emptyMsg.setPosition(this.getHalfWidth() - (bounds.width / 2), this.getHalfHeight() - (bounds.height / 2));
                }
            } catch (Exception ex) {
                Log.err(log, "render", ex);
            }
            if (emptyMsg != null)
                emptyMsg.draw(batch, 0.5f);
        } else {
            try {
                super.render(batch);
                if (mMustSetPos) {
                    renderThreadSetPos(mMustSetPosValue, mMustSetPosKinetic);
                } else {
                    // Run WorkPool
                    isWorkOnRunOnGL.set(true);
                    synchronized (runOnGL_List) {
                        if (runOnGL_List.size() > 0) {
                            for (int i = 0, n = runOnGL_List.size(); i < n; i++) {
                                IRunOnGL run = runOnGL_List.get(i);
                                if (run != null)
                                    run.run();
                            }

                            runOnGL_List.clear();
                        }
                    }
                    isWorkOnRunOnGL.set(false);
                    // work RunOnGlPool
                    synchronized (runOnGL_ListWaitpool) {
                        if (runOnGL_ListWaitpool.size() > 0) {
                            if (runOnGL_ListWaitpool.size() > 0) {
                                for (int i = 0, n = runOnGL_ListWaitpool.size(); i < n; i++) {
                                    IRunOnGL run = runOnGL_ListWaitpool.get(i);
                                    if (run != null)
                                        run.run();
                                }

                                runOnGL_ListWaitpool.clear();
                            }

                        }
                    }
                }
            } catch (Exception ignored) {
                // Log.err(log,"");
            }
        }

    }

    @Override
    public void renderChilds(final Batch batch, ParentInfo parentInfo) {
        try {
            super.renderChilds(batch, parentInfo);
        } catch (Exception ex) {
            Log.err(log, "renderChilds", ex);
        }

    }

    /**
     * überpüft ob die Liste oben oder unten Platz hat und lässt eine Animation aus, in der die Liste auf die erste oder letzte Position
     * scrollt.
     */
    @Override
    public void chkSlideBack() {
        if (!mIsDraggable) {
            startAnimationtoTop();
            return;
        }
        if (mPos > 0)
            startAnimationtoTop();
        else if (mPos < mcalcAllSizeBase) {
            lastPos = mcalcAllSizeBase;
            startAnimationToBottom();
        }

    }

    @Override
    public boolean isDraggable() {
        return mIsDraggable;
    }

    @Override
    public abstract boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan);

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        // isTouch = true;
        return true;
    }

    void startAnimationtoTop() {
        if (adapter == null)
            return;
        mBottomAnimation = false;
        float firstPos = 0;
        scrollTo(firstPos);
    }

    void startAnimationToBottom() {
        if (adapter == null)
            return;
        mBottomAnimation = true;
        scrollTo(lastPos);
    }

    public void scrollToItem(int i) {
        if (mPosDefault == null || adapter == null)
            return;

        Point lastAndFirst = getFirstAndLastVisibleIndex();

        if (lastAndFirst.y == -1) {
            setListPos(0);
            return;
        }

        float versatz = (i < lastAndFirst.y) ? -getListViewLength() + this.adapter.getItemSize(i) : 0;

        try {
            if (i >= 0 && i < mPosDefault.size()) {
                setListPos(mPosDefault.get(i) + versatz, true);
            } else {
                setListPos(mPosDefault.get(mPosDefault.size() - 1), true);
            }
        } catch (Exception ex) {
            Log.err(log, "scroll to item", ex);
        }

    }

    public void scrollTo(float Pos) {

        mAnimationTarget = Pos;
        stopTimer();

        mAnimationTimer = new Timer();
        try {
            long ANIMATION_TICK = 50;
            mAnimationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    TimerMethod();
                }

                private void TimerMethod() {
                    float newPos = mPos - ((mPos - mAnimationTarget) / 2);
                    if ((!mBottomAnimation && mAnimationTarget + 1.5 > mPos) || (mBottomAnimation && mAnimationTarget - 1.5 < mPos)) {
                        if (mAnimationTimer == null) {
                            this.cancel();
                            return;
                        }
                        setListPos(mAnimationTarget, true);
                        //Log.info(log, "ListAnimation ready mPos=" + mPos);

                        stopTimer();
                        return;
                    }

                    // Log.debug(log, "Set Animatet ListPos");
                    setListPos(newPos, true);
                }

            }, 0, ANIMATION_TICK);
        } catch (Exception ignored) {
        }
    }

    private void stopTimer() {
        try {
            if (mAnimationTimer != null) {
                mAnimationTimer.cancel();
            }
        } catch (Exception e) {
            mAnimationTimer = null;
        } finally {
            mAnimationTimer = null;
        }
    }

    public float getDividerHeight() {
        return mDividerSize;
    }

    public ListViewItemBase getSelectedItem() {
        if (adapter == null)
            return null;
        if (mSelectedIndex == -1)
            return null;
        if (mSelectedIndex >= adapter.getCount())
            return null;
        return adapter.getView(mSelectedIndex);
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelection(int i) {
        if (mSelectedIndex != i && i >= 0) {
            selectionChanged = true;
            synchronized (childs) {

                for (int j = 0, m = childs.size(); j < m; j++) {
                    GL_View_Base v = childs.get(j);
                    if (v instanceof ListViewItemBase) {
                        if (((ListViewItemBase) v).getIndex() == mSelectedIndex) {
                            ((ListViewItemBase) v).isSelected = false;
                            break;
                        }
                    }
                }
                mSelectedIndex = i;
                for (int j = 0, m = childs.size(); j < m; j++) {
                    GL_View_Base v = childs.get(j);
                    if (v instanceof ListViewItemBase) {
                        if (((ListViewItemBase) v).getIndex() == mSelectedIndex) {
                            ((ListViewItemBase) v).isSelected = true;
                            break;
                        }
                    }
                }

                // alle Items löschen, damit das Selection flag neu gesetzt werden kann.
                if (childs.size() == 0) {
                    reloadItems();
                }
            }
            GL.that.renderOnce();

        }

    }

    /**
     * Returns a Point<br>
     * x= first full visible Index<br>
     * y= last full visible Index<br>
     *
     */
    public Point getFirstAndLastVisibleIndex() {
        Point ret = new Point();
        synchronized (childs) {

            CB_List<ListViewItemBase> visibleList = new CB_List<>();

            for (int j = 0, m = childs.size(); j < m; j++) {
                GL_View_Base v = childs.get(j);
                if (v instanceof ListViewItemBase) {
                    visibleList.add(((ListViewItemBase) v));
                }
            }

            if (visibleList.isEmpty()) {
                ret.x = -1;
                ret.y = -1;
                return ret;
            }

            visibleList.sort();
            boolean foundFirstVisible = false;
            int lastFoundedVisible = 0;
            for (int j = 0, m = visibleList.size(); j < m; j++) {
                ListViewItemBase lv = visibleList.get(j);
                if (this.thisWorldRec.contains(lv.thisWorldRec)) {
                    if (!foundFirstVisible)
                        ret.x = lv.getIndex();
                    foundFirstVisible = true;
                    lastFoundedVisible = lv.getIndex();
                } else {
                    if (!foundFirstVisible) {
                        continue;
                    }
                    ret.y = lastFoundedVisible;
                    break;
                }
            }

        }

        // Log.debug(log, "getLastVisiblePosition = " + ret);

        return ret;
    }

    /**
     * Gibt die Anzahl der Items, welche gleichzeitig dargestellt werden können, wenn alle Items so groß sind wie das kleinste Item in der
     * List, zurück.
     */
    public int getMaxItemCount() {
        return mMaxItemCount;
    }

    public abstract void notifyDataSetChanged();

    @Override
    public float getScrollPos() {
        return mPos;
    }

    @Override
    public CB_View_Base getView() {
        return this;
    }

    @Override
    public float getFirstItemSize() {
        return firstItemSize;
    }

    @Override
    public float getLastItemSize() {
        return lastItemSize;
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        if (childs.contains(view)) {
            // Remove first
            childs.remove(view);
        }
        GL.that.RunOnGL(() -> {
            if (last) {
                childs.add(0, view);
            } else {
                childs.add(view);
            }
            chkChildClickable();
        });

        return view;
    }

    @Override
    public void removeChild(final GL_View_Base view) {
        GL.that.RunOnGL(() -> {
            try {
                if (childs.size() > 0)
                    childs.remove(view);
            } catch (Exception ignored) {
            }
            chkChildClickable();
        });
    }

    @Override
    public void removeChilds() {

        GL.that.RunOnGLWithThreadCheck(() -> {
            try {
                if (childs.size() > 0)
                    childs.clear();
            } catch (Exception ignored) {
            }
            chkChildClickable();
        });
    }

    @Override
    public void removeChilds(final MoveableList<GL_View_Base> Childs) {
        GL.that.RunOnGLWithThreadCheck(() -> {
            try {
                if (childs.size() > 0)
                    childs.remove(Childs);
            } catch (Exception ignored) {
            }
            chkChildClickable();
        });
    }

    @Override
    public void dispose() {
        if (scrollbar != null)
            scrollbar.dispose();
        scrollbar = null;

        if (noListChilds != null) {
            for (int i = 0; i < noListChilds.size(); i++) {
                noListChilds.get(i).dispose();
            }
            noListChilds.clear();
        }

        if (clearList != null) {
            for (int i = 0; i < clearList.size(); i++) {
                clearList.get(i).dispose();
            }
            clearList.clear();
        }
        clearList = null;

        runOnGL_List.clear();
        runOnGL_ListWaitpool.clear();
        if (mAddedIndexList != null) {
            mAddedIndexList.clear();
        }
        mAddedIndexList = null;
        if (mPosDefault != null) {
            mPosDefault.clear();
        }
        mPosDefault = null;

        mAnimationTimer = null;

        isWorkOnRunOnGL = null;

        adapter = null;
        super.dispose();
    }

    public interface IListPosChanged {
        void ListPosChanged();
    }
}
