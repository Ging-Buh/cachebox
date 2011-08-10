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


import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Custom_Controls.QuickButtonList.HorizontalListView;
import de.droidcachebox.Ui.Sizes;
import CB_Core.Config;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
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
import android.view.MotionEvent;
import android.view.View;


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
		this.setOnTouchListener(myTouchListner);
				
		Me=this;
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
	private int QuickButtonHeight;
	private int QuickButtonMaxHeight;
	
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
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) 
    {
    	QuickButtonMaxHeight=Sizes.getQuickButtonListHeight();
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
    public static boolean isInitial=false;
	private boolean drag;
	
	private OnTouchListener myTouchListner = new OnTouchListener() 
	{
		
		@Override public boolean onTouch(View v, MotionEvent event) 
		{
			 // events when touching the screen

			 int eventaction = event.getAction();
			 int X = (int)event.getX();
			 int Y = (int)event.getY();
			  if(contains(X, Y)) drag=true;
			 
			 
			 switch (eventaction ) 
			 {
			 	case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on a ball
//			 		setDebugMsg("Down");
			 		break;


			 	case MotionEvent.ACTION_MOVE: // touch drag with the ball
				 // move the balls the same as the finger
			
//			 		setDebugMsg("Move:" + String.format("%n")+ "x= " + X + String.format("%n") + "y= " + Y);
			 		if (drag)setPos(Y-25); //y - 25 minus halbe Button H�he
			 		break;
			 		
			 	case MotionEvent.ACTION_UP:
			 		if (drag)ActionUp();
			 		drag=false;
			 		break;

			 }
			
			if(drag)
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
		
		/**
		 * Beim ersten Zeichnen, wird der Letzte Zustand abgefragt!
		 */
		 if(!isInitial)
	        {
	        	if(Config.GetBool("quickButtonShow") && Config.GetBool("quickButtonLastShow"))
	    		{
	    			setPos(QuickButtonMaxHeight);
	    		}
	    		else
	    		{
	    			setPos(0);
	    		}
	        	isInitial=true;
	        }
		
		
		if(!drag)
		{
			yPos=QuickButtonHeight=Config.GetBool("quickButtonShow")? main.getQuickButtonHeight():0;
		}
		
		int FSize = (int) (Sizes.getScaledFontSize_normal()*1.2);
	
		if(CacheInfoHeight==0)CacheInfoHeight = (int) (FSize * 5.5);
		
		if (paint==null)
		{
			paint = new Paint();
			paint.setColor(Global.getColor(R.attr.TextColor));
			paint.setTextSize(FSize);
			paint.setAntiAlias(true);
		}
		
		final Drawable Slide = Global.BtnIcons[0];
		
		mBtnRec.set(-10, yPos - 2, width+10 , (int) (yPos + 2 + Sizes.getScaledFontSize_normal()*2.2));
		Slide.setBounds(mBtnRec);
   	  	Slide.draw(canvas);
		
   	 	
   	 	// Draw Background
   	 	if (backPaint==null)
   	 	{
   	 		backPaint= new Paint();
   	 		backPaint.setColor(Global.getColor(R.attr.SlideDownBackColor));
   	 		//backPaint.setColor(Color.RED); //DEBUG RED
   	 	}
   	 	mBackRec.set(-10, QuickButtonHeight+2, width+10 , yPos+1);
   	 	canvas.drawRect(mBackRec, backPaint);
  
   	 	if (mCache == null)
			return;

   	 	// draw Cache Name
		canvas.drawText(mCache.Name,5,yPos + (FSize + (FSize/3)), paint);
		
		
		
		//Draw only is visible
		if(Config.GetBool("quickButtonShow"))
		{
			if(yPos<=QuickButtonMaxHeight)
				return;
		}
		else
		{
			if(yPos<=1)
				return;
		}
		
		
		if(Config.GetBool("quickButtonShow"))
		{
			canvas.clipRect(mBackRec);
		}
		
	 	 

		// draw GPS Info
		int versatz = -yPos+GPSInfoHeight;
		canvas.translate(0,-versatz);
		drawGPSInfo(canvas);
		canvas.translate(0,+versatz);
//		canvas.clipRect(mBackRec);
   	 	
//		if(yPos>1)return;
   	 	// draw WP Info
   	 	versatz += WPInfoHeight;
   	 	canvas.translate(0,-versatz);
   	 	Boolean WPisDraw = drawWPInfo(canvas);
		canvas.translate(0,+versatz);
//		canvas.clipRect(mBackRec);
   	 
   	 	// draw Cache Info
		versatz += CacheInfoHeight;
		canvas.translate(5,-versatz);
   	 	CacheDraw.DrawInfo(mCache,canvas, width - 10, CacheInfoHeight, WPisDraw? Global.getColor(R.attr.ListBackground) : Global.getColor(R.attr.ListBackground_select), DrawStyle.withOwnerAndName);
   	 	canvas.translate(0,+versatz);
   	 	//canvas.clipRect(mBackRec);
	}

	
	
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
			iconWidth=ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[(int)mWaypoint.Type.ordinal()], Sizes.getHalfCornerSize(),Sizes.getCornerSize(), imgSize);

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
		 iconWidth=ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[30], Sizes.getHalfCornerSize(),Sizes.getCornerSize(), imgSize);

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
		if(downSlider.Me != null)
		{
			if(Config.GetBool("quickButtonShow"))
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
		if(Pos>=0)
		{
			yPos=Pos;
			if(Config.GetBool("quickButtonShow"))
			{
				if(Pos<=QuickButtonMaxHeight)
				{
					QuickButtonHeight=Pos;
					((main)main.mainActivity).setTopButtonHeight(Pos);
				}
				else
				{
					QuickButtonHeight=QuickButtonMaxHeight;
					((main)main.mainActivity).setTopButtonHeight(QuickButtonMaxHeight);
				}
			}
			else
			{
				QuickButtonHeight=0;
				((main)main.mainActivity).setTopButtonHeight(0);
			}
			
			
		}
		else
		{
			yPos=0;
			
		}
		
		
		//chk if info Visible then update info
		int InfoBeginnAt = Config.GetBool("quickButtonShow")? QuickButtonMaxHeight:0;
		if(yPos>InfoBeginnAt)
		{
			if(!isVisible)
				startUpdateTimer();
			isVisible=true;
		}
		else
		{
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
			Location location = Global.Locator.getLocation();
//			Location location = ((main) main.mainActivity).locationManager.getLastKnownLocation(provider);
			
			if(location!=null)
			{
				setNewLocation(location);
				
				//getAllSatellites();
				
				if(isVisible)
					handler.postDelayed(task,1200);
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
	
	public void ActionUp() // Slider zur�ck scrolllen lassen
	{
		boolean QuickButtonShow = Config.GetBool("quickButtonShow");
		
		//check if QuickButtonList snap in
		if(yPos>=(QuickButtonMaxHeight*0.5) && QuickButtonShow)
		{
			QuickButtonHeight=QuickButtonMaxHeight;
			Config.Set("quickButtonLastShow",true);
			Config.AcceptChanges();
		}
		else
		{
			QuickButtonHeight=0;
			Config.Set("quickButtonLastShow",false);
			Config.AcceptChanges();
		}
		
		((main)main.mainActivity).setTopButtonHeight(QuickButtonHeight);
		
		if (yPos>height*0.7)
		{
			yPos=(int) (height-(Sizes.getScaledFontSize_normal()*2.2));
		}
		else
		{
			yPos=QuickButtonShow? QuickButtonHeight:0;
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
		WPLayoutTextPaint.setTextSize(Sizes.getScaledFontSize_normal());
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
		
		String br = String.format("%n");
		String Text= 
			Global.Translations.Get("current")+ " " + mLatitude + " " + mLongitude + br +
			Global.Translations.Get("alt") + " " + mAlt + "m" + br +
			Global.Translations.Get("accuracy") + "  +/- " + mAccuracy + "m" + br +
			Global.Translations.Get("sats") + " " + mSats ;
			
		
	
		
		if(GPSLayoutTextPaint==null)
		{
			GPSLayoutTextPaint = new TextPaint();
			GPSLayoutTextPaint.setTextSize(Sizes.getScaledFontSize_normal());
			GPSLayoutTextPaint.setAntiAlias(true);
			GPSLayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
		}
		
		int TextWidth = this.width- this.imgSize;
		GPSLayout = new StaticLayout(Text , GPSLayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		GPSInfoHeight = (mLatitude.equals(""))? 0 : (LineSep * 4) + GPSLayout.getHeight();
		
		
		
		this.invalidate();
	}
	
	
	
	
	
	/*
	 * Wollte eigentlich mit dieser Funktion die Anzahl der Satellieten und deren Signalst�rke
	 * in einem BalkenDiagramm darstellen. Mangels Funktions Umfang meines HD2 muss ich das auf sp�ter
	 * verschieben, bis ich ein richtiges Android Ger�t habe.
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
