package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GestureHelp extends CB_View_Base
{

	public GestureHelp(SizeF Size, String Name)
	{
		super(Size, Name);

	}

	Pixmap pixmap = null;
	Texture tex = null;

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
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
	}

	private void requestLayout()
	{
		SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));

	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

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
		if (x + width > UiSizes.getWindowWidth())
		{
			UseLastBtnBackground = true;
			x = UiSizes.getWindowWidth() - width;
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
		if (icon != null) Button.setNinePatch(icon);
		this.addChild(Button);
	}

	public void addUp(Sprite icon)
	{
		Up = new Image(cX, cY + h + d, h, ArrowH, "Up");
		if (icon != null) Up.setSprite(SpriteCache.Arrows.get(7));
		this.addChild(Up);

		UpIcon = new Image(cX, cY + h + d + ArrowH, h, h, "UpIcon");
		if (icon != null) UpIcon.setSprite(icon);
		this.addChild(UpIcon);
	}

	public void addDown(Sprite icon)
	{
		Down = new Image(cX, cY - d - ArrowH, h, ArrowH, "Down");
		if (icon != null) Down.setSprite(SpriteCache.Arrows.get(6));
		this.addChild(Down);

		DownIcon = new Image(cX, cY - d - ArrowH - h, h, h, "DownIcon");
		if (icon != null) DownIcon.setSprite(icon);
		this.addChild(DownIcon);
	}

	public void addLeft(Sprite icon)
	{
		Left = new Image(cX - d - ArrowW, cY, ArrowW, h, "Left");
		if (icon != null) Left.setSprite(SpriteCache.Arrows.get(8));
		this.addChild(Left);

		LeftIcon = new Image(cX - d - ArrowH - h, cY, h, h, "LeftIcon");
		if (icon != null) LeftIcon.setSprite(icon);
		this.addChild(LeftIcon);
	}

	public void addRight(Sprite icon)
	{
		Right = new Image(cX + h + d, cY, ArrowW, h, "Up");
		if (icon != null) Right.setSprite(SpriteCache.Arrows.get(9));
		this.addChild(Right);

		RightIcon = new Image(cX + h + d + ArrowW, cY, h, h, "UpIcon");
		if (icon != null) RightIcon.setSprite(icon);
		this.addChild(RightIcon);
	}

}
