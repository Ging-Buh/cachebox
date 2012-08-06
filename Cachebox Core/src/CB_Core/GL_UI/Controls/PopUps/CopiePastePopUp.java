package CB_Core.GL_UI.Controls.PopUps;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.EditTextField;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CopiePastePopUp extends PopUp_Base
{

	private Button pasteButton;

	public CopiePastePopUp(CB_RectF rec, String Name, final EditTextField textField)
	{
		super(rec, Name);

		int p = SpriteCache.patch;

		setBackground(new NinePatchDrawable(new NinePatch(SpriteCache.Bubble.get(3), p, p, p, (int) (p * 1.432))));

		this.setClickable(true);

		pasteButton = new Button(rec.ScaleCenter(0.6f), "PasteButton");
		pasteButton.setFont(Fonts.getBubbleNormal());
		pasteButton.setText("paste");
		pasteButton.setBackground(new NinePatchDrawable(new NinePatch(SpriteCache.Icons.get(50), 1, 1, 1, 1)));
		pasteButton.setY(rec.getHeight() * 0.4f);
		pasteButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Logger.LogCat("Paste Button Clicked");
				close();
				textField.paste();
				return false;
			}
		});

		this.addChild(pasteButton);
	}

	@Override
	public void Initial()
	{
		pasteButton.setninePatch((new SpriteDrawable(SpriteCache.Icons.get(50))));
		pasteButton.setninePatchPressed((new SpriteDrawable(SpriteCache.Icons.get(50))));
	}

	public void flipX()
	{
		int p = SpriteCache.patch;

		Drawable drawable = new NinePatchDrawable(new NinePatch(SpriteCache.Bubble.get(5), p, p, (int) (p * 1.432), p));
		setBackground(drawable);
		pasteButton.setY(this.height * 0.07f);
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}
}
