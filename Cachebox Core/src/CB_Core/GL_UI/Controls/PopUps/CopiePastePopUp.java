package CB_Core.GL_UI.Controls.PopUps;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.libGdx_Controls.CB_TextField;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class CopiePastePopUp extends PopUp_Base
{

	private Button pasteButton;

	public CopiePastePopUp(CB_RectF rec, String Name, final CB_TextField textField)
	{
		super(rec, Name);
		setBackground(new NinePatch(SpriteCache.Bubble.get(3), 16, 16, 16, 23));

		this.setClickable(true);

		pasteButton = new Button(rec.ScaleCenter(0.6f), "PasteButton");
		pasteButton.setFont(Fonts.getBubbleNormal());
		pasteButton.setText("paste");
		pasteButton.setBackground(new NinePatch(SpriteCache.Icons.get(50), 1, 1, 1, 1));
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
		pasteButton.setninePatch((new NinePatch(SpriteCache.Icons.get(50), 1, 1, 1, 1)));
		pasteButton.setninePatchPressed((new NinePatch(SpriteCache.Icons.get(50), 1, 1, 1, 1)));
	}

	public void flipX()
	{
		NinePatch patch = new NinePatch(SpriteCache.Bubble.get(5), 16, 16, 23, 16);
		setBackground(patch);
		pasteButton.setY(this.height * 0.07f);
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}
}
