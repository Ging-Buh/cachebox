package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class CB_CheckBox extends CB_Button {

    protected Drawable drawableDisabledChk;
    protected boolean isChk = false;
    protected OnCheckChangedListener changeListener;

    public CB_CheckBox(String name) {
        super(new CB_RectF(UI_Size_Base.that.getChkBoxSize()), name);
        this.setClickable(true);
    }

    public CB_CheckBox(CB_RectF rec, String name) {
        super(rec, name);
        this.setClickable(true);
    }

    @Override
    protected void render(Batch batch) {
        if (drawableNormal == null || drawablePressed == null || drawableDisabledChk == null || drawableDisabled == null) {
            Initial();
            GL.that.renderOnce();
        }

        if (!isChk && !isDisabled) {
            if (drawableNormal != null) {
                drawableNormal.draw(batch, 0, 0, getWidth(), getHeight());
            }
        } else if (isChk && isDisabled) {
            if (drawableDisabledChk != null) {
                drawableDisabledChk.draw(batch, 0, 0, getWidth(), getHeight());
            }
        } else if (isChk) {
            if (drawablePressed != null) {
                drawablePressed.draw(batch, 0, 0, getWidth(), getHeight());
            }
        } else {
            if (drawableDisabled != null) {
                drawableDisabled.draw(batch, 0, 0, getWidth(), getHeight());
            }
        }

    }

    @Override
    protected void Initial() {

        // die einzelnen Hintergründe werden hier anders benutzt
        // drawableNormal= unchecked
        // drawablePressed= checked
        // drawableDisabled= unchecked Disabled
        // drawableDisabledChk = checked Disabled

        if (drawableNormal == null) {
            drawableNormal = Sprites.chkOff;
        }
        if (drawablePressed == null) {
            drawablePressed = Sprites.chkOn;
        }
        if (drawableDisabled == null) {
            drawableDisabled = Sprites.chkOffDisabled;
        }

        if (drawableDisabledChk == null) {
            drawableDisabledChk = Sprites.chkOnDisabled;
        }
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        if (!isDisabled) {

            GL.that.renderOnce();
        }
        return dragableButton ? false : true;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        GL.that.renderOnce();
        return false;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {

        GL.that.renderOnce();
        return dragableButton ? false : true;
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        if (!isDisabled) {
            isChk = !isChk;
            if (changeListener != null)
                changeListener.onCheckedChanged(this, isChk);
        }
        if (mOnClickListener != null)
            mOnClickListener.onClick(this, x, y, pointer, button);
        return true;
    }

    @Override
    public void setEnabled(boolean b) {
        isDisabled = !b;
    }

    public boolean isChecked() {
        return isChk;
    }

    public void setChecked(boolean b) {
        isChk = b;
        if (changeListener != null)
            changeListener.onCheckedChanged(this, isChk);
    }

    public void setOnCheckChangedListener(OnCheckChangedListener listener) {
        changeListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the checked state of a compound button changed.
     */
    public static interface OnCheckChangedListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        void onCheckedChanged(CB_CheckBox view, boolean isChecked);
    }

}
