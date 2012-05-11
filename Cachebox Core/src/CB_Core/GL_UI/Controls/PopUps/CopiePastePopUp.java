package CB_Core.GL_UI.Controls.PopUps;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class CopiePastePopUp extends PopUp_Base
{

	private Button pasteButton;

	public CopiePastePopUp(CB_RectF rec, String Name, final TextField textField)
	{
		super(rec, Name);
		setBackground(new NinePatch(SpriteCache.Bubble.get(3), 16, 16, 16, 16));

		this.setClickable(true);

		pasteButton = new Button(rec.ScaleCenter(0.5f), "PasteButton");
		pasteButton.setText("Paste");

		pasteButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				Logger.LogCat("Paste Button Clicked");
				close();
				textField.paste();
				return false;
			}
		});

		this.addChild(pasteButton);
	}

}
