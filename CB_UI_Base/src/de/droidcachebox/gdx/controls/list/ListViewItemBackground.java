package de.droidcachebox.gdx.controls.list;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public abstract class ListViewItemBackground extends ListViewItemBase {

    protected static boolean backGroundIsInitialized = false;
    private static NinePatch backGroundColorForSelectedLine;
    private static NinePatch backGroundColorForEvenLine;
    private static NinePatch backGroundColorForOddLine;
    protected boolean isPressed = false;

    /**
     * Constructor
     *
     * @param rec size
     * @param index Index in der List
     * @param name name
     */
    public ListViewItemBackground(CB_RectF rec, int index, String name) {
        super(rec, index, name);
    }

    public static void ResetBackground() {
        backGroundIsInitialized = false;
    }

    public static float getLeftWidthStatic() {
        if (backGroundIsInitialized) {
            return backGroundColorForSelectedLine.getLeftWidth();
        }
        return 0;
    }

    public static float getRightWidthStatic() {
        if (backGroundIsInitialized) {
            return backGroundColorForSelectedLine.getRightWidth();
        }
        return 0;
    }

    @Override
    protected void renderInit() {
        if (!backGroundIsInitialized) {
            backGroundColorForSelectedLine = new NinePatch(Sprites.getSprite("listrec-selected"), 13, 13, 13, 13);
            backGroundColorForEvenLine = new NinePatch(Sprites.getSprite("listrec-first"), 13, 13, 13, 13);
            backGroundColorForOddLine = new NinePatch(Sprites.getSprite("listrec-secend"), 13, 13, 13, 13);
            backGroundIsInitialized = true;
        }
    }

    @Override
    protected void render(Batch batch) {
        if (isPressed) {
            isPressed = GL_Input.that.getIsTouchDown();
        }

        if (isDisposed || !isVisible())
            return;

        super.render(batch);

        // Draw Background
        if (!backGroundIsInitialized) {
            renderInit();
        }
        if (isSelected) {
            backGroundColorForSelectedLine.draw(batch, 0, 0, getWidth(), getHeight());
        } else if ((mIndex % 2) == 1) {
            backGroundColorForEvenLine.draw(batch, 0, 0, getWidth(), getHeight());
        } else {
            backGroundColorForOddLine.draw(batch, 0, 0, getWidth(), getHeight());
        }

    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        isPressed = true;
        GL.that.renderOnce();
        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        if (isPressed) {
            isPressed = false;
        }
        GL.that.renderOnce();
        return false;
    }

    @Override
    public float getLeftWidth() {
        if (!backGroundIsInitialized)
            renderInit();
        if (isSelected) {
            return backGroundColorForSelectedLine.getLeftWidth();
        } else if ((mIndex % 2) == 1) {
            return backGroundColorForEvenLine.getLeftWidth();
        } else {
            return backGroundColorForOddLine.getLeftWidth();
        }
    }

    @Override
    public float getBottomHeight() {
        if (!backGroundIsInitialized)
            renderInit();
        if (isSelected) {
            return backGroundColorForSelectedLine.getBottomHeight();
        } else if ((mIndex % 2) == 1) {
            return backGroundColorForEvenLine.getBottomHeight();
        } else {
            return backGroundColorForOddLine.getBottomHeight();
        }
    }

    @Override
    public float getRightWidth() {
        if (!backGroundIsInitialized)
            renderInit();
        if (isSelected) {
            return backGroundColorForSelectedLine.getRightWidth();
        } else if ((mIndex % 2) == 1) {
            return backGroundColorForEvenLine.getRightWidth();
        } else {
            return backGroundColorForOddLine.getRightWidth();
        }
    }

    @Override
    public float getTopHeight() {
        if (!backGroundIsInitialized)
            renderInit();
        if (isSelected) {
            return backGroundColorForSelectedLine.getTopHeight();
        } else if ((mIndex % 2) == 1) {
            return backGroundColorForEvenLine.getTopHeight();
        } else {
            return backGroundColorForOddLine.getTopHeight();
        }
    }

}
