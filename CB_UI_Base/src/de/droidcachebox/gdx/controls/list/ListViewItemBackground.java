package de.droidcachebox.gdx.controls.list;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public abstract class ListViewItemBackground extends ListViewItemBase {

    protected static boolean mBackIsInitial = false;
    private static NinePatch backSelect;
    private static NinePatch back1;
    private static NinePatch back2;
    protected boolean isPressed = false;

    /**
     * Constructor
     *
     * @param rec
     * @param Index Index in der List
     * @param Name
     */
    public ListViewItemBackground(CB_RectF rec, int Index, String Name) {
        super(rec, Index, Name);
    }

    public static void ResetBackground() {
        mBackIsInitial = false;
    }

    public static float getLeftWidthStatic() {
        if (mBackIsInitial) {
            return backSelect.getLeftWidth();
        }
        return 0;
    }

    public static float getRightWidthStatic() {
        if (mBackIsInitial) {
            return backSelect.getRightWidth();
        }
        return 0;
    }

    @Override
    protected void initialize() {
        if (!mBackIsInitial) {
            backSelect = new NinePatch(Sprites.getSprite("listrec-selected"), 13, 13, 13, 13);
            back1 = new NinePatch(Sprites.getSprite("listrec-first"), 13, 13, 13, 13);
            back2 = new NinePatch(Sprites.getSprite("listrec-secend"), 13, 13, 13, 13);

            mBackIsInitial = true;
        }
    }

    @Override
    protected void render(Batch batch) {
        if (isPressed) {
            isPressed = GL_Input.that.getIsTouchDown();
        }

        if (this.isDisposed() || !this.isVisible())
            return;

        super.render(batch);

        // Draw Background
        if (!mBackIsInitial) {
            initialize();
        }
        Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);
        if (isSelected) {
            backSelect.draw(batch, 0, 0, this.getWidth(), this.getHeight());
        } else if (BackGroundChanger) {
            back1.draw(batch, 0, 0, this.getWidth(), this.getHeight());
        } else {
            back2.draw(batch, 0, 0, this.getWidth(), this.getHeight());
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
        if (!mBackIsInitial)
            initialize();
        if (isSelected) {
            return backSelect.getLeftWidth();
        } else if ((this.getIndex() % 2) == 1) {
            return back1.getLeftWidth();
        } else {
            return back2.getLeftWidth();
        }
    }

    @Override
    public float getBottomHeight() {
        if (!mBackIsInitial)
            initialize();
        if (isSelected) {
            return backSelect.getBottomHeight();
        } else if ((this.getIndex() % 2) == 1) {
            return back1.getBottomHeight();
        } else {
            return back2.getBottomHeight();
        }
    }

    @Override
    public float getRightWidth() {
        if (!mBackIsInitial)
            initialize();
        if (isSelected) {
            return backSelect.getRightWidth();
        } else if ((this.getIndex() % 2) == 1) {
            return back1.getRightWidth();
        } else {
            return back2.getRightWidth();
        }
    }

    @Override
    public float getTopHeight() {
        if (!mBackIsInitial)
            initialize();
        if (isSelected) {
            return backSelect.getTopHeight();
        } else if ((this.getIndex() % 2) == 1) {
            return back1.getTopHeight();
        } else {
            return back2.getTopHeight();
        }
    }

}
