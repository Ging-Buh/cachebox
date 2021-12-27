package de.droidcachebox.gdx.controls.animation;

import com.badlogic.gdx.graphics.g2d.Batch;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.math.CB_RectF;

public abstract class AnimationBase extends CB_View_Base {

    boolean mPlaying = false;
    int mDuration = 1;
    float mSpriteWidth;
    float mSpriteHeight;
    boolean mPause = false;

    public AnimationBase(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    protected abstract void render(Batch batch);

    public abstract void play();

    public abstract void stop();

    public abstract void pause();

}