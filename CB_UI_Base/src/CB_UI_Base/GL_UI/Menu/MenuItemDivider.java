package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.DialogElement;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;

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
			Sprite s = SpriteCacheBase.Dialog.get(DialogElement.divider.ordinal());
			spriteHeight = s.getHeight();
			spriteWidth = s.getWidth();
			Image = new NinePatchDrawable(new NinePatch(s, 1, 1, 1, 1));
		}

		Image.draw(batch, 0, this.getHalfHeight() - (spriteHeight / 2), getWidth(), spriteHeight);
	}

}
