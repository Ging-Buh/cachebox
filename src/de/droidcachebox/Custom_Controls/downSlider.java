package de.droidcachebox.Custom_Controls;


import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Cache.DrawStyle;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.WaypointView;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;



/*
 * Control Tamplate zum Copieren!
 * 
 * XML Layout einbindung über :
 * 
    <de.droidcachebox.Custom_Controls.downSlider
			android:id="@+id/myName" android:layout_height="wrap_content"
			android:layout_width="fill_parent" android:layout_marginLeft="2dip"
			android:layout_marginRight="2dip" android:layout_marginTop="1dip" />
 */





public final class downSlider extends View implements SelectedCacheEvent 
{

	public downSlider(Context context) 
	{
		super(context);
	}

	public downSlider(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		SelectedCacheEventList.Add(this);
	}

	public downSlider(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		
	}
	
	
	/*
	 *  Private Member
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
	private int WPInfoHeight=0;
		
	
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
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) 
    {
        int result = 0;
        
        int specSize = MeasureSpec.getSize(measureSpec);

       
            result = specSize;
        
        
        return result;
    }

    int yPos =0;
    Rect mBtnRec = new Rect();
    Rect mBackRec = new Rect();
    Paint backPaint;
	private Cache mCache;
	private Waypoint mWaypoint;
	private int CacheInfoHeight= 0;
    
	@Override
	protected void onDraw(Canvas canvas) 
	{
	
		if(CacheInfoHeight==0)CacheInfoHeight = (int) (Global.scaledFontSize_normal * 4.9);
		
		final Drawable Slide = Global.BtnIcons[0];
		
		mBtnRec.set(-10, yPos, width+10 , yPos+50);
		Slide.setBounds(mBtnRec);
   	 	
		Slide.setColorFilter(new PorterDuffColorFilter(Global.getColor(R.attr.SlideDownColorFilter), android.graphics.PorterDuff.Mode.MULTIPLY ));
   	 	Slide.draw(canvas);
		
   	 	
   	 	// Draw Background
   	 	if (backPaint==null)
   	 	{
   	 		backPaint= new Paint();
   	 		backPaint.setColor(Global.getColor(R.attr.SlideDownBackColor));
   	 		//backPaint.setColor(Color.RED); //DEBUG RED
   	 	}
   	 	mBackRec.set(-10, 0, width+10 , yPos+2);
   	 	canvas.drawRect(mBackRec, backPaint);
  
   	 	if (mCache == null)
			return;

   	 	// draw Cache Name
		canvas.drawText(mCache.Name,5,yPos+ 30, CacheNameView.paint);
   	 	
   	 	// draw WP Info
   	 	int versatz=-yPos+WPInfoHeight;
   	 	canvas.translate(0,-versatz);
   	 	Boolean WPisDraw = drawWPInfo(canvas);
		canvas.restore();
   	 	
   	 
   	 	// draw Cache Info
		versatz +=CacheInfoHeight;
		canvas.translate(5,-versatz);
   	 	mCache.DrawInfo(canvas, width - 10, CacheInfoHeight, WPisDraw? Global.getColor(R.attr.ListBackground) : Global.getColor(R.attr.ListBackground_select), DrawStyle.withoutBearing);
   	 	canvas.restore();
   	 	
   	
		
	}

	
	private final int CornerSize =Global.CornerSize;
	
	private Boolean drawWPInfo(Canvas canvas) {
		if(mWaypoint==null)
				return false;
		 
		 
		 int LineColor = Global.getColor(R.attr.ListSeparator);
		 Rect DrawingRec = new Rect(5, 5, width-5, WPInfoHeight-5);
		 ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground_select));
  
  
		 int left= 15;
		 int top = LineSep *2;
		
		 int iconWidth = 0;
		 // draw icon
		 if (((int)mWaypoint.Type.ordinal()) < Global.CacheIconsBig.length)
			iconWidth=ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[(int)mWaypoint.Type.ordinal()], CornerSize/2,CornerSize, imgSize);

		 // draw Text info
		 left += iconWidth;
		 top += ActivityUtils.drawStaticLayout(canvas, WPLayoutName, left, top);
		 top += ActivityUtils.drawStaticLayout(canvas, WPLayoutDesc, left, top);
		 top += ActivityUtils.drawStaticLayout(canvas, WPLayoutCord, left, top);
		 if (mWaypoint.Clue != null)ActivityUtils.drawStaticLayout(canvas, WPLayoutClue, left, top);
					
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}
	
	/*public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}
	*/
	public void setPos(int Pos)
	{
		yPos=(Pos<0)? 0 : Pos;
		this.invalidate();
	}
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
		if (yPos>height*0.7)
		{
			yPos=height-50;
		}
		else
		{
			yPos=0;
		}
		
		
		invalidate();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) 
	{
		mCache=cache;
		mWaypoint=waypoint;
		
		int TextWidth = this.width- this.imgSize;
		
		Rect bounds = new Rect();
		WPLayoutTextPaint = new TextPaint();
		WPLayoutTextPaint.setTextSize(Global.scaledFontSize_normal);
		WPLayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LineSep = bounds.height()/3;
        
		
		String Clue = "";
		if(mWaypoint!=null)
		{
		if (waypoint.Clue != null)
			Clue = waypoint.Clue;
		WPLayoutTextPaint.setAntiAlias(true);
		WPLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
		WPLayoutCord = new StaticLayout(Global.FormatLatitudeDM(waypoint.Latitude()) + " / " + Global.FormatLongitudeDM(waypoint.Longitude()), WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		WPLayoutDesc = new StaticLayout(waypoint.Description, WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		WPLayoutClue = new StaticLayout(Clue, WPLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		WPLayoutTextPaintBold = new TextPaint(WPLayoutTextPaint);
		WPLayoutTextPaintBold.setFakeBoldText(true);
		WPLayoutName = new StaticLayout(waypoint.GcCode + ": " + waypoint.Title, WPLayoutTextPaintBold, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		WPInfoHeight = (LineSep*5)+ WPLayoutCord.getHeight()+WPLayoutDesc.getHeight()+WPLayoutClue.getHeight()+WPLayoutName.getHeight();
		}
		
	}
	
}
