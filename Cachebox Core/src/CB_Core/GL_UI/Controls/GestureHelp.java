package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class GestureHelp extends CB_View_Base
{

	public GestureHelp(SizeF Size, String Name)
	{
		super(Size, Name);

	}

	/**
	 * CacheID of the Cache showing Bubble
	 */
	private long mCacheId = -1;

	public long getCacheId()
	{
		return mCacheId;
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		Sprite sprite = SpriteCache.Bubble.get(UseLastBtnBackground ? 4 : 3);
		sprite.setPosition(0, 0);
		sprite.setSize(width, height);
		sprite.draw(batch);
		super.render(batch);
	}

	@Override
	protected void Initial()
	{

	}

	Image Button;
	Image UpIcon;
	Image Up;
	Image DownIcon;
	Image Down;
	Image LeftIcon;
	Image Left;
	Image RightIcon;
	Image Right;
	float h;
	float cX;
	float cY;
	float d;
	float ArrowH;
	float ArrowW;
	boolean UseLastBtnBackground = false;

	@Override
	public void setPos(float x, float y)
	{
		if (x + width > UI_Size_Base.that.getWindowWidth())
		{
			UseLastBtnBackground = true;
			x = UI_Size_Base.that.getWindowWidth() - width;
		}

		super.setPos(x, y);
	}

	public void addBtnIcon(NinePatch icon)
	{
		h = GL_UISizes.BottomButtonHeight / 2.4f;
		d = h / 8;
		cX = (this.height / 2) - (h / 2);
		cY = cX + d + d;
		ArrowH = h / 3;
		ArrowW = h / 3;

		Button = new Image(cX, cY, h, h, "UpIcon");
		if (icon != null) Button.setDrawable(new NinePatchDrawable(icon));
		this.addChild(Button);
	}

	public void addUp(Sprite icon)
	{
		Up = new Image(cX, cY + h + d, h, ArrowH, "Up");
		if (icon != null) Up.setDrawable(new SpriteDrawable(SpriteCache.Arrows.get(7)));
		this.addChild(Up);

		UpIcon = new Image(cX, cY + h + d + ArrowH, h, h, "UpIcon");
		if (icon != null) UpIcon.setDrawable(new SpriteDrawable(icon));
		this.addChild(UpIcon);
	}

	public void addDown(Sprite icon)
	{
		Down = new Image(cX, cY - d - ArrowH, h, ArrowH, "Down");
		if (icon != null) Down.setDrawable(new SpriteDrawable(SpriteCache.Arrows.get(6)));
		this.addChild(Down);

		DownIcon = new Image(cX, cY - d - ArrowH - h, h, h, "DownIcon");
		if (icon != null) DownIcon.setDrawable(new SpriteDrawable(icon));
		this.addChild(DownIcon);
	}

	public void addLeft(Sprite icon)
	{
		Left = new Image(cX - d - ArrowW, cY, ArrowW, h, "Left");
		if (icon != null) Left.setDrawable(new SpriteDrawable(SpriteCache.Arrows.get(8)));
		this.addChild(Left);

		LeftIcon = new Image(cX - d - ArrowH - h, cY, h, h, "LeftIcon");
		if (icon != null) LeftIcon.setDrawable(new SpriteDrawable(icon));
		this.addChild(LeftIcon);
	}

	public void addRight(Sprite icon)
	{
		Right = new Image(cX + h + d, cY, ArrowW, h, "Up");
		if (icon != null) Right.setDrawable(new SpriteDrawable(SpriteCache.Arrows.get(9)));
		this.addChild(Right);

		RightIcon = new Image(cX + h + d + ArrowW, cY, h, h, "UpIcon");
		if (icon != null) RightIcon.setDrawable(new SpriteDrawable(icon));
		this.addChild(RightIcon);
	}

	@Override
	protected void SkinIsChanged()
	{

	}

}
