package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.math.CB_RectF;

public class ProgressBar extends CB_View_Base {
    private final CB_Label label;
    private float progressDrawWidth = 0;
    private int currentPogress;
    private Drawable progressFill, progressFillDisabled;
    private String msg = "";
    private boolean isDisabled = false;

    public ProgressBar(CB_RectF rec, String Name) {
        super(rec, Name);
        label = new CB_Label(this);
        label.setHAlignment(HAlignment.CENTER);
        this.addChild(label);
        currentPogress = 0;
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        label.setWidth(width);
    }

    @Override
    protected void initialize() {
        if (drawableBackground == null) {
            setBackground(Sprites.progressBack);
        }

        if (progressFill == null) {
            progressFill = Sprites.progressFill;
        }

        if (progressFillDisabled == null) {
            progressFillDisabled = Sprites.progressDisabled;
        }

        GL.that.renderOnce();
    }

    /**
     * @param newProgress ?
     *  set progressDrawWidth, renders the pos of Progress
     */
    public void setPogress(int newProgress) {
        if (!isDisposed()) {
            if (newProgress > currentPogress) {
                currentPogress = newProgress;
                if (currentPogress > 100)
                    currentPogress = 100;
                progressDrawWidth = (getWidth() / 100) * currentPogress;
                GL.that.renderOnce();
            }
        }
    }

    /**
     * @param newProgress ?
     * @param Msg ?
     * renders the pos of Progress
     */
    public void setProgress(int newProgress, final String Msg) {
        if (!isDisposed()) {
            if (newProgress > currentPogress) {
                setPogress(newProgress);
                if (!msg.equals(Msg)) {
                    msg = Msg;
                    GL.that.RunOnGL(() -> label.setText(msg));
                }
            }
        }
    }

    public void resetProgress(final String Msg) {
        currentPogress = 0;
        GL.that.RunOnGL(() -> label.setText(Msg));
    }

    public void setProgressFill(Drawable drawable) {
        progressFill = drawable;
    }

    @Override
    protected void render(Batch batch) {
        if (this.isDisposed())
            return;
        if (progressFill == null || progressFillDisabled == null)
            initialize();

        if (!isDisabled) {
            if (progressFill != null) {
                float patch = progressFill.getLeftWidth() + progressFill.getRightWidth();
                if (progressDrawWidth >= patch) {
                    progressFill.draw(batch, 0, 0, progressDrawWidth, getHeight());
                }
            }
        } else {
            if (progressFillDisabled != null) {
                float patch = progressFillDisabled.getLeftWidth() + progressFillDisabled.getRightWidth();
                if (progressDrawWidth >= patch) {
                    progressFillDisabled.draw(batch, 0, 0, progressDrawWidth, getHeight());
                }
            }
        }
        super.render(batch);
    }

    public void setText(String message) {
        if (isDisposed())
            return;
        msg = message;
        label.setText(msg);
    }

    public int getCurrentPogress() {
        return currentPogress;
    }

    public void enable() {
        isDisabled = false;
    }

    public void disable() {
        isDisabled = true;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public float getProgressDrawWidth() {
        return progressDrawWidth;
    }
}
