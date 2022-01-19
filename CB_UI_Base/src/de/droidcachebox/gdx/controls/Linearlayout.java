package de.droidcachebox.gdx.controls;

import java.util.Hashtable;
import java.util.Iterator;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.MoveableList;

public class Linearlayout extends CB_View_Base {

    private float margin;
    private LayoutChanged mLayoutChangedListener;
    private Hashtable<GL_View_Base, Float> sonderMargins;

    /**
     * height is calculated from added childs.
     *
     * @param width ?
     * @param name  ?
     */
    public Linearlayout(float width, String name) {
        super(new CB_RectF(0, 0, width, 0), name);
        margin = (Fonts.measure("T").height) / 2;
    }

    public void setLayoutChangedListener(LayoutChanged listener) {
        mLayoutChangedListener = listener;
    }

    public void layout() {
        if (this.childs == null || this.childs.size() == 0)
            return; // gibt nix zu layouten

        Iterator<GL_View_Base> iterator = this.childs.reverseIterator();
        float lastYPos = margin;
        do {
            GL_View_Base view = iterator.next();
            if (view != null && view.getHeight() > 0) {
                view.setY(lastYPos);
                float itemMargin = margin;
                if (sonderMargins != null) {
                    if (sonderMargins.containsKey(view)) {
                        itemMargin = sonderMargins.get(view);
                    }
                }
                lastYPos += view.getHeight() + itemMargin;
            }
        } while (iterator.hasNext());

        this.setHeight(lastYPos);

        this.setZeroPos();

        if (mLayoutChangedListener != null)
            mLayoutChangedListener.layoutIsChanged(this, lastYPos);

    }

    /**
     * @param view   ?
     * @param margin der Abstand zu den Oberen Item
     * @return ?
     */
    public GL_View_Base addChild(final GL_View_Base view, float margin) {
        if (sonderMargins == null)
            sonderMargins = new Hashtable<>();
        sonderMargins.put(view, margin);
        return addChildDirect(view);
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view) {
        return addChildDirect(view);
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        GL_View_Base v;

        if (last) {
            v = addChildDirectLast(view);
        } else {
            v = addChildDirect(view);
        }
        layout();

        return v;
    }

    @Override
    public void removeChild(final GL_View_Base view) {
        removeChildDirect(view);
        layout();
    }

    @Override
    public void removeChildren() {
        removeChildrenDirect();
        layout();

    }

    @Override
    public void removeChildren(final MoveableList<GL_View_Base> Childs) {
        removeChildrenDirect(Childs);
        layout();
    }

    @Override
    public GL_View_Base addChildDirect(final GL_View_Base view) {
        synchronized (childs) {
            childs.add(view);
            layout();
        }
        return view;
    }

    @Override
    public GL_View_Base addChildDirectLast(final GL_View_Base view) {
        synchronized (childs) {
            childs.add(0, view);
            layout();
        }
        return view;
    }

    @Override
    public void removeChildrenDirect() {
        synchronized (childs) {
            childs.clear();
            layout();
        }
    }

    public interface LayoutChanged {
        void layoutIsChanged(Linearlayout linearLayout, float newHeight);
    }

}
