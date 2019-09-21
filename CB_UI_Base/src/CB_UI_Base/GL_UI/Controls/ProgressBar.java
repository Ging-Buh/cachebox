package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.CB_Label.HAlignment;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ProgressBar extends CB_View_Base {
    private final CB_Label label;
    protected float progressDrawWidth = 0;
    private int progress;
    private Drawable progressFill, progressFillDisabled;
    private String msg = "";
    private boolean isDisabled = false;

    public ProgressBar(CB_RectF rec, String Name) {
        super(rec, Name);
        label = new CB_Label(this);
        label.setHAlignment(HAlignment.CENTER);
        this.addChild(label);
        progress = 0;
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);
        label.setWidth(width);
    }

    @Override
    protected void initialize() {
        if (drawableBackground == null) {
            setBackground(Sprites.ProgressBack);
        }

        if (progressFill == null) {
            progressFill = Sprites.ProgressFill;
        }

        if (progressFillDisabled == null) {
            progressFillDisabled = Sprites.ProgressDisabled;
        }

        GL.that.renderOnce();
    }

    /**
     * @param value
     * @return the pos of Progress end
     */
    public void setProgress(int value) {
        if (!isDisposed()) {
            if (value > progress) {
                progress = value;
                if (progress > 100)
                    progress = 100;
                progressDrawWidth = (getWidth() / 100) * progress;
                GL.that.renderOnce();
            }
        }
    }

    /**
     * @param value
     * @param Msg
     * @return the pos of Progress end
     */
    public void setProgress(int value, final String Msg) {
        if (!isDisposed()) {
            if (value > progress) {
                setProgress(value);
                if (!msg.equals(Msg)) {
                    msg = Msg;
                    GL.that.RunOnGL(() -> label.setText(msg));
                }
            }
        }
    }

    public void resetProgress(final String Msg) {
        progress = 0;
        GL.that.RunOnGL(() -> label.setText(Msg));
    }

    public void setProgressFill(Drawable drawable) {
        progressFill = drawable;
    }

    public void setProgressFillDisabled(Drawable drawable) {
        progressFillDisabled = drawable;
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

    public int getProgress() {
        return progress;
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

}
