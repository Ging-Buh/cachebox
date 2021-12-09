package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public class ProgressBar extends CB_Label {
    private float progressDrawWidth = 0;
    private int currentPercent;
    private Drawable progressFill, progressFillDisabled;
    private boolean isDisabled = false;
    private String message;

    public ProgressBar(CB_RectF rec) {
        super(rec);
        init();
    }

    public ProgressBar() {
        super();
        init();
    }

    private void init() {
        mHAlignment = HAlignment.CENTER;
        mVAlignment = VAlignment.TOP; // looks better
        currentPercent = 0;
        message = "";
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
    }

    /**
     * @param newPercent ?
     *                   set progressDrawWidth, renders the pos of Progress
     */
    public void fillBarAt(int newPercent) {
        if (newPercent != currentPercent) {
            currentPercent = newPercent;
            if (currentPercent > 100)
                currentPercent = 100;
            progressDrawWidth = (getWidth() / 100) * currentPercent;
        }
    }

    /**
     * @param newPercent ?
     * @param newMessage ?
     */
    public void setValues(int newPercent, final String newMessage) {
        if (!isDisposed()) {
            fillBarAt(newPercent);
            if (!newMessage.equals(message)) {
                setText(newMessage);
            }
        }
    }

    public void setProgressFill(Drawable drawable) {
        progressFill = drawable;
    }

    public int getCurrentPercent() {
        return currentPercent;
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

    @Override
    protected void render(Batch batch) {
        if (this.isDisposed())
            return;
        if (progressFill == null || progressFillDisabled == null)
            initialize();

        if (isDisabled) {
            if (progressFillDisabled != null) {
                float patch = progressFillDisabled.getLeftWidth() + progressFillDisabled.getRightWidth();
                if (progressDrawWidth >= patch) {
                    progressFillDisabled.draw(batch, 0, 0, progressDrawWidth, getHeight());
                    super.render(batch);
                }
            }
        } else {
            if (progressFill != null) {
                float patch = progressFill.getLeftWidth() + progressFill.getRightWidth();
                if (progressDrawWidth >= patch) {
                    progressFill.draw(batch, 0, 0, progressDrawWidth, getHeight());
                    super.render(batch);
                }
            }
        }

        if (!message.equals(getText())) {
            super.render(batch);
            message = getText();
        }
    }

}
