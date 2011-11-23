package de.droidcachebox.Custom_Controls;

import CB_Core.Config;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Debug;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;

/**
 * Das Debug Panel ist ein Panel, das auf der main Activity verschiebbar ist und
 * Debug Meldungen je nach Settings anzeigt!
 * 
 * @author Longri
 */
public final class DebugInfoPanel extends View
{
	private ActivityManager activityManager;
	private android.app.ActivityManager.MemoryInfo memoryInfo;

	public DebugInfoPanel(Context context)
	{
		super(context);
		activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		memoryInfo = new ActivityManager.MemoryInfo();
	}

	public DebugInfoPanel(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
		memoryInfo = new ActivityManager.MemoryInfo();

		setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				LayoutParams param = (LayoutParams) getLayoutParams();

				float distanceX = 0;
				float distanceY = 0;

				switch (event.getAction() & MotionEvent.ACTION_MASK)
				{
				case MotionEvent.ACTION_DOWN:
					lastX = event.getX();
					lastY = event.getY();

					break;

				case MotionEvent.ACTION_MOVE:
					distanceX = lastX - event.getX();
					distanceY = lastY - event.getY();
					break;
				}

				if (distanceX > 0 || distanceY > 0)
				{

					int x = param.rightMargin;
					int y = param.topMargin;
					x += (int) distanceX;
					y -= (int) distanceY;

					param.setMargins(0, y, x, 0);
					lastX = event.getX();
					lastY = event.getY();
					setLayoutParams(param);
				}

				return true;
			}
		});

	}

	public void onShow()
	{
		counter = new MyCount(500, 500);
		counter.start();
	}

	public void onHide()
	{
		counter.cancel();
	}

	public DebugInfoPanel(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void OnFree()
	{
		counter.cancel();
		counter = null;
	}

	/*
	 * Private Member
	 */
	private int height;
	private int width;
	private TextPaint LayoutTextPaint;
	private int LineSep;
	private StaticLayout LayoutMemInfo;
	private StaticLayout LayoutMsg;
	private StaticLayout LayoutLogMsg;
	private int ContentWidth;
	private float lastX;
	private float lastY;
	private String Msg = "";
	private String Log = "";

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);
		Rect bounds = new Rect();
		LayoutTextPaint = new TextPaint();
		LayoutTextPaint.setTextSize(Sizes.getScaledFontSize_normal());
		LayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LayoutTextPaint.setColor(Color.WHITE);
		LineSep = bounds.height() / 3;
		ContentWidth = width - (Sizes.getCornerSize() * 2);

		LayoutMemInfo = new StaticLayout("1. Zeile " + String.format("%n") + "2.Zeile" + String.format("%n") + "3.Zeile", LayoutTextPaint,
				ContentWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutMsg = new StaticLayout(Msg, LayoutTextPaint, ContentWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

		LayoutLogMsg = new StaticLayout(Log, LayoutTextPaint, ContentWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		int LineCount = LayoutLogMsg.getLineCount();
		if (LineCount > 5)
		{
			String[] Lines = Log.split(String.format("%n"));

			String newLog = "";
			for (int i = 0; i < Lines.length; i++)
			{
				if (i > Lines.length - 5)
				{
					newLog += Lines[i];
				}
			}

			LayoutLogMsg = new StaticLayout(newLog, LayoutTextPaint, ContentWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		}

		// Calc height
		this.height = 0;
		if (Config.settings.DebugMemory.getValue()) this.height += (LineSep * 4) + LayoutMemInfo.getHeight();
		if (Config.settings.DebugShowMsg.getValue()) this.height += (LineSep * 2) + LayoutMsg.getHeight();
		if (Config.settings.DebugShowLog.getValue()) this.height += (LineSep * 2) + LayoutLogMsg.getHeight();

		setMeasuredDimension(this.width, this.height);
	}

	private int measure(int measureSpec)
	{
		int result = 0;

		int specSize = MeasureSpec.getSize(measureSpec);

		result = specSize;

		return result;
	}

	private int left;
	private int top;

	@Override
	protected void onDraw(Canvas canvas)
	{
		int LineColor = Color.argb(200, 200, 255, 200);

		Rect DrawingRec = new Rect(0, 0, width, height);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.SlideDownBackColor));

		left = top = Sizes.getCornerSize();

		if (Config.settings.DebugMemory.getValue()) drawMemInfo(canvas);

		if (Config.settings.DebugShowMsg.getValue()) drawMsg(canvas);

		if (Config.settings.DebugShowLog.getValue()) drawLogMsg(canvas);
	}

	private void drawMsg(Canvas canvas)
	{
		top += ActivityUtils.drawStaticLayout(canvas, LayoutMsg, left, top);
	}

	private void drawLogMsg(Canvas canvas)
	{
		top += ActivityUtils.drawStaticLayout(canvas, LayoutLogMsg, left, top);
	}

	private void drawMemInfo(Canvas canvas)
	{

		// calculate total_bytes_used using mi...

		long available_bytes = activityManager.getMemoryClass();
		String line1 = "Memory Info:";
		String line2 = "Gesamt: " + available_bytes * 1024 + " kB";
		String line3 = "Free: " + (available_bytes * 1024 - Debug.getNativeHeapAllocatedSize() / 1024) + " kB";
		LayoutMemInfo = new StaticLayout(line1 + String.format("%n") + line2 + String.format("%n") + line3, LayoutTextPaint, ContentWidth,
				Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

		top += ActivityUtils.drawStaticLayout(canvas, LayoutMemInfo, left, top);

	}

	private class MyCount extends CountDownTimer
	{
		public MyCount(long millisInFuture, long countDownInterval)
		{
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish()
		{
			invalidate();
			this.start();
			// Toast.makeText(getApplicationContext(), "timer",
			// Toast.LENGTH_LONG).show();
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			// TODO Auto-generated method stub

		}
	}

	MyCount counter = null;

	public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}

	public void setMsg(String msg)
	{

		Msg = msg;
		this.requestLayout();
	}

	public void addLogMsg(String Msg)
	{
		Log += Msg;

		this.requestLayout();
	}
}
