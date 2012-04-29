package CB_Core.GL_UI;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Strucutr für die Aufnahme von drei Sprites für die drei Zustände eines Buttons </BR> Normal, Pressed, Disabled
 * 
 * @author Longri
 */
public class ButtonSprites
{
	NinePatch mPressed;
	NinePatch mNormal;
	NinePatch mDisabled;

	public NinePatch getPressed()
	{
		return mPressed;
	}

	public NinePatch getNormal()
	{
		return mNormal;
	}

	public NinePatch getDisabled()
	{
		return mDisabled;
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den Werten 0,0,0,0
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name für das Normal Sprite
	 * @param Pressed
	 *            Name für das Pressed Sprite
	 * @param Disabled
	 *            Name für das Disabled Sprite
	 */
	public ButtonSprites(TextureAtlas atlas, String Normal, String Pressed, String Disabled)
	{
		mPressed = new NinePatch(atlas.findRegion(Pressed), 0, 0, 0, 0);
		mNormal = new NinePatch(atlas.findRegion(Normal), 0, 0, 0, 0);
		mDisabled = new NinePatch(atlas.findRegion(Disabled), 0, 0, 0, 0);
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den übergebenen Werten
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name für das Normal Sprite
	 * @param Pressed
	 *            Name für das Pressed Sprite
	 * @param Disabled
	 *            Name für das Disabled Sprite
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public ButtonSprites(TextureAtlas atlas, String Normal, String Pressed, String Disabled, int left, int right, int top, int bottom)
	{
		mPressed = new NinePatch(atlas.findRegion(Pressed), left, right, top, bottom);
		mNormal = new NinePatch(atlas.findRegion(Normal), left, right, top, bottom);
		mDisabled = new NinePatch(atlas.findRegion(Disabled), left, right, top, bottom);
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den Werten 0,0,0,0
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name für das Normal Sprite
	 * @param Pressed
	 *            Name für das Pressed Sprite
	 */
	public ButtonSprites(Sprite Normal, Sprite Pressed)
	{
		mPressed = new NinePatch(Pressed, 0, 0, 0, 0);
		mNormal = new NinePatch(Normal, 0, 0, 0, 0);
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den übergebenen Werten
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name für das Normal Sprite
	 * @param Pressed
	 *            Name für das Pressed Sprite
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public ButtonSprites(Sprite Normal, Sprite Pressed, int left, int right, int top, int bottom)
	{
		mPressed = new NinePatch(Pressed, left, right, top, bottom);
		mNormal = new NinePatch(Normal, left, right, top, bottom);
	}

	public void dispose()
	{
		mPressed = null;
		mNormal = null;
		mDisabled = null;
	}
}
