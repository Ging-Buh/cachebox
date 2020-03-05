package de.droidcachebox.gdx.controls;

import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.MoveableList;

/**
 * Eine CollabsBox mit LinearLayout
 *
 * @author Longri
 */
public class LinearCollapseBox extends CollapseBox {
    private Linearlayout linearLayout;

    public LinearCollapseBox(CB_RectF rec, String Name) {
        super(rec, Name);
        linearLayout = new Linearlayout(rec.getWidth(), "LinearLayout-" + Name);
        this.childs.add(linearLayout);

        linearLayout.setLayoutChangedListener((linearLayout, newHeight) -> layout());

    }

    public GL_View_Base addChild(final GL_View_Base view) {
        return linearLayout.addChild(view, false);
    }

    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        GL_View_Base v = linearLayout.addChild(view, last);
        layout();

        return v;
    }

    public void removeChild(final GL_View_Base view) {

        linearLayout.removeChild(view);
        layout();
    }

    public void removeChilds() {
        linearLayout.removeChilds();
        layout();

    }

    public void removeChilds(final MoveableList<GL_View_Base> Childs) {
        linearLayout.removeChilds(Childs);
        layout();
    }

    public GL_View_Base addChildDirekt(final GL_View_Base view) {
        linearLayout.addChildDirekt(view);
        layout();
        return view;
    }

    public GL_View_Base addChildDirektLast(final GL_View_Base view) {
        linearLayout.addChildDirektLast(view);

        layout();
        return view;
    }

    public void removeChildsDirekt() {
        linearLayout.removeChildsDirekt();
        layout();

    }

    private void layout() {
        this.setHeight(linearLayout.getHeight());
    }

    /**
     * make this view clickable, if true else not.  </br>
     * if not, no click is transferred to a child
     *
     * @param value ?
     */
    public void setClickable(boolean value) {
        linearLayout.setClickable(value);
        super.setClickable(value);
    }
}
