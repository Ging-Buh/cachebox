package CB_Core.GL_UI.Activitys;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.utils.ColorDrawable;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Color;

public class ColorPicker extends ActivityBase
{
	private Color InitialColor;
	private Button bOK;
	private Button bCancel;
	private float innerWidth;
	private IReturnListner mReturnListner;

	private Box lastColor;
	private Box aktColor;
	private Image arrow;

	private Image viewHue;

	public interface IReturnListner
	{
		public void returnColor(Color color);
	}

	public ColorPicker(CB_RectF rec, Color color, IReturnListner listner)
	{
		super(rec, "ColorPicker");
		InitialColor = color;
		innerWidth = this.width - Left - Left;

		createOkCancelBtn();
		createColorPreviewLine();
		createViewHue();
	}

	private void createOkCancelBtn()
	{
		bOK = new Button(Left, Left, innerWidth / 2, UiSizes.getButtonHeight(), "OK Button");
		bCancel = new Button(bOK.getMaxX(), Left, innerWidth / 2, UiSizes.getButtonHeight(), "Cancel Button");

		// Translations
		bOK.setText(GlobalCore.Translations.Get("ok"));
		bCancel.setText(GlobalCore.Translations.Get("cancel"));

		this.addChild(bOK);
		bOK.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				finish();
				return true;
			}
		});

		this.addChild(bCancel);
		bCancel.setOnClickListener(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				finish();
				return true;
			}
		});

	}

	private void createColorPreviewLine()
	{
		CB_RectF rec = new CB_RectF(0, bOK.getMaxY() + margin, UiSizes.getButtonWidthWide(), UiSizes.getButtonHeight());
		lastColor = new Box(rec, "LastColor");
		aktColor = new Box(rec, "aktColor");

		rec.setWidth(rec.getHeight());
		arrow = new Image(rec, "arrowImage");
		arrow.setSprite(SpriteCache.Arrows.get(11));

		float lineWidth = lastColor.getWidth() + margin + arrow.getWidth() + margin + aktColor.getWidth();
		float left = this.getHalfWidth() - (lineWidth / 2);
		lastColor.setX(left);
		arrow.setX(lastColor.getMaxX() + margin);
		aktColor.setX(arrow.getMaxX() + margin);

		lastColor.setBackground(new ColorDrawable(InitialColor));
		aktColor.setBackground(new ColorDrawable(InitialColor));

		this.addChild(lastColor);
		this.addChild(arrow);
		this.addChild(aktColor);
	}

	private void createViewHue()
	{
		float vWidth = bOK.getHeight();

		viewHue = new Image(this.width - Right - vWidth, aktColor.getMaxY() + margin, vWidth, this.height - Top - aktColor.getMaxY()
				- margin, "viewHue");
		viewHue.setSprite(SpriteCache.ambilwarna_hue);

		this.addChild(viewHue);
	}

}
