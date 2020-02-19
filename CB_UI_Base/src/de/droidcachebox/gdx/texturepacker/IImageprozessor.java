package de.droidcachebox.gdx.texturepacker;

import com.badlogic.gdx.utils.Array;
import de.droidcachebox.utils.AbstractFile;

public interface IImageprozessor {

    public abstract void addImage(AbstractFile abstractFile);

    public abstract Array<Rect_Base> getImages();

}