package de.cachebox_test.Custom_Controls;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Map.SpriteCache;

public class MultiToggleButton extends Button implements OnClickListener
{

	private Resources res;

	public MultiToggleButton()
	{
		super(main.mainActivity);
	}

	public MultiToggleButton(Context context)
	{
		super(context);

		res = context.getResources();
	}

	public MultiToggleButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		res = context.getResources();
		setOnClickListener(this);
		State.add(new States("off", Color.GRAY));
		setState(0);
	}

	public MultiToggleButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		res = context.getResources();
	}

	/*
	 * Private Member
	 */

	private Drawable mLedDrawable;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);

		Math.min(chosenWidth, chosenHeight);

		setMeasuredDimension(widthSize, heightSize);
		this.setBackgroundResource(main.N ? R.drawable.night_btn : R.drawable.day_btn);
	}

	private int chooseDimension(int mode, int size)
	{
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY)
		{
			return size;
		}
		else
		{ // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		}
	}

	// in case there is no size specified
	private int getPreferredSize()
	{
		return 50;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{

		mLedDrawable = res.getDrawable(R.drawable.day_btn_toggle_off);

		try
		{
			this.setTextColor(Global.getColor(R.attr.TextColor));
		}
		catch (Exception e)
		{
			// Designer Error
			this.setTextColor(Color.BLUE);
		}

		super.onDraw(canvas);

		// canvas.drawColor(Color.RED);
		int width = getWidth();
		int height = getHeight();
		int ledHeight = 0;
		int ledWidth = 0;

		int left;
		int top = 0;

		final Drawable finalLed = mLedDrawable;
		if (finalLed != null)
		{
			Rect mRect = new Rect();
			ledHeight = finalLed.getIntrinsicHeight();
			ledWidth = finalLed.getIntrinsicWidth();
			left = (width / 2) - (ledWidth / 2);
			top = height - ledHeight;
			PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(aktState.Color, android.graphics.PorterDuff.Mode.MULTIPLY);
			mRect.set(left, top, ledWidth + left, ledHeight + top);
			finalLed.setBounds(mRect);

			finalLed.setColorFilter(colorFilter);
			finalLed.draw(canvas);
		}

		Rect tRec = new Rect();
		tRec.set(10, 10, width - 10, height - top - 3);

	}

	public class States
	{
		public String Text;
		public int Color;

		public States(String text, int color)
		{
			Text = text;
			Color = color;
		}
	}

	private ArrayList<States> State = new ArrayList<MultiToggleButton.States>();

	public void addState(String Text, int color)
	{
		State.add(new States(Text, color));
	}

	public void setState(int ID)
	{
		StateId = ID;
		if (StateId > State.size() - 1) StateId = 0;
		aktState = State.get(StateId);
		this.setText(aktState.Text);
		this.invalidate();
	}

	public void clearStates()
	{
		State.clear();
	}

	private States aktState;
	private int StateId = 0;

	@Override
	public void onClick(View arg0)
	{
		StateId++;
		setState(StateId);
	}

	public void onClick()
	{
		onClick(null);
	}

	public int getState()
	{
		return StateId;
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt)
	{
		String ButtonTxt = (String) bt.getText();
		bt.clearStates();
		bt.addState(ButtonTxt, Global.getColor(R.attr.ToggleBtColor_off));
		bt.addState(ButtonTxt, Global.getColor(R.attr.ToggleBtColor_on));

	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt, String txtOn, String txtOff)
	{
		bt.clearStates();
		bt.addState(txtOff, Global.getColor(R.attr.ToggleBtColor_off));
		bt.addState(txtOn, Global.getColor(R.attr.ToggleBtColor_on));

	}

	private RectF hitRec = null;

	public boolean hitTest(Vector2 pos)
	{
		if (hitRec != null)
		{
			onClick();
			return hitRec.contains(pos.x, pos.y);
		}
		return false;
	}

	BitmapFont font;

	public void Render(SpriteBatch batch, Vector2 togglepos, float togglewidth, float toggleheight)
	{
		if (font == null)
		{
			font = new BitmapFont();
			font.setColor(0.0f, 0.0f, 0.0f, 1.0f);
			font.setScale(1.4f);
		}

		// set hitRec
		if (hitRec == null)
		{
			hitRec = new RectF(togglepos.x, togglepos.y, togglepos.x + togglewidth, togglepos.y + toggleheight);
		}

		// draw button
		Sprite btn = SpriteCache.ToggleBtn.get(0);
		btn.setSize(togglewidth, toggleheight);
		btn.setPosition(togglepos.x, togglepos.y);
		btn.draw(batch);

		// draw led
		Sprite led = SpriteCache.ToggleBtn.get(this.StateId + 2);
		led.setSize(togglewidth, toggleheight);
		led.setPosition(togglepos.x, togglepos.y);
		led.draw(batch);

		// draw btn text
		TextBounds bounds = font.getBounds(this.getText());
		float halfWidth = bounds.width / 2;
		font.draw(batch, this.getText(), togglepos.x + (togglewidth / 2) - halfWidth, togglepos.y + (toggleheight / 2) + bounds.height);

	}

}
