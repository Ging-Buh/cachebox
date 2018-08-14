package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Util.MoveableList;

import java.util.Hashtable;
import java.util.Iterator;

public class Linearlayout extends CB_View_Base {

    private float margin = 5;
    private LayoutChanged mLayoutChangedListener;
    private Hashtable<GL_View_Base, Float> sonderMargins;

    /**
     * H�he wird von den zugef�gten Items bestimmt.
     *
     * @param width
     * @param Name
     */
    public Linearlayout(float width, String Name) {
        super(new CB_RectF(0, 0, width, 0), Name);
        margin = (Fonts.Measure("T").height) / 2;
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
                float ItemMargin = margin;
                if (sonderMargins != null) {
                    if (sonderMargins.containsKey(view)) {
                        ItemMargin = sonderMargins.get(view);
                    }
                }

                lastYPos += view.getHeight() + ItemMargin;
            }
        } while (iterator.hasNext());

        this.setHeight(lastYPos);

        this.setZeroPos();

        if (mLayoutChangedListener != null)
            mLayoutChangedListener.LayoutIsChanged(this, lastYPos);

    }

    /**
     * @param view
     * @param margin der Abstand zu den Oberen Item
     * @return
     */
    public GL_View_Base addChild(final GL_View_Base view, float margin) {
        if (sonderMargins == null)
            sonderMargins = new Hashtable<GL_View_Base, Float>();
        sonderMargins.put(view, margin);
        return addChildDirekt(view);
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view) {
        return addChildDirekt(view);
    }

    @Override
    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        GL_View_Base v;

        if (last) {
            v = addChildDirektLast(view);
        } else {
            v = addChildDirekt(view);
        }
        layout();

        return v;
    }

    @Override
    public void removeChild(final GL_View_Base view) {
        removeChildsDirekt(view);
        layout();
    }

    @Override
    public void removeChilds() {
        removeChildsDirekt();
        layout();

    }

    @Override
    public void removeChilds(final MoveableList<GL_View_Base> Childs) {
        removeChildsDirekt(Childs);
        layout();
    }

    @Override
    public GL_View_Base addChildDirekt(final GL_View_Base view) {
        synchronized (childs) {
            childs.add(view);
            layout();
        }

        return view;
    }

    @Override
    public GL_View_Base addChildDirektLast(final GL_View_Base view) {
        synchronized (childs) {
            childs.add(0, view);
            layout();
        }

        return view;
    }

    @Override
    public void removeChildsDirekt() {
        synchronized (childs) {
            childs.clear();
            layout();
        }
    }

    public interface LayoutChanged {
        public void LayoutIsChanged(Linearlayout linearLayout, float newHeight);
    }

}
