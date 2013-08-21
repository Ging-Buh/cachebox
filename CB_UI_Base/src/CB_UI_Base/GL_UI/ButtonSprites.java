package CB_UI_Base.GL_UI;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Strucutr für die Aufnahme von drei Sprites für die drei Zustände eines Buttons </BR> Normal, Pressed, Disabled
 * 
 * @author Longri
 */
public class ButtonSprites
{
	Drawable mPressed;
	Drawable mNormal;
	Drawable mDisabled;
	Drawable mFocus;

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

	public Drawable getFocus()
	{
		return mFocus;
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
		mPressed = new SpriteDrawable(atlas.createSprite(Pressed));
		mNormal = new SpriteDrawable(atlas.createSprite(Normal));
		mDisabled = new SpriteDrawable(atlas.createSprite(Disabled));
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
		mPressed = new NinePatchDrawable(new NinePatch(atlas.findRegion(Pressed), left, right, top, bottom));
		mNormal = new NinePatchDrawable(new NinePatch(atlas.findRegion(Normal), left, right, top, bottom));
		mDisabled = new NinePatchDrawable(new NinePatch(atlas.findRegion(Disabled), left, right, top, bottom));
	}

	public ButtonSprites(TextureAtlas atlas, String Normal, String Pressed, String Disabled, String Focus, int left, int right, int top,
			int bottom)
	{
		mPressed = new NinePatchDrawable(new NinePatch(atlas.findRegion(Pressed), left, right, top, bottom));
		mNormal = new NinePatchDrawable(new NinePatch(atlas.findRegion(Normal), left, right, top, bottom));
		mDisabled = new NinePatchDrawable(new NinePatch(atlas.findRegion(Disabled), left, right, top, bottom));
		mFocus = new NinePatchDrawable(new NinePatch(atlas.findRegion(Focus), left, right, top, bottom));
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
		mPressed = new SpriteDrawable(Pressed);
		mNormal = new SpriteDrawable(Normal);
	}

	public ButtonSprites(Sprite Normal, Sprite Pressed, Sprite Disabled, Sprite Focus)
	{
		if (Pressed != null) mPressed = new SpriteDrawable(Pressed);
		if (Normal != null) mNormal = new SpriteDrawable(Normal);
		if (Disabled != null) mDisabled = new SpriteDrawable(Disabled);
		if (Focus != null) mFocus = new SpriteDrawable(Focus);
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
		mPressed = new NinePatchDrawable(new NinePatch(Pressed, left, right, top, bottom));
		mNormal = new NinePatchDrawable(new NinePatch(Normal, left, right, top, bottom));
	}

	public void dispose()
	{
		mPressed = null;
		mNormal = null;
		mDisabled = null;
		mFocus = null;
	}
}
