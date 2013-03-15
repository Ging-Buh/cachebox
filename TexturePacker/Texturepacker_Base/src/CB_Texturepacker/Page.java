package CB_Texturepacker;

import com.badlogic.gdx.utils.Array;

/** @author Nathan Sweet */
class Page
{
	public String imageName;
	public Array<Rect_Base> outputRects, remainingRects;
	public float occupancy;
	public int x, y, width, height;
}
