package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.MoveableList;

/**
 * Scrollbox contains a V_ListView with only the item in the List
 * Height is the visible part
 * virtualHeight is the Height of the item
 * the item holds all Controls of the Scrollbox
 * Prefer to use only one Control (a Box) in the item,
 * so that you can use addNext/addLast (to the box),
 * cause these are not overwritten. (Only addChild() is overwritten)
 * the box should always be at Pos(0,0)
 */
public class ScrollBox extends CB_View_Base {
    protected V_ListView lv;
    protected float virtualHeight;
    protected ListViewItemBase item;
    protected ScrollBoxAdapter thisAdapter;

    public ScrollBox(CB_RectF rec) {
        super(rec, "ScrollBox");
        initScrollBox();
    }

    public ScrollBox(float Width, float Height) {
        super(0, 0, Width, Height, "ScrollBox");
        initScrollBox();
    }

    protected void initScrollBox() {
        virtualHeight = this.getHeight();

        lv = new V_ListView(this, this, "ListView-" + name);
        lv.setClickable(true);
        item = new ListViewItemBase(this, 0, "ListViewItem-" + name) {
            @Override
            protected void initialize() {
                isInitialized = true;
            }
        };
        item.setHeight(virtualHeight);
        item.setClickable(true);
        thisAdapter = new ScrollBoxAdapter();
        lv.setDisposeFlag(false);
        lv.setAdapter(thisAdapter);
        layout();
        this.childs.add(lv);
    }

    protected void layout() {
        if (this.isDisposed())
            return;
        if (lv == null) {
            initScrollBox();
            return;
        } else if (lv.isDisposed())
            return;

        item.setHeight(virtualHeight);
        lv.setSize(innerWidth, innerHeight);
        lv.calculateItemPosition();
        lv.setPos(leftBorder, bottomBorder);
        lv.scrollTo(lv.getScrollPos());
    }

    public float getVirtualHeight() {
        return virtualHeight;
    }

    /**
     * * virtualHeight->Heigth of the item, that takes all placed objects (scrolls if > height)
     **/
    public void setVirtualHeight(float virtualHeight) {
        this.virtualHeight = virtualHeight;
        layout();
    }

    // ################ overrides of CB_View_Base ############################################
    @Override
    public void onResized(CB_RectF rec) {
        lv.setSize(innerWidth, innerHeight);
        item.setWidth(innerWidth);
    }

    @Override
    public int getCildCount() {
        return item.getCildCount();
    }

    @Override
    public GL_View_Base addChildDirekt(final GL_View_Base view) {
        item.addChildDirekt(view);
        lv.notifyDataSetChanged();
        return view;
    }

    @Override
    public GL_View_Base addChildDirektLast(final GL_View_Base view) {
        item.addChildDirektLast(view);
        lv.notifyDataSetChanged();
        return view;
    }

    @Override
    public void removeChildsDirekt(final GL_View_Base view) {
        item.removeChildsDirekt(view);
        lv.notifyDataSetChanged();
    }

    @Override
    public void removeChildsDirekt() {
        item.removeChildsDirekt();
        lv.notifyDataSetChanged();
    }

    @Override
    public GL_View_Base getChild(int i) {
        return item.getChild(i);
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return true; // muss behandelt werden, da sonnst kein onTouchDragged() ausgelöst wird.
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        return true;
    }

    @Override
    protected void initialize() {
        isInitialized = true;
    }

    // ################ overrides of GL_View_Base ############################################
    @Override
    public GL_View_Base addChild(final GL_View_Base view) {
        item.addChildDirekt(view);
        lv.notifyDataSetChanged();
        return view;
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        if (last) {
            item.addChildDirektLast(view);
        } else {
            item.addChildDirekt(view);
        }
        lv.notifyDataSetChanged();
        return view;
    }

    @Override
    public void removeChild(final GL_View_Base view) {
        item.removeChild(view);
        lv.notifyDataSetChanged();
    }

    @Override
    public void removeChilds() {
        item.removeChilds();
        lv.notifyDataSetChanged();
    }

    @Override
    public void removeChilds(final MoveableList<GL_View_Base> Childs) {
        item.removeChilds(Childs);
        lv.notifyDataSetChanged();
    }

    @Override
    public void setBorders(float l, float r) {
        super.setBorders(l, r);
        layout();
    }

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
        layout();
    }

    @Override
    public void setClickable(boolean value) {
        lv.setClickable(value);
        super.setClickable(value);
    }

    @Override
    public void setLongClickable(boolean value) {
        lv.setLongClickable(value);
        super.setLongClickable(value);
    }

    public float getScrollY() {
        return lv.getScrollPos();
    }

    public void scrollTo(float scrollPos) {
        lv.scrollTo(scrollPos);
    }

    public class ScrollBoxAdapter implements Adapter {
        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public ListViewItemBase getView(int position) {
            return item;
        }

        @Override
        public float getItemSize(int position) {
            return item.getHeight();
        }
    }

}
