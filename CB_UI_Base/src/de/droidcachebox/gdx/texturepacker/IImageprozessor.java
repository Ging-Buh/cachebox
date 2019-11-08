package de.droidcachebox.gdx.texturepacker;

import com.badlogic.gdx.utils.Array;
import de.droidcachebox.utils.File;

public interface IImageprozessor {

    public abstract void addImage(File file);

    public abstract Array<Rect_Base> getImages();

}