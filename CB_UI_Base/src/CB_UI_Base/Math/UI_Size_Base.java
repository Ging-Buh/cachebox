package CB_UI_Base.Math;

import CB_UI_Base.Global;
import CB_Utils.Log.Logger;

public abstract class UI_Size_Base
{
	public static UI_Size_Base that;

	public UI_Size_Base()
	{
		that = this;
	}

	protected Size Button;
	protected int scaledFontSize_normal;
	protected int iconSize;
	protected int windowWidth;
	protected int windowHeight;
	protected int scaledFontSize_big;
	protected int scaledFontSize_btn;
	protected int ScaledFontSize_small;
	protected int ScaledFontSize_supersmall;
	protected int scaledRefSize_normal;
	protected int IconContextMenuHeight;
	protected float scale;
	protected int margin;
	protected double calcBase;
	protected int RefWidth;
	protected int mClickToleranz;
	public devicesSizes ui;

	public abstract void instanzeInitial();

	public Size initial(devicesSizes ini)
	{
		Logger.DEBUG("UISizes.initial()");
		ui = ini;
		windowWidth = ini.Window.width;// d.getWidth();
		windowHeight = ini.Window.height;// d.getHeight();

		scale = ini.Density;// res.getDisplayMetrics().density;

		mClickToleranz = (int) (17 * scale);

		calcBase = 533.333 * scale;

		margin = ini.Margin;

		int b = (int) (40 * scale);
		Button = new Size(b, b);

		if (Global.isTab)
		{
			// RefWidth LeftWidth on Tab
			// must have place for 5 Buttons

			b *= 1.5f; // Bud the bottom button are a little bit higher!

			RefWidth = ((b + margin) * 5) + margin;
		}
		else
		{
			RefWidth = windowWidth;
		}

		GL_UISizes.writeDebug("Button", Button.asFloat());

		scaledRefSize_normal = (int) ((calcBase / (ini.RefSize)) * scale);
		scaledFontSize_normal = (int) ((calcBase / (ini.TextSize_Normal)) * scale);
		scaledFontSize_big = (int) (scaledFontSize_normal * 1.1);
		ScaledFontSize_small = (int) (scaledFontSize_normal * 0.9);
		ScaledFontSize_supersmall = (int) (ScaledFontSize_small * 0.8);
		scaledFontSize_btn = (int) ((calcBase / ini.ButtonTextSize) * scale);

		iconSize = (int) ((calcBase / ini.IconSize) * scale);

		IconContextMenuHeight = (int) (calcBase / 11.1);

		instanzeInitial();

		return new Size(windowWidth, windowHeight);

	}

	public int getMargin()
	{
		return margin;
	}

	public int getWindowHeight()
	{
		return windowHeight;
	}

	public int getWindowWidth()
	{
		return windowWidth;
	}

	public int getButtonHeight()
	{
		return Button.height;
	}

	public int getButtonWidth()
	{
		return Button.width;
	}

	public int getButtonWidthWide()
	{
		return (int) (Button.width * 1.6);
	}

	public SizeF getChkBoxSize()
	{
		float h = Button.height * 0.88f;
		return new SizeF(h, h);
	}

	public int getScaledFontSize()
	{
		return scaledFontSize_normal;
	}

	public int getScaledFontSize_btn()
	{
		return scaledFontSize_btn;
	}

	public int getScaledRefSize_normal()
	{
		return scaledRefSize_normal;
	}

	public int getScaledFontSize_big()
	{
		return scaledFontSize_big;
	}

	public int getScaledFontSize_small()
	{
		return ScaledFontSize_small;
	}

	public int getScaledFontSize_supersmall()
	{
		return ScaledFontSize_supersmall;
	}

	public int getIconSize()
	{
		return iconSize;
	}

	public float getScale()
	{
		return scale;
	}

	public float getSmallestWidth()
	{
		return Math.min(windowHeight, windowWidth);
	}

	public int getClickToleranz()
	{
		return mClickToleranz;
	}

}