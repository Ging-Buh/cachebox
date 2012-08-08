package CB_Core.GL_UI.Activitys;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.ColorPickerRec;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.utils.ColorDrawable;
import CB_Core.GL_UI.utils.GradiantFill;
import CB_Core.GL_UI.utils.HSV_Color;
import CB_Core.GL_UI.utils.GradiantFilledRectangle;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.Color;

public class ColorPicker extends ActivityBase
{
	private HSV_Color InitialColor;
	private HSV_Color actColor;
	private Button bOK;
	private Button bCancel;
	private float innerWidth;
	private IReturnListner mReturnListner;

	private Box lastColorBox;
	private Box actColorBox;
	private Image arrow;
	private ColorPickerRec viewSatVal;

	private Image viewCursor;
	private Image viewTarget;

	private Image viewHue;

	public interface IReturnListner
	{
		public void returnColor(Color color);
	}

	public ColorPicker(CB_RectF rec, Color color, IReturnListner listner)
	{
		super(rec, "ColorPicker");
		actColor = InitialColor = new HSV_Color(color);
		innerWidth = this.width - Left - Left;
		mReturnListner = listner;
		this.setClickable(true);
		createOkCancelBtn();
		createColorPreviewLine();
		createViewHue();
		createTest();

		hueChanged();

		moveCursor();
		moveTarget();

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
				if (mReturnListner != null) mReturnListner.returnColor(actColor);
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
		lastColorBox = new Box(rec, "LastColor");
		actColorBox = new Box(rec, "aktColor");

		rec.setWidth(rec.getHeight());
		arrow = new Image(rec, "arrowImage");
		arrow.setSprite(SpriteCache.Arrows.get(11));

		float lineWidth = lastColorBox.getWidth() + margin + arrow.getWidth() + margin + actColorBox.getWidth();
		float left = this.getHalfWidth() - (lineWidth / 2);
		lastColorBox.setX(left);
		arrow.setX(lastColorBox.getMaxX() + margin);
		actColorBox.setX(arrow.getMaxX() + margin);

		lastColorBox.setBackground(new ColorDrawable(InitialColor));
		actColorBox.setBackground(new ColorDrawable(InitialColor));

		this.addChild(lastColorBox);
		this.addChild(arrow);
		this.addChild(actColorBox);
	}

	private void createViewHue()
	{
		float vWidth = bOK.getHeight();

		viewHue = new Image(this.width - Right - margin - vWidth, actColorBox.getMaxY() + margin, vWidth, this.height - Top
				- actColorBox.getMaxY() - margin * 2, "viewHue");
		viewHue.setSprite(SpriteCache.ambilwarna_hue);
		this.addChild(viewHue);

		float cursorSize = Fonts.Mesure("T").height;

		viewCursor = new Image(0, 0, cursorSize, cursorSize, "");
		viewCursor.setSprite(SpriteCache.ambilwarna_cursor);
		this.addChild(viewCursor);

	}

	private GradiantFill gradiantWhite;
	private GradiantFill gradiantBlack;

	private void createTest()
	{
		CB_RectF rec = new CB_RectF(Left + margin, viewHue.getY(), viewHue.getX() - margin * 3 - Left, viewHue.getHeight());

		viewSatVal = new ColorPickerRec(rec, "");
		this.addChild(viewSatVal);

		{
			// Gradiant Test

			// Color blackTransparent = new Color(1f, 1f, 0f, 0.2f);
			// gradiantBlack = new GradiantFill(Color.BLACK, blackTransparent, -30);
			// rectangle FillRecBlack = new rectangle(rec, gradiantBlack);
			// this.addChild(FillRecBlack);
		}

		Color whiteTransparent = new Color(1f, 1f, 1f, 0f);
		gradiantWhite = new GradiantFill(Color.WHITE, whiteTransparent, 0);
		GradiantFilledRectangle FillRecWhite = new GradiantFilledRectangle(rec, gradiantWhite);
		this.addChild(FillRecWhite);

		Color blackTransparent = new Color(0f, 0f, 0f, 0f);
		gradiantBlack = new GradiantFill(Color.BLACK, blackTransparent, 90);
		GradiantFilledRectangle FillRecBlack = new GradiantFilledRectangle(rec, gradiantBlack);
		this.addChild(FillRecBlack);

		float cursorSize = Fonts.Mesure("T").height;

		viewTarget = new Image(0, 0, cursorSize, cursorSize, "");
		viewTarget.setSprite(SpriteCache.ambilwarna_target);
		this.addChild(viewTarget);

	}

	private void hueChanged()
	{
		if (viewSatVal != null) viewSatVal.setHue(actColor.getHue());
	}

	protected void moveCursor()
	{
		float y = viewHue.getHeight() - (getHue() * viewHue.getHeight() / 360.f);
		if (y == viewHue.getHeight()) y = 0.f;

		viewCursor.setX((float) (viewHue.getLeft() - Math.floor(viewCursor.getWidth() / 2)));

		viewCursor.setY((float) (viewHue.getTop() - y - Math.floor(viewCursor.getHeight() / 2)));

	}

	protected void moveTarget()
	{
		float x = getSat() * viewSatVal.getWidth();
		float y = getVal() * viewSatVal.getHeight();

		viewTarget.setX((float) (viewSatVal.getX() + x - Math.floor(viewTarget.getWidth() / 2)));
		viewTarget.setY((float) (viewSatVal.getY() + y - Math.floor(viewTarget.getHeight() / 2)));

	}

	private float getHue()
	{
		return actColor.getHue();
	}

	private float getSat()
	{
		return actColor.getSat();
	}

	private float getVal()
	{
		return actColor.getVal();
	}

	private void setHue(float hue)
	{
		actColor.setHue(hue);
	}

	private void setSat(float sat)
	{
		actColor.setSat(sat);
	}

	private void setVal(float val)
	{
		actColor.setVal(val);
	}

	private void onClick_DracgHueView(float y)
	{
		if (y < 0.f) y = 0.f;
		if (y > viewHue.getHeight()) y = viewHue.getHeight() - 0.001f; // to avoid looping from end to start.
		float hue = 360.f / viewHue.getHeight() * y;
		if (hue == 360.f) hue = 0.f;
		setHue(hue);

		// update view
		viewSatVal.setHue(getHue());
		moveCursor();
		actColorBox.setBackground(new ColorDrawable(actColor));
	}

	private void onClickDragg_Sat(float x, float y)
	{
		if (x < 0.f) x = 0.f;
		if (x > viewSatVal.getWidth()) x = viewSatVal.getWidth();
		if (y < 0.f) y = 0.f;
		if (y > viewSatVal.getHeight()) y = viewSatVal.getHeight();

		setSat(1.f / viewSatVal.getWidth() * x);
		setVal(1.f / viewSatVal.getHeight() * y);

		// update view
		moveTarget();
		actColorBox.setBackground(new ColorDrawable(actColor));
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{

		if (viewHue.contains(x, y))
		{
			onClick_DracgHueView(y - viewHue.getY());
			return true;
		}

		if (viewSatVal.contains(x, y))
		{
			onClickDragg_Sat(x - viewSatVal.getX(), y - viewSatVal.getY());
			return true;
		}

		return super.click(x, y, pointer, button);
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		if (!KineticPan && viewHue.contains(x, y))
		{
			onClick_DracgHueView(y - viewHue.getY());
			return true;
		}

		if (!KineticPan && viewSatVal.contains(x, y))
		{
			onClickDragg_Sat(x - viewSatVal.getX(), y - viewSatVal.getY());
			return true;
		}

		return false;
	}

}
