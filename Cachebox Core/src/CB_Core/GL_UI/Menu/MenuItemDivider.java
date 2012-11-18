package CB_Core.GL_UI.Menu;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class MenuItemDivider extends MenuItemBase
{

	Drawable Image;
	float spriteHeight = 0;
	float spriteWidth = 0;

	public MenuItemDivider(CB_RectF rec, int Index, String Name)
	{
		super(rec, Index, Name);

	}

	public MenuItemDivider(SizeF sizeF, int Index, String Name)
	{
		super(sizeF.getBounds(), Index, Name);
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (Image == null)
		{
			Sprite s = SpriteCache.Dialog.get(4);
			spriteHeight = s.getHeight();
			spriteWidth = s.getWidth();
			Image = new NinePatchDrawable(new NinePatch(s, 1, 1, 1, 1));
		}

		Image.draw(batch, 0, this.halfHeight - (spriteHeight / 2), width, spriteHeight);
	}

}
