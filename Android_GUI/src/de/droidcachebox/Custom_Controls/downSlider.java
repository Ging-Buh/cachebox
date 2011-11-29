/* 
 * Copyright (C) 2011 team-cachebox.de
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

import java.util.Iterator;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Enums.Attributes;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Events.GpsStateChangeEvent;
import de.droidcachebox.Events.GpsStateChangeEventList;
import de.droidcachebox.Locator.GPS;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.Forms.ScreenLock;

public final class downSlider extends View implements SelectedCacheEvent, GpsStateChangeEvent
{

	public downSlider(Context context)
	{
		super(context);
	}

	public downSlider(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		SelectedCacheEventList.Add(this);

		mGestureDetector = new GestureDetector(context, new LearnGestureListener());

		this.setOnTouchListener(myTouchListner);

		Me = this;
		GpsStateChangeEventList.Add(this);

	}

	public downSlider(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

	}

	/*
	 * Private Member
	 */
	private int height;
	private int width;
	private int imgSize;
	private StaticLayout WPLayoutName;
	private StaticLayout WPLayoutDesc;
	private StaticLayout WPLayoutCord;
	private StaticLayout WPLayoutClue;
	private TextPaint WPLayoutTextPaint;
	private TextPaint WPLayoutTextPaintBold;
	private int LineSep;
	private int WPInfoHeight = 0;
	private int GPSInfoHeight = 0;
	private static int QuickButtonHeight;
	private int QuickButtonMaxHeight;
	private int attHeight = -1;
	int attCompleadHeight = 0;
	int topCalc = -1;
	int attLineHeight = -1;

	private static downSlider Me;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);

		imgSize = (int) ((height / 5) * 0.6);

		setMeasuredDimension(this.width, this.height);
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measure(int measureSpec)
	{
		QuickButtonMaxHeight = Sizes.getQuickButtonListHeight();
		int result = 0;
		int specSize = MeasureSpec.getSize(measureSpec);
		result = specSize;

		return result;
	}

	int yPos = 0;
	Rect mBtnRec = new Rect();
	Rect mBackRec = new Rect();
	Paint backPaint;
	private Cache mCache;
	private Waypoint mWaypoint;
	private int CacheInfoHeight = 0;
	private Paint paint;
	public static boolean isInitial = false;
	private boolean drag;
	private boolean ButtonDrag;
	private int lastDragYPos = 0;
	private boolean swipeUp = false;
	private boolean swipeDown = false;

	private OnTouchListener myTouchListner = new OnTouchListener()
	{

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{

			mGestureDetector.onTouchEvent(event);

			// events when touching the screen

			int eventaction = event.getAction();
			int X = (int) event.getX();
			int Y = (int) event.getY();
			if (contains(X, Y)) drag = true;
			if (Y < QuickButtonHeight) ButtonDrag = true;

			switch (eventaction)
			{
			case MotionEvent.ACTION_DOWN: // touch down so check if the finger
											// is on a ball
				AnimationIsRunning = false;
				break;

			case MotionEvent.ACTION_MOVE: // touch drag with the ball
				// move the balls the same as the finger

				// setDebugMsg("Move:" + String.format("%n")+ "x= " + X +
				// String.format("%n") + "y= " + Y);
				if (drag)
				{
					int value = Y - 25;// y - 25 minus halbe Button Höhe
					int buttom = (int) (height - (Sizes.getScaledRefSize_normal() * 2.2));
					if (value > buttom) value = buttom - 1;

					setPos(value);
				}
				break;

			case MotionEvent.ACTION_UP:
				if (drag)
				{
					drag = false;
					ButtonDrag = false;
					ActionUp();
				}

				break;

			}

			if (drag)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	};

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (ScreenLock.SliderMoves) return;

		/**
		 * Beim ersten Zeichnen, wird der Letzte Zustand abgefragt!
		 */
		if (!isInitial)
		{
			if (Config.settings.quickButtonShow.getValue() && Config.settings.quickButtonLastShow.getValue())
			{
				setPos(QuickButtonMaxHeight);
			}
			else
			{
				setPos(0);
			}
			isInitial = true;
		}

		if (!drag && !AnimationIsRunning && !ButtonDrag)
		{
			yPos = QuickButtonHeight = Config.settings.quickButtonShow.getValue() ? main.getQuickButtonHeight() : 0;
			// Toast.makeText(main.mainActivity, "!drag to" +
			// String.valueOf(yPos), Toast.LENGTH_SHORT).show();
		}

		float FSize = ((float) (Sizes.getScaledFontSize_big() * 1.3));

		if (paint == null)
		{
			paint = new Paint();
			paint.setColor(Global.getColor(R.attr.TextColor));
			paint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
			paint.setAntiAlias(true);
		}

		final Drawable Slide = Global.BtnIcons[0];

		mBtnRec.set(-10, yPos - 2, width + 10, (int) (yPos + 2 + Sizes.getScaledRefSize_normal() * 3.3));
		Slide.setBounds(mBtnRec);
		Slide.draw(canvas);

		// Draw Background
		if (backPaint == null)
		{
			backPaint = new Paint();
			backPaint.setColor(Global.getColor(R.attr.SlideDownBackColor));
			// backPaint.setColor(Color.RED); //DEBUG RED
		}
		mBackRec.set(-10, QuickButtonHeight + 2, width + 10, yPos + 1);
		canvas.drawRect(mBackRec, backPaint);

		if (mCache == null) return;

		// Draw Slide Icons
		final Drawable SlideIcon = (QuickButtonHeight > 0) ? Global.Icons[41] : Global.Icons[40];
		Rect SlideIconRec = new Rect(mBtnRec);
		SlideIconRec.set(SlideIconRec.left + 10, SlideIconRec.top, SlideIconRec.height(), SlideIconRec.bottom);
		SlideIcon.setBounds(SlideIconRec);
		SlideIcon.draw(canvas);

		SlideIconRec.offset(width - SlideIconRec.width(), 0);
		SlideIcon.setBounds(SlideIconRec);
		SlideIcon.draw(canvas);

		// draw Cache Name
		canvas.drawText(mCache.Name, 20 + SlideIconRec.width(), yPos + (FSize + (FSize / 3)), paint);

		// Draw only is visible
		if (Config.settings.quickButtonShow.getValue())
		{
			if (yPos <= QuickButtonMaxHeight) return;
		}
		else
		{
			if (yPos <= 1) return;
		}

		if (Config.settings.quickButtonShow.getValue())
		{
			canvas.clipRect(mBackRec);
		}

		// draw GPS Info
		int versatz = -yPos + GPSInfoHeight;
		canvas.translate(0, -versatz);
		drawGPSInfo(canvas);
		canvas.translate(0, +versatz);
		// canvas.clipRect(mBackRec);

		// if(yPos>1)return;
		// draw WP Info
		versatz += WPInfoHeight;
		canvas.translate(0, -versatz);
		Boolean WPisDraw = drawWPInfo(canvas);
		canvas.translate(0, +versatz);
		// canvas.clipRect(mBackRec);

		// draw Cache Info

		Iterator<Attributes> iterator = mCache.getAttributes().iterator();
		int lines = 0;
		if (iterator != null && iterator.hasNext())
		{
			if (attHeight == -1) attHeight = (int) (Sizes.getIconSize() * 0.75);

			lines = 1 + (mCache.getAttributes().size() / 8);
			attCompleadHeight = (int) (lines * attHeight * 1.3);

		}

		if (attLineHeight == -1) attLineHeight = attHeight + (Sizes.getScaledFontSize() / 3);

		if (CacheInfoHeight == 0)
		{
			CacheInfoHeight = (int) (FSize * 9) + attCompleadHeight;
			topCalc = (int) (CacheInfoHeight - (attLineHeight * lines) - attLineHeight + Sizes.getScaledFontSize());
		}

		versatz += CacheInfoHeight;
		canvas.translate(5, -versatz);
		CacheDraw.DrawInfo(mCache, canvas, width - 10, CacheInfoHeight,
				WPisDraw ? Global.getColor(R.attr.ListBackground) : Global.getColor(R.attr.ListBackground_select),
				DrawStyle.withOwnerAndName);

		// draw Attributes

		int left = 8;

		int top = topCalc;
		if (iterator != null && iterator.hasNext())
		{
			int i = 0;
			do
			{
				Attributes att = iterator.next();
				String uri = "drawable/" + att.getImageName();

				int imageResource = getResources().getIdentifier(uri, null, main.mainActivity.getPackageName());
				Drawable image = null;
				try
				{
					image = getResources().getDrawable(imageResource);
				}
				catch (NotFoundException e)
				{
					image = Global.Icons[34];
				}

				if (image != null)
				{
					left += ActivityUtils.PutImageTargetHeight(canvas, image, left, top, attHeight) + 3;
					i++;
					if (i % 8 == 0 && i > 7)
					{
						left = 8;
						top += attLineHeight;
					}
				}

			}
			while (iterator.hasNext());

		}
		canvas.translate(0, +versatz);
	}

	private Boolean drawWPInfo(Canvas canvas)
	{
		if (mWaypoint == null) return false;

		int LineColor = Global.getColor(R.attr.ListSeparator);
		Rect DrawingRec = new Rect(5, 5, width - 5, WPInfoHeight - 5);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground_select));

		int left = 15;
		int top = LineSep * 2;

		int iconWidth = 0;
		// draw icon
		if (((int) mWaypoint.Type.ordinal()) < Global.CacheIconsBig.length) iconWidth = ActivityUtils.PutImageTargetHeight(canvas,
				Global.CacheIconsBig[(int) mWaypoint.Type.ordinal()], Sizes.getHalfCornerSize(), Sizes.getCornerSize(), imgSize);

		// draw Text info
		left += iconWidth;
		top += ActivityUtils.drawStaticLayout(canvas, WPLayoutName, left, top);
		top += ActivityUtils.drawStaticLayout(canvas, WPLayoutDesc, left, top);
		top += ActivityUtils.drawStaticLayout(canvas, WPLayoutCord, left, top);
		if (mWaypoint.Clue != null) ActivityUtils.drawStaticLayout(canvas, WPLayoutClue, left, top);

		return true;
	}

	private Boolean drawGPSInfo(Canvas canvas)
	{
		if (GPSInfoHeight == 0) return false;

		int LineColor = Global.getColor(R.attr.ListSeparator);
		Rect DrawingRec = new Rect(5, 5, width - 5, GPSInfoHeight - 5);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground));

		main.setSatStrength();

		Bitmap b = Bitmap.createBitmap(main.strengthLayout.getMeasuredWidth(), main.strengthLayout.getMeasuredHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		main.strengthLayout.draw(c);

		int left = width - b.getWidth() - 22;
		int top = GPSInfoHeight - b.getHeight() - 10;

		canvas.drawBitmap(b, left, top, null);

		left = 15;
		top = LineSep * 2;

		int iconWidth = 0;
		// draw icon
		iconWidth = ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[30], Sizes.getHalfCornerSize(), Sizes.getCornerSize(), imgSize);

		// draw Text info
		left += iconWidth;
		top += ActivityUtils.drawStaticLayout(canvas, GPSLayout, left, top);

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}

	public static void ButtonShowStateChanged()
	{
		if (downSlider.Me != null)
		{
			if (Config.settings.quickButtonShow.getValue())
			{
				downSlider.Me.setPos(downSlider.Me.QuickButtonMaxHeight);
			}
			else
			{
				downSlider.Me.setPos(0);
			}
		}

	}

	public void setPos(int Pos)
	{
		if (Pos >= 0)
		{
			yPos = Pos;
			if (Config.settings.quickButtonShow.getValue())
			{
				if (Pos <= QuickButtonMaxHeight)
				{
					QuickButtonHeight = Pos;
				}
				else
				{
					QuickButtonHeight = QuickButtonMaxHeight;
				}
			}
			else
			{
				QuickButtonHeight = 0;
			}
			((main) main.mainActivity).setQuickButtonHeight(QuickButtonHeight);

		}
		else
		{
			yPos = 0;

		}

		// chk if info Visible then update info
		int InfoBeginnAt = Config.settings.quickButtonShow.getValue() ? QuickButtonMaxHeight : 0;
		if (yPos > InfoBeginnAt)
		{
			if (!isVisible) startUpdateTimer();
			isVisible = true;

		}
		else
		{
			isVisible = false;

		}

		this.invalidate();
	}

	int tmpPos = 0;

	public void setPos_onUI(final int Pos)
	{
		tmpPos = Pos;
		Thread t = new Thread()
		{
			public void run()
			{
				((main) main.mainActivity).runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						((main) main.mainActivity).InfoDownSlider.setPos(Pos);
					}
				});
			}
		};

		t.start();

	}

	private void startUpdateTimer()
	{
		handler.postDelayed(task, 400);
	}

	Handler handler = new Handler();
	Runnable task = new Runnable()
	{

		@Override
		public void run()
		{
			String provider = LocationManager.GPS_PROVIDER;
			Location location = Global.Locator.getLocation();
			// Location location = ((main)
			// main.mainActivity).locationManager.getLastKnownLocation(provider);

			if (location != null)
			{
				setNewLocation(location);

				// getAllSatellites();

				if (isVisible) handler.postDelayed(task, 1200);
			}
		}
	};

	public int getPos()
	{
		return yPos;
	}

	public boolean contains(int x, int y)
	{
		return mBtnRec.contains(x, y);
	}

	public void ActionUp() // Slider zurück scrolllen lassen
	{
		boolean QuickButtonShow = Config.settings.quickButtonShow.getValue();

		// check if QuickButtonList snap in
		if (yPos >= (QuickButtonMaxHeight * 0.5) && QuickButtonShow)
		{
			QuickButtonHeight = QuickButtonMaxHeight;
			Config.settings.quickButtonLastShow.setValue(true);
			Config.AcceptChanges();
		}
		else
		{
			QuickButtonHeight = 0;
			Config.settings.quickButtonLastShow.setValue(false);
			Config.AcceptChanges();
		}

		((main) main.mainActivity).setQuickButtonHeight(QuickButtonHeight);

		if (swipeUp || swipeDown)
		{
			if (swipeUp)
			{
				startAnimationTo(QuickButtonShow ? QuickButtonHeight : 0);
			}
			else
			{
				startAnimationTo((int) (height - (Sizes.getScaledFontSize() * 2.2)));
			}
			swipeUp = swipeDown = false;

		}
		else
		{
			if (yPos > height * 0.7)
			{
				startAnimationTo((int) (height - (Sizes.getScaledFontSize() * 2.2)));
			}
			else
			{
				startAnimationTo(QuickButtonShow ? QuickButtonHeight : 0);
				// isVisible=false;
			}
		}

		invalidate();
	}

	public static int getAktQuickButtonHeight()
	{
		return Config.settings.quickButtonShow.getValue() ? QuickButtonHeight : 0;
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		mCache = cache;
		mWaypoint = waypoint;

		attCompleadHeight = 0;
		CacheInfoHeight = 0;
		WPInfoHeight = 0;
		topCalc = 0;

		int TextWidth = this.width - this.imgSize;

		Rect bounds = new Rect();
		WPLayoutTextPaint = new TextPaint();
		WPLayoutTextPaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
		WPLayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LineSep = bounds.height() / 3;

		String Clue = "";
		if (mWaypoint != null)
		{
			if (waypoint.Clue != null) Clue = waypoint.Clue;
			WPLayoutTextPaint.setAntiAlias(true);
			WPLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
			WPLayoutCord = new StaticLayout(Global.FormatLatitudeDM(waypoint.Latitude()) + " / "
					+ Global.FormatLongitudeDM(waypoint.Longitude()), WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
					false);
			WPLayoutDesc = new StaticLayout(waypoint.Description, WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			WPLayoutClue = new StaticLayout(Clue, WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			WPLayoutTextPaintBold = new TextPaint(WPLayoutTextPaint);
			WPLayoutTextPaintBold.setFakeBoldText(true);
			WPLayoutName = new StaticLayout(waypoint.GcCode + ": " + waypoint.Title, WPLayoutTextPaintBold, TextWidth,
					Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
			WPInfoHeight = (LineSep * 5) + WPLayoutCord.getHeight() + WPLayoutDesc.getHeight() + WPLayoutClue.getHeight()
					+ WPLayoutName.getHeight();
		}

		this.invalidate();

	}

	private Boolean isVisible = false;

	private String mLatitude = "";
	private String mLongitude = "";
	private String mSats;
	private String mAccuracy;
	private String mAlt;
	private TextPaint GPSLayoutTextPaint;

	private StaticLayout GPSLayout;

	public void setNewLocation(Location location)
	{

		if (this.width == 0) return;

		// mSats = String.valueOf(location.getExtras().getInt("satellites"));
		mSats = GPS.getSatAndFix();

		mAccuracy = String.valueOf((int) location.getAccuracy());
		mAlt = Global.Locator.getAltString();
		mLatitude = Global.FormatLatitudeDM(location.getLatitude());
		mLongitude = Global.FormatLongitudeDM(location.getLongitude());

		String br = String.format("%n");
		String Text = GlobalCore.Translations.Get("current") + " " + mLatitude + " " + mLongitude + br + GlobalCore.Translations.Get("alt")
				+ " " + mAlt + br + GlobalCore.Translations.Get("accuracy") + "  +/- " + mAccuracy + "m" + br
				+ GlobalCore.Translations.Get("sats") + " " + mSats;

		if (GPSLayoutTextPaint == null)
		{
			GPSLayoutTextPaint = new TextPaint();
			GPSLayoutTextPaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
			GPSLayoutTextPaint.setAntiAlias(true);
			GPSLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
		}

		int TextWidth = this.width - this.imgSize;
		GPSLayout = new StaticLayout(Text, GPSLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		GPSInfoHeight = (mLatitude.equals("")) ? 0 : (LineSep * 4) + GPSLayout.getHeight();

		this.invalidate();
	}

	private boolean AnimationIsRunning = false;
	private final int AnimationTime = 50;
	private final double AnimationMulti = 1.4;
	private int AnimationDirection = -1;
	private int AnimationTarget = 0;

	private void startAnimationTo(int newYPos)
	{
		if (yPos == newYPos) return; // wir brauchen nichts Animieren

		AnimationIsRunning = true;
		AnimationTarget = newYPos;
		if (yPos > newYPos) AnimationDirection = -1;
		else
			AnimationDirection = 1;
		handler.postDelayed(AnimationTask, AnimationTime);
	}

	Runnable AnimationTask = new Runnable()
	{

		@Override
		public void run()
		{

			if (!AnimationIsRunning) return; // Animation wurde abgebrochen

			int newValue = 0;
			if (AnimationDirection == -1)
			{
				int tmp = yPos - AnimationTarget;
				if (tmp <= 0)// Div 0 vehindern
				{
					setPos_onUI(AnimationTarget);
					AnimationIsRunning = false;
				}

				newValue = (int) (yPos - (tmp / AnimationMulti));
				if (newValue <= AnimationTarget)
				{
					setPos_onUI(AnimationTarget);
					AnimationIsRunning = false;
				}
				else
				{
					setPos_onUI(newValue);
					handler.postDelayed(AnimationTask, AnimationTime);
				}
			}
			else
			{
				int tmp = AnimationTarget - yPos;
				if (tmp <= 0)// Div 0 vehindern
				{
					setPos_onUI(AnimationTarget);
					AnimationIsRunning = false;
				}
				else
				{
					newValue = (int) (yPos + (tmp / AnimationMulti));
					if (newValue >= AnimationTarget)
					{
						setPos_onUI(AnimationTarget);
						AnimationIsRunning = false;
					}
					else
					{
						setPos_onUI(newValue);
						handler.postDelayed(AnimationTask, AnimationTime);
					}
				}

			}

		}
	};

	private GestureDetector mGestureDetector;

	class LearnGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		@Override
		public boolean onSingleTapUp(MotionEvent ev)
		{
			Log.d("onSingleTapUp", ev.toString());
			return true;
		}

		@Override
		public void onShowPress(MotionEvent ev)
		{
			Log.d("onShowPress", ev.toString());
		}

		@Override
		public void onLongPress(MotionEvent ev)
		{
			Log.d("onLongPress", ev.toString());
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
		{
			Log.d("onScroll", e1.toString());
			return true;
		}

		@Override
		public boolean onDown(MotionEvent ev)
		{
			Log.d("onDownd", ev.toString());
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
		{
			if (velocityY > 500)
			{
				swipeDown = true;
			}
			else if (velocityY < -500)
			{
				swipeUp = true;
			}
			return true;
		}
	}

	@Override
	public void GpsStateChanged()
	{
		this.invalidate();

	}

}
