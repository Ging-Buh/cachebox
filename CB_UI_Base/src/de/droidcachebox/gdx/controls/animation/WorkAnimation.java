package de.droidcachebox.gdx.controls.animation;

import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.math.CB_RectF;

public class WorkAnimation extends RotateAnimation {
    private static final String sClass = "WorkAnimation";

    public WorkAnimation() {
        this(new CB_RectF(0, 0, 50, 50));
    }

    public WorkAnimation(CB_RectF cb_rectF) {
        super(cb_rectF, sClass);
        setSprite(Sprites.getSprite(IconName.settings.name()));
        setOrigin(this.getHalfWidth(), this.getHalfHeight());
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        setOrigin(this.getHalfWidth(), this.getHalfHeight());
    }

    @Override
    public void play() {

    }
}
