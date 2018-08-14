package CB_UI_Base.GL_UI.Controls.Animation;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

public abstract class FrameAnimation extends AnimationBase {
    ArrayList<Drawable> frames;
    int count = 0;

    public FrameAnimation(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
    }

    public FrameAnimation(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    int getFrameIndex(int Duration, int Frames) {
        // Duration != 0
        // Frames != 0
        return (1 + ((int) (GL.that.getStateTime() * 1000) % Duration) / (Duration / Frames));
    }

    public void addFrame(Sprite frame) {
        if (frames == null)
            frames = new ArrayList<Drawable>();

        frames.add(new SpriteDrawable(frame));
    }

    public void addLastFrame(Sprite frame) {
        mSpriteWidth = frame.getWidth();
        mSpriteHeight = frame.getHeight();
        frames.add(new SpriteDrawable(frame));
    }

    @Override
    protected void render(Batch batch) {

        if (frames == null || frames.size() == 0)
            return;

        int Frameindex = getFrameIndex(mDuration, frames.size());

        count++;
        if (count > frames.size() - 2)
            count = 0;

        Drawable mDrawable = mPlaying ? frames.get(Frameindex - 1) : frames.get(0);

        if (mDrawable != null) {
            float drawwidth = getWidth();
            float drawHeight = getHeight();
            float drawX = 0;
            float drawY = 0;

            if (mSpriteWidth > 0 && mSpriteHeight > 0) {
                float proportionWidth = getWidth() / mSpriteWidth;
                float proportionHeight = getHeight() / mSpriteHeight;

                float proportion = Math.min(proportionWidth, proportionHeight);

                drawwidth = mSpriteWidth * proportion;
                drawHeight = mSpriteHeight * proportion;
                drawX = (getWidth() - drawwidth) / 2;
                drawY = (getHeight() - drawHeight) / 2;
            }

            mDrawable.draw(batch, drawX, drawY, drawwidth, drawHeight);

        }
        GL.that.renderOnce();
    }

    protected void play(int duration) {
        this.mDuration = duration;
        mPlaying = true;
    }

    @Override
    public void stop() {
        mPlaying = false;
    }

    @Override
    public void pause() {

    }

}
