package de.droidcachebox.gdx.controls.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;

public abstract class AnimationBase extends CB_View_Base {

    protected boolean mPlaying = false;
    protected int mDuration = 1;
    protected float mSpriteWidth;
    protected float mSpriteHeight;
    protected boolean mPause = false;

    public AnimationBase(String Name) {
        super(Name);
    }

    public AnimationBase(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
    }

    public AnimationBase(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name) {
        super(X, Y, Width, Height, Parent, Name);
    }

    public AnimationBase(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public AnimationBase(CB_RectF rec, GL_View_Base Parent, String Name) {
        super(rec, Parent, Name);
    }

    public AnimationBase(SizeF size, String Name) {
        super(size, Name);
    }

    protected abstract void render(Batch batch);

    public abstract void play();

    public abstract void stop();

    public abstract void pause();

    public abstract AnimationBase INSTANCE();

    public abstract AnimationBase INSTANCE(CB_RectF rec);

}