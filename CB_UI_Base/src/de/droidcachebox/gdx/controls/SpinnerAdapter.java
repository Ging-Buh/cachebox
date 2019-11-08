package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public interface SpinnerAdapter {

    int getCount();

    String getText(int position);

    Drawable getIcon(int Position);

}
