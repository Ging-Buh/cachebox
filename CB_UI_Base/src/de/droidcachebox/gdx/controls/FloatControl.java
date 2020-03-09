package de.droidcachebox.gdx.controls;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.math.CB_RectF;

public class FloatControl extends CB_View_Base {
    ProgressBar progressbar;
    CB_Button slideButton;
    iValueChanged changeListener;

    public FloatControl(CB_RectF rec, String Name, iValueChanged listener) {
        super(rec, Name);
        changeListener = listener;
        progressbar = new ProgressBar(rec, "");
        progressbar.setHeight(this.getHeight() * 0.75f);
        progressbar.setText("");
        progressbar.setZeroPos();
        progressbar.setY(getHalfHeight() - progressbar.getHalfHeight());
        this.addChild(progressbar);
        slideButton = new CB_Button(rec, "");
        slideButton.setWidth(this.getHeight());
        slideButton.setZeroPos();
        slideButton.setDraggable();
        this.addChild(slideButton);
    }

    public void setProgress(int value) {
        progressbar.setPogress(value);
        float ButtonPos = progressbar.getProgressDrawWidth() - slideButton.getHalfWidth();
        if (ButtonPos < 0)
            ButtonPos = 0;
        if (ButtonPos > this.getWidth() - slideButton.getWidth())
            ButtonPos = this.getWidth() - slideButton.getWidth();

        slideButton.setX(ButtonPos);
        GL.that.renderOnce();
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return !slideButton.isDisabled();
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        if (slideButton.isDisabled())
            return false;
        if (!KineticPan) {
            int progress = (int) (100 / (getWidth() / x));
            if (progress >= 0 && progress <= 100)
                this.setProgress(progress);
        }

        return true;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        if (slideButton.isDisabled())
            return false;
        if (changeListener != null)
            changeListener.ValueChanged(progressbar.getCurrentPogress());
        return true;
    }

    public void disable(boolean checked) {
        if (checked) {
            slideButton.disable();
            progressbar.disable();
        } else {
            slideButton.enable();
            progressbar.enable();
        }

    }

    public interface iValueChanged {
        void ValueChanged(int value);
    }

}
