/* 
 * Copyright (C) 2011-2012 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.Custom_Controls;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;

/**
 * @author Longri
 */
public class MultiToggleButton extends Button implements OnClickListener, OnLongClickListener
{

	/*
	 * wenn True wird der letzte State nur über ein LongClick angewählt
	 */
	private boolean lastStateWithLongClick = false;
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
		setState(0, true);
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
		this.setBackgroundResource(Config.settings.nightMode.getValue() ? R.drawable.night_btn : R.drawable.day_btn);
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
			try
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
			catch (Exception e)
			{
				e.printStackTrace();
			}
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
		setState(ID, false);
	}

	public void setState(int ID, boolean force)
	{
		if (StateId == ID && !force) return;

		StateId = ID;
		if (StateId > State.size() - 1) StateId = 0;
		aktState = State.get(StateId);
		this.setText(aktState.Text);
		led = null;
		System.gc();
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
		if (lastStateWithLongClick)
		{
			if (StateId == State.size() - 2)
			{
				StateId = 0;
			}
			else
			{
				StateId++;
			}
		}
		else
		{
			StateId++;
		}

		setState(StateId, true);
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
		bt.setState(0, true);
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt, String txtOn, String txtOff)
	{
		bt.clearStates();
		bt.addState(txtOff, Global.getColor(R.attr.ToggleBtColor_off));
		bt.addState(txtOn, Global.getColor(R.attr.ToggleBtColor_on));
		bt.setState(0, true);
	}

	private CB_RectF hitRec = null;
	private boolean onTouch = false;

	public boolean hitTest(Vector2 pos)
	{
		if (hitRec != null)
		{
			if (hitRec.contains(pos.x, pos.y))
			{
				onClick();
				return true;
			}
		}
		return false;
	}

	public void TouchRelease()
	{
		onTouch = false;
	}

	public boolean touchDownTest(Vector2 pos)
	{
		if (hitRec != null)
		{
			if (hitRec.contains(pos.x, pos.y))
			{
				onTouch = true;
				return true;
			}
		}
		return false;
	}

	BitmapFont font;

	private Sprite led;
	private Sprite btn;

	public void Render(SpriteBatch batch, CB_RectF rect, BitmapFont font)
	{

		hitRec = rect;

		// draw button
		btn = SpriteCache.ToggleBtn.get(onTouch ? 1 : 0);
		btn.setBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
		btn.draw(batch);

		// draw led
		led = SpriteCache.ToggleBtn.get(this.StateId + 2);
		led.setBounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
		led.draw(batch);

		// draw btn text
		TextBounds bounds = font.getBounds(this.getText());
		float halfWidth = bounds.width / 2;
		font.draw(batch, this.getText(), rect.getX() + (rect.getWidth() / 2) - halfWidth, rect.getY() + (rect.getHeight() / 2)
				+ bounds.height);

	}

	public void setLastStateWithLongClick(boolean value)
	{
		lastStateWithLongClick = value;
		setState(0, true);
	}

	@Override
	public boolean onLongClick(View v)
	{
		if (lastStateWithLongClick)
		{
			setState(State.size() - 1, true);
		}
		else
		{
			onClick(v);
		}
		return true;
	}

	public boolean onLongClick()
	{
		onLongClick(null);
		return true;
	}

	public boolean longHitTest(Vector2 clickedAt)
	{
		if (hitRec != null)
		{
			if (hitRec.contains(clickedAt.x, clickedAt.y))
			{
				onLongClick();
				return true;
			}
		}
		return false;

	}

}
