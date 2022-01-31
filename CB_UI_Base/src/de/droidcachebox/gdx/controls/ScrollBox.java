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
 * Scrollbox contains a V_ListView with only the contentItem in the List
 * Height is the visible part
 * virtualHeight is the Height of the contentItem
 * the contentItem holds all Controls of the Scrollbox
 * Prefer to use only one Control (a Box) in the contentItem,
 * so that you can use addNext/addLast (to the box),
 * cause these are not overwritten. (Only addChild() is overwritten)
 * the box should always be at Pos(0,0)
 */
public class ScrollBox extends CB_View_Base {
    protected V_ListView scrollBoxContent;
    protected ListViewItemBase contentItem;
    protected ScrollBoxAdapter scrollBoxAdapter;
    protected float virtualHeight;

    public ScrollBox(CB_RectF rec) {
        super(rec, "ScrollBox");
        initScrollBox();
    }

    public ScrollBox(float Width, float Height) {
        this(new CB_RectF(0, 0, Width, Height));
    }

    protected void initScrollBox() {
        virtualHeight = getHeight();

        scrollBoxContent = new V_ListView(this, this, "ListView-" + name);
        scrollBoxContent.setClickable(true);
        contentItem = new ListViewItemBase(this, 0, "ListViewItem-" + name) {
            @Override
            protected void renderInit() {
                isRenderInitDone = true;
            }
        };
        contentItem.setHeight(virtualHeight);
        contentItem.setClickable(true);
        scrollBoxAdapter = new ScrollBoxAdapter();
        scrollBoxContent.setDisposeFlag(false);
        scrollBoxContent.setAdapter(scrollBoxAdapter);
        layout();
        childs.add(scrollBoxContent);
    }

    protected void layout() {
        if (isDisposed)
            return;
        if (scrollBoxContent == null) {
            initScrollBox();
            return;
        } else if (scrollBoxContent.isDisposed())
            return;

        contentItem.setHeight(virtualHeight);
        scrollBoxContent.setSize(innerWidth, innerHeight);
        scrollBoxContent.calculateItemPosition();
        scrollBoxContent.setPos(leftBorder, bottomBorder);
        scrollBoxContent.scrollTo(scrollBoxContent.getScrollPos());
    }

    public float getVirtualHeight() {
        return virtualHeight;
    }

    /**
     * * virtualHeight->Heigth of the item, that takes all placed objects (scrolls if > height)
     **/
    public void setVirtualHeight(float newVirtualHeight) {
        virtualHeight = newVirtualHeight;
        layout();
    }

    // ################ overrides of CB_View_Base ############################################
    @Override
    public void onResized(CB_RectF rec) {
        scrollBoxContent.setSize(innerWidth, innerHeight);
        contentItem.setWidth(innerWidth);
    }

    @Override
    public int getChildCount() {
        return contentItem.getChildCount();
    }

    @Override
    public GL_View_Base addChildDirect(final GL_View_Base view) {
        contentItem.addChildDirect(view);
        scrollBoxContent.notifyDataSetChanged();
        return view;
    }

    @Override
    public GL_View_Base addChildDirectLast(final GL_View_Base view) {
        contentItem.addChildDirectLast(view);
        scrollBoxContent.notifyDataSetChanged();
        return view;
    }

    @Override
    public void removeChildDirect(final GL_View_Base view) {
        contentItem.removeChildDirect(view);
        scrollBoxContent.notifyDataSetChanged();
    }

    @Override
    public void removeChildrenDirect() {
        contentItem.removeChildrenDirect();
        scrollBoxContent.notifyDataSetChanged();
    }

    @Override
    public GL_View_Base getChild(int i) {
        return contentItem.getChild(i);
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
    protected void renderInit() {
        isRenderInitDone = true;
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view) {
        contentItem.addChildDirect(view);
        scrollBoxContent.notifyDataSetChanged();
        return view;
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        if (last) {
            contentItem.addChildDirectLast(view);
        } else {
            contentItem.addChildDirect(view);
        }
        scrollBoxContent.notifyDataSetChanged();
        return view;
    }

    @Override
    public void removeChild(final GL_View_Base child) {
        contentItem.removeChild(child);
        scrollBoxContent.notifyDataSetChanged();
    }

    @Override
    public void removeChildren() {
        contentItem.removeChildren();
        scrollBoxContent.notifyDataSetChanged();
    }

    @Override
    public void removeChildren(final MoveableList<GL_View_Base> Childs) {
        contentItem.removeChildren(Childs);
        scrollBoxContent.notifyDataSetChanged();
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
        scrollBoxContent.setClickable(value);
        super.setClickable(value);
    }

    @Override
    public void setLongClickable(boolean value) {
        scrollBoxContent.setLongClickable(value);
        super.setLongClickable(value);
    }

    public float getScrollY() {
        return scrollBoxContent.getScrollPos();
    }

    public void scrollTo(float scrollPos) {
        scrollBoxContent.scrollTo(scrollPos);
    }

    public class ScrollBoxAdapter implements Adapter {
        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public ListViewItemBase getView(int position) {
            return contentItem;
        }

        @Override
        public float getItemSize(int position) {
            return contentItem.getHeight();
        }
    }

}
