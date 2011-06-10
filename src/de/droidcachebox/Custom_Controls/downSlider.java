package de.droidcachebox.Custom_Controls;


import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Cache.DrawStyle;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
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
import android.view.View;



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
	private int GPSInfoHeight=0;	
	
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
	private Paint paint;
    
	@Override
	protected void onDraw(Canvas canvas) 
	{
		int FSize = (int) (Global.scaledFontSize_normal*1.2);
	
		if(CacheInfoHeight==0)CacheInfoHeight = (int) (FSize * 4.7);
		
		if (paint==null)
		{
			paint = new Paint();
			paint.setColor(Global.getColor(R.attr.TextColor));
			paint.setTextSize(FSize);
			paint.setAntiAlias(true);
		}
		
		final Drawable Slide = Global.BtnIcons[0];
		
		mBtnRec.set(-10, yPos - 2, width+10 , yPos + 2 + CacheNameView.getMyHeight());
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
		canvas.drawText(mCache.Name,5,yPos + (FSize + (FSize/3)), paint);
		
		
		// draw GPS Info
		int versatz = -yPos+GPSInfoHeight;
		canvas.translate(0,-versatz);
		drawGPSInfo(canvas);
		canvas.restore();
   	 	
   	 	// draw WP Info
   	 	versatz += WPInfoHeight;
   	 	canvas.translate(0,-versatz);
   	 	Boolean WPisDraw = drawWPInfo(canvas);
		canvas.restore();
   	 	
   	 
   	 	// draw Cache Info
		versatz += CacheInfoHeight;
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
	
	
	private Boolean drawGPSInfo(Canvas canvas) 
	{
		if(GPSInfoHeight==0)
				return false;
		 
		 
		 int LineColor = Global.getColor(R.attr.ListSeparator);
		 Rect DrawingRec = new Rect(5, 5, width-5, GPSInfoHeight-5);
		 ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground));
  
  
		 int left= 15;
		 int top = LineSep *2;
		
		 int iconWidth = 0;
		 // draw icon
		 iconWidth=ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[30], CornerSize/2,CornerSize, imgSize);

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
	

	public void setPos(int Pos)
	{
		if(Pos>0)
		{
			yPos=Pos;
			
			if(!isVisible)
				startUpdateTimer();
			
			isVisible=true;
		}
		else
		{
			yPos=0;
			isVisible=false;
		}
		
		
		this.invalidate();
	}
	
	private void startUpdateTimer() 
	{
		handler.postDelayed(task,400);
	}
	
	Handler handler = new Handler();
	Runnable task = new Runnable() 
	{
		
		@Override
		public void run() 
		{
			String provider = LocationManager.GPS_PROVIDER;
			Location location = ((main) main.mainActivity).locationManager.getLastKnownLocation(provider);
			setNewLocation(location);
			
			//getAllSatellites();
			
			if(isVisible)
				handler.postDelayed(task,400);
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
		if (yPos>height*0.7)
		{
			yPos=height-CacheNameView.getMyHeight();
		}
		else
		{
			yPos=0;
			isVisible=false;
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
		
		this.invalidate();
		
	}

	
	private Boolean isVisible=false;
	
	private String mLatitude="";
	private String mLongitude="";
	private String mSats;
	private String mAccuracy;
	private String mAlt;
	private TextPaint GPSLayoutTextPaint;
	
	private StaticLayout GPSLayout;
	

	public void setNewLocation(Location location) 
	{
		
		if(this.width==0)return;
		
		
		
		mSats = String.valueOf(location.getExtras().getInt("satellites"));
		mAccuracy = String.valueOf(location.getAccuracy());
		mAlt = Global.Locator.getAltString();
		mLatitude = Global.FormatLatitudeDM(location.getLatitude());
		mLongitude = Global.FormatLongitudeDM(location.getLongitude());
		
		String br = StringFunctions.newLine();
		String Text= 
			Global.Translations.Get("current")+ " " + mLatitude + " " + mLongitude + br +
			Global.Translations.Get("alt") + " " + mAlt + "m" + br +
			Global.Translations.Get("accuracy") + "  +/- " + mAccuracy + "m" + br +
			Global.Translations.Get("sats") + " " + mSats ;
			
		
	
		
		if(GPSLayoutTextPaint==null)
		{
			GPSLayoutTextPaint = new TextPaint();
			GPSLayoutTextPaint.setTextSize(Global.scaledFontSize_normal);
			GPSLayoutTextPaint.setAntiAlias(true);
			GPSLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
		}
		
		int TextWidth = this.width- this.imgSize;
		GPSLayout = new StaticLayout(Text , GPSLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		GPSInfoHeight = (mLatitude.equals(""))? 0 : (LineSep * 4) + GPSLayout.getHeight();
		
		
		
		this.invalidate();
	}
	
	
	
	
	
	/*
	 * Wollte eigentlich mit dieser Funktion die Anzahl der Satellieten und deren Signalstärke
	 * in einem BalkenDiagramm darstellen. Mangels Funktions Umfang meines HD2 muss ich das auf später
	 * verschieben, bis ich ein richtiges Android Gerät habe.
	 * 
	 * Longri
	 */
	
	/*
	private int mNumSatellites;
	private static String satellites[] = new String[20];
	
	private void getAllSatellites() 
	{
		final GpsStatus gps_status = ((main) main.mainActivity).locationManager.getGpsStatus(null);
		
			final Iterator<GpsSatellite> sats = gps_status.getSatellites().iterator();
			this.mNumSatellites = 0;
			while (sats.hasNext()) 
			{
				GpsSatellite temp = sats.next();
				satellites[this.mNumSatellites]=Float.toString(temp.getSnr());
				this.mNumSatellites++;
			}
	}*/

	
	
}
