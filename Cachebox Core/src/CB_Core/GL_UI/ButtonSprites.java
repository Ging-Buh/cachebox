package CB_Core.GL_UI;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Strucutr f�r die Aufnahme von drei Sprites f�r die drei Zust�nde eines Buttons </BR> Normal, Pressed, Disabled
 * 
 * @author Longri
 */
public class ButtonSprites
{
	Drawable mPressed;
	Drawable mNormal;
	Drawable mDisabled;

	public Drawable getPressed()
	{
		return mPressed;
	}

	public Drawable getNormal()
	{
		return mNormal;
	}

	public Drawable getDisabled()
	{
		return mDisabled;
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den Werten 0,0,0,0
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name f�r das Normal Sprite
	 * @param Pressed
	 *            Name f�r das Pressed Sprite
	 * @param Disabled
	 *            Name f�r das Disabled Sprite
	 */
	public ButtonSprites(TextureAtlas atlas, String Normal, String Pressed, String Disabled)
	{
		mPressed = new SpriteDrawable(atlas.createSprite(Pressed));
		mNormal = new SpriteDrawable(atlas.createSprite(Normal));
		mDisabled = new SpriteDrawable(atlas.createSprite(Disabled));
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den �bergebenen Werten
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name f�r das Normal Sprite
	 * @param Pressed
	 *            Name f�r das Pressed Sprite
	 * @param Disabled
	 *            Name f�r das Disabled Sprite
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public ButtonSprites(TextureAtlas atlas, String Normal, String Pressed, String Disabled, int left, int right, int top, int bottom)
	{
		mPressed = new NinePatchDrawable(new NinePatch(atlas.findRegion(Pressed), left, right, top, bottom));
		mNormal = new NinePatchDrawable(new NinePatch(atlas.findRegion(Normal), left, right, top, bottom));
		mDisabled = new NinePatchDrawable(new NinePatch(atlas.findRegion(Disabled), left, right, top, bottom));
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den Werten 0,0,0,0
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name f�r das Normal Sprite
	 * @param Pressed
	 *            Name f�r das Pressed Sprite
	 */
	public ButtonSprites(Sprite Normal, Sprite Pressed)
	{
		mPressed = new SpriteDrawable(Pressed);
		mNormal = new SpriteDrawable(Normal);
	}

	/**
	 * Inintialisiert die ninePatchSprites mit den �bergebenen Werten
	 * 
	 * @param atlas
	 *            aus dem die Sprites gelesen werden
	 * @param Normal
	 *            Name f�r das Normal Sprite
	 * @param Pressed
	 *            Name f�r das Pressed Sprite
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public ButtonSprites(Sprite Normal, Sprite Pressed, int left, int right, int top, int bottom)
	{
		mPressed = new NinePatchDrawable(new NinePatch(Pressed, left, right, top, bottom));
		mNormal = new NinePatchDrawable(new NinePatch(Normal, left, right, top, bottom));
	}

	public void dispose()
	{
		mPressed = null;
		mNormal = null;
		mDisabled = null;
	}
}
