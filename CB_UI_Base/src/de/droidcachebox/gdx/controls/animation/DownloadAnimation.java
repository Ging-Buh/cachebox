package de.droidcachebox.gdx.controls.animation;

import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public class DownloadAnimation extends FrameAnimation {
    private static final String sClass = "DownloadAnimation";

    public DownloadAnimation() {
        this(new CB_RectF(0, 0, 50, 50));
    }

    public DownloadAnimation(CB_RectF cb_rectF) {
        super(cb_rectF, sClass);
        addFrame(Sprites.getSprite("download-1"));
        addFrame(Sprites.getSprite("download-2"));
        addFrame(Sprites.getSprite("download-3"));
        addFrame(Sprites.getSprite("download-4"));
        addFrame(Sprites.getSprite("download-5"));
        mDuration = 1000;
        mPlaying=true;
    }

    @Override
    public void play() {

    }
}
