package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.Point;

public class SelectionMarker extends CB_View_Base {
    protected Type type;
    protected Drawable marker;
    // X-Position des Einfügepunktes des Markers relativ zur linke Seite
    protected float markerXPos;
    private Point touchDownPos = null;

    public SelectionMarker(Type type) {
        super(0, 0, 10, 10, "");
        this.type = type;
        init();
    }

    @Override
    protected void skinIsChanged() {
        init();
    }

    @Override
    protected void render(Batch batch) {
        marker.draw(batch, 0, 0, getWidth(), getHeight());
    }

    private void init() {
        this.marker = Sprites.selection_set;
        switch (type) {
            case Center:
                break;
            case Left:
                marker = Sprites.selection_left;
                break;
            case Right:
                marker = Sprites.selection_right;
                break;
        }

        // Orginalgröße des Marker-Sprites
        float orgWidth = marker.getMinWidth();
        float orgHeight = marker.getMinHeight();
        float size = UiSizes.getInstance().getButtonHeight();
        float width = size / orgHeight * orgWidth;
        // markerXPos ist der Einfügepunkt rel. der linken Seite
        switch (type) {
            case Center:
                markerXPos = ((orgWidth - 1) / 2) / orgWidth * width;
                break;
            case Right:
                markerXPos = 0;
                break;
            case Left:
                markerXPos = (orgWidth - 1) / orgWidth * width;
                break;
        }
        this.setSize(width, size);
    }

    /**
     * onTouchDown
     */
    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        if (pointer == 0) {
            touchDownPos = new Point(x, y);
            // Log.info(log, "touchdown at " + x + "/" + y);
        }
        return true;
    }

    /**
     * onTouchDragged
     */
    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        if ((pointer == 0) && (touchDownPos != null) && (!KineticPan)) {
            int deltaX = x - touchDownPos.x;
            int deltaY = y - touchDownPos.y;
            if (deltaX == 0 && deltaY == 0)
                return true;
            EditTextField tv = GL.that.getFocusedEditTextField();
            if (tv != null) {
                Point newMarkerPos = tv.aSelectionMarkerIsDragged(deltaX, deltaY, type);
                if (newMarkerPos != null) {
                    moveTo(newMarkerPos.x, newMarkerPos.y);
                }
            }
        }
        return true;
    }

    /**
     * onTouchUp
     */
    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        touchDownPos = null;
        return true;
    }

    /**
     * move absolute
     *
     * @param x
     * @param y
     */
    public void moveTo(float x, float y) {
        final EditTextField tv = GL.that.getFocusedEditTextField();
        if (tv != null) {
            x = x + tv.thisWorldRec.getX();
            y = y + tv.thisWorldRec.getY();
            this.setPos(x - markerXPos, y - getHeight());
        }
    }

    /**
     * move relative
     *
     * @param dx
     * @param dy
     */
    public void moveBy(float dx, float dy) {
        if ((Math.abs(dx) < 0.5) && (Math.abs(dy) < 0.5))
            return;
        this.setPos(this.getX() + dx, this.getY() + dy);
    }

    public enum Type {
        Center, Left, Right
    }
}
