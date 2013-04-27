package CB_Core.GL_UI.Controls.PopUps;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.ImageButton;
import CB_Core.GL_UI.interfaces.ICopyPaste;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UI_Size_Base;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class CopiePastePopUp extends PopUp_Base
{

	private ImageButton btnCopy;
	private ImageButton btnCut;
	private ImageButton btnPaste;

	public CopiePastePopUp(String Name, final ICopyPaste copyPasteControl)
	{
		super(new CB_RectF(0, 0, UI_Size_Base.that.getButtonWidth() * 3.2f, UI_Size_Base.that.getButtonHeight() * 1.5f), Name);

		int p = SpriteCache.patch;

		setBackground(new NinePatchDrawable(new NinePatch(SpriteCache.Bubble.get(3), p, p, p, (int) (p * 1.432))));

		this.setClickable(true);

		CB_RectF rec = new CB_RectF(0, 0, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());

		btnPaste = new ImageButton(rec, "PasteButton");
		btnPaste.setFont(Fonts.getBubbleNormal());
		btnPaste.setImage(SpriteCache.paste);
		btnPaste.setY(rec.getHeight() * 0.4f);
		btnPaste.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Logger.LogCat("Paste Button Clicked");
				close();
				copyPasteControl.pasteFromClipboard();
				return false;
			}
		});

		this.addChild(btnPaste);

		btnCopy = new ImageButton(rec, "CopyButton");
		btnCopy.setFont(Fonts.getBubbleNormal());
		btnCopy.setImage(SpriteCache.copy);
		btnCopy.setY(rec.getHeight() * 0.4f);
		btnCopy.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Logger.LogCat("Copy Button Clicked");
				close();
				copyPasteControl.copyToClipboard();
				return false;
			}
		});

		this.addChild(btnCopy);

		btnCut = new ImageButton(rec, "CutButton");
		btnCut.setFont(Fonts.getBubbleNormal());
		btnCut.setImage(SpriteCache.cut);
		btnCut.setY(rec.getHeight() * 0.4f);
		btnCut.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				// Logger.LogCat("Cut Button Clicked");
				close();
				copyPasteControl.cutToClipboard();
				return false;
			}
		});

		this.addChild(btnCut);

		float sollDivider = (this.width - p - (rec.getWidth() * 3)) / 4;

		btnCut.setX(sollDivider + (p / 2));
		btnCopy.setX(btnCut.getMaxX() + sollDivider);
		btnPaste.setX(btnCopy.getMaxX() + sollDivider);
	}

	@Override
	public void Initial()
	{
	}

	public void flipX()
	{
		int p = SpriteCache.patch;

		Drawable drawable = new NinePatchDrawable(new NinePatch(SpriteCache.Bubble.get(5), p, p, (int) (p * 1.432), p));
		setBackground(drawable);

		float yValue = this.height * 0.07f;

		btnPaste.setY(yValue);
		btnCopy.setY(yValue);
		btnCut.setY(yValue);
	}

	@Override
	protected void SkinIsChanged()
	{

	}
}
