package de.droidcachebox.Components;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Ui.Size;
import de.droidcachebox.Ui.Sizes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Enums.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.LogEntry;

import CB_Core.Types.Waypoint;


public class CacheDraw 
{

	//Draw Metods
    public enum DrawStyle
    {
    	all,		// alle infos
    	withoutBearing,	//ohne Richtungs-Pfeil
    	withoutSeparator, //ohne unterster trennLinie
    	withOwner, //mit Owner statt Name
    	withOwnerAndName; // mit Owner und Name
    };
    
    private static StaticLayout layoutCacheName;
    private static StaticLayout layoutCacheOwner;
	
	public String shortDescription;
	
	public String longDescription;
    
    public static void DrawInfo(Cache cache, Canvas canvas, int width,int height, int BackgroundColor, DrawStyle drawStyle)
    {
      	int x=0;
      	int y=0;
      	Rect DrawRect = new Rect(x,y,width,height);
      	DrawInfo(cache, canvas, DrawRect, BackgroundColor,  drawStyle);
    }
    
    
    // Static Mesured Member
   
    private static int VoteWidth = 0;
    private static int rightBorder = 0;
    private static int nameLayoutWidth = 0;
    private static Paint DTPaint;
    public static Rect BearingRec; 
    private static TextPaint namePaint;
    
    // Die Cached Bmp wird nur zur Darstellung als Bubble in der 
    // MapView benötigt.
    private static Bitmap CachedBitmap;
    private static long CachedBitmapId = -1;
    private static Paint CachedBitmapPaitnt;
    
    
    public  static void ReleaseCacheBMP()
    {
    	CachedBitmap.recycle();
    	CachedBitmap=null;
    	CachedBitmapId=-1;
    }
    
    public static void DrawInfo(Cache cache, Canvas canvas,Rect rec, int BackgroundColor, DrawStyle drawStyle,float scale)
    {
    	if (CachedBitmap==null || !(CachedBitmapId== cache.Id))
    	{
    		CachedBitmap = Bitmap.createBitmap(rec.width(), rec.height(), Bitmap.Config.ARGB_8888);
        	Rect newRec= new Rect(0,0,rec.width(),rec.height());
        	Canvas scaledCanvas = new Canvas(CachedBitmap);
        	scaledCanvas.drawColor(Color.TRANSPARENT);
        	DrawInfo(cache, scaledCanvas,newRec, BackgroundColor, Color.RED, drawStyle);
        	CachedBitmapId=cache.Id;
    	}
    	    	
    	if(CachedBitmapPaitnt==null)
    	{
    		CachedBitmapPaitnt = new Paint();
    		CachedBitmapPaitnt.setAntiAlias(true);
    		CachedBitmapPaitnt.setFilterBitmap(true);
    		CachedBitmapPaitnt.setDither(true);
    	}
    	

    	    	
    	canvas.save();
    	canvas.scale(scale, scale);
    	canvas.drawBitmap(CachedBitmap, rec.left/scale, rec.top/scale, CachedBitmapPaitnt);
    	//canvas.drawBitmap(newBmp, newRec, rec, paint);
    	canvas.restore();
    	
    }
    
    public static void DrawInfo(Cache cache, Canvas canvas,Rect rec, int BackgroundColor, DrawStyle drawStyle)
    {
    	DrawInfo(cache, canvas,rec, BackgroundColor,-1, drawStyle);
    }
    
    public static void DrawInfo(Cache cache, Canvas canvas,Rect rec, int BackgroundColor,int BorderColor, DrawStyle drawStyle)
    {
    	// init
    		Boolean notAvailable = (!cache.Available && !cache.Archived);
            Boolean Night = Config.GetBool("nightMode");
            Boolean GlobalSelected = cache == GlobalCore.SelectedCache();
            if(BackgroundColor==-1)BackgroundColor= GlobalSelected? Global.getColor(R.attr.ListBackground_select):Global.getColor(R.attr.ListBackground);
            if(BorderColor==-1)BorderColor= Global.getColor(R.attr.ListSeparator);
            
            final int left = rec.left + Sizes.getHalfCornerSize();
            final int top = rec.top +  Sizes.getHalfCornerSize();
            final int width = rec.width() -  Sizes.getHalfCornerSize();
            final int height = rec.height() -  Sizes.getHalfCornerSize();
            final int SDTImageTop = (int) (height-(Sizes.getScaledFontSize_normal()/1.5))+ rec.top-10;
            final int SDTLineTop = SDTImageTop + Sizes.getScaledFontSize_normal();
            	
    	// Mesure
    		if (VoteWidth == 0) // Grössen noch nicht berechnet
    		{
		    	
		      	VoteWidth = Sizes.getScaledFontSize_normal()/2;
		      	
		      	rightBorder = (int) (height * 0.7);
		      	nameLayoutWidth = width - VoteWidth - Sizes.getIconSize() - rightBorder - (Sizes.getScaledFontSize_normal()/2);
		      	DTPaint = new Paint();
	    	    DTPaint.setTextSize(Sizes.getScaledFontSize_normal());
	    	    DTPaint.setAntiAlias(true);
    		}
    		if (namePaint==null)
    		{
    			namePaint = new TextPaint();
    			namePaint.setTextSize(Sizes.getScaledFontSize_normal());
    		}
    		
    	// reset namePaint attr
    		namePaint.setColor(Global.getColor(R.attr.TextColor));
    		namePaint.setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
			namePaint.setAntiAlias(true);
    		
    		DTPaint.setColor(Global.getColor(R.attr.TextColor));
    		
    	// Draw roundetRect
    		ActivityUtils.drawFillRoundRecWithBorder(canvas, rec, 2, BorderColor, BackgroundColor);
    		
    	// Draw Vote
    		if (cache.Rating > 0)
    			ActivityUtils.PutImageScale(canvas, Global.StarIcons[(int)(cache.Rating * 2)],-90,left, top , (double)Sizes.getScaledFontSize_normal()/60);
    	
    	// Draw Icon
    		 if (cache.MysterySolved())
    	        {
    			 	ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[19],left + VoteWidth, top - (int) (Sizes.getScaledFontSize_normal() / 2) , Sizes.getIconSize()); 
    	        }
    	        else
    	        {
    	        	ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[cache.Type.ordinal()], left  + VoteWidth, top - (int) (Sizes.getScaledFontSize_normal() / 2) , Sizes.getIconSize()); 
    	        }
    	
    	// Draw Cache Name
    		   
    	     if(notAvailable)
    	     {
    	    	 namePaint.setColor(Color.RED);
    	    	 namePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
    	    	 DTPaint.setAntiAlias(true);
    	     }
    	     
    	     SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy"); 
		     String dateString = postFormater.format(cache.DateHidden); 
    	     
    	     String drawName = (drawStyle==DrawStyle.withOwner)? "by " + cache.Owner + ", "+ dateString:cache.Name;
    	     
    	     layoutCacheName = new StaticLayout(drawName, namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    	     int LayoutHeight = ActivityUtils.drawStaticLayout(canvas, layoutCacheName, left + VoteWidth + Sizes.getIconSize() + 5, top);
    	       
    	// over draw 3. Cache name line
    	     int VislinesHeight = LayoutHeight*2/layoutCacheName.getLineCount();
    	     if(layoutCacheName.getLineCount()>2)
    	     {
    	    	 Paint backPaint = new Paint();
    	    	 backPaint.setColor(BackgroundColor); // Color.RED

    	    	 canvas.drawRect(new Rect(left + VoteWidth + Sizes.getIconSize() + 5,top + VislinesHeight,nameLayoutWidth+left + VoteWidth + Sizes.getIconSize() + 5,top+LayoutHeight+VislinesHeight-4), backPaint);
    	     }
    	     
    	     
    	// Draw owner and Last Found
    	     if (drawStyle==DrawStyle.withOwnerAndName)
    	     {
    	    	 String DrawText="by " + cache.Owner + ", "+ dateString;
    	    	 String LastFound = getLastFoundLogDate(cache);
    	    	 if(!LastFound.equals(""))
    	    	 {
    	    		 DrawText += String.format("%n");
    	    		 DrawText += "last found: " + LastFound;
    	    	 }
    	    	 layoutCacheOwner = new StaticLayout(DrawText , namePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    	    	 ActivityUtils.drawStaticLayout(canvas, layoutCacheOwner, left + VoteWidth + Sizes.getIconSize() + 5, top + VislinesHeight);
    	     }
    	     
    	// Draw S/D/T
    	     int SDTleft = left+ 2;
    	     
    	     canvas.drawText("S",SDTleft,SDTLineTop , DTPaint);
    	     SDTleft += Sizes.getSpaceWidth();
    	     SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.SizeIcons[(int)(cache.Size.ordinal())], SDTleft, SDTImageTop, Sizes.getScaledFontSize_normal());
    	     SDTleft += Sizes.getTabWidth();	
    	     canvas.drawText("D",SDTleft,SDTLineTop , DTPaint);
    	     SDTleft += Sizes.getSpaceWidth();
    	     SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int)(cache.Difficulty * 2)],SDTleft, SDTImageTop, Sizes.getScaledFontSize_normal());
    	     SDTleft += Sizes.getTabWidth();
    	     canvas.drawText("T", SDTleft,SDTLineTop , DTPaint);
    	     SDTleft += Sizes.getSpaceWidth();
    	     SDTleft += ActivityUtils.PutImageTargetHeight(canvas, Global.StarIcons[(int)(cache.Terrain * 2)], SDTleft, SDTImageTop, Sizes.getScaledFontSize_normal());
    	     SDTleft += Sizes.getTabWidth();
    	     
    	
    	     
    	// Draw TB
    	     int numTb = cache.NumTravelbugs;
             if (numTb > 0)
              {
            	 SDTleft += ActivityUtils.PutImageScale(canvas, Global.Icons[0],-90,SDTleft, SDTImageTop -1 , (double)Sizes.getScaledFontSize_normal()/30);
                  //SDTleft += space;
                 if (numTb > 1)
                	 canvas.drawText("x" + String.valueOf(numTb),SDTleft, SDTLineTop , DTPaint);
              }
    	
             
        // Draw Bearing
    	     
    	     if (drawStyle != DrawStyle.withoutBearing && drawStyle != DrawStyle.withOwnerAndName)
    	     {
    	    	 if (BearingRec==null) BearingRec = new Rect(rec.right-rightBorder,rec.top,rec.right,(int) (SDTImageTop*0.9));
    	    	 DrawBearing(cache,canvas,BearingRec);
    	     }
    	    	
    	
    	  if (cache.Found)
          {
        	  
    		  ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[2],left  + VoteWidth+Sizes.getIconSize()/2,top - (int) (Sizes.getScaledFontSize_normal() / 2)+Sizes.getIconSize()/2, Sizes.getIconSize()/2);//Smile
          }
              

          if (cache.Favorit())
          {
        	  ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[19],left + VoteWidth + 2, top , Sizes.getIconSize()/2);
          }

         

          if (cache.Archived)
          {
        	  ActivityUtils.PutImageTargetHeight(canvas, Global.Icons[24], left + VoteWidth, top - (int) (Sizes.getScaledFontSize_normal() / 2), Sizes.getIconSize()/2);
          }

          String gcLogin = Config.GetString("GcLogin").toLowerCase(); 
          if (cache.Owner.toLowerCase().equals(gcLogin) && !(gcLogin.equals("")))
          {
        	  ActivityUtils.PutImageTargetHeight(canvas,Global.Icons[17],left  + VoteWidth+Sizes.getIconSize()/2,top - (int) (Sizes.getScaledFontSize_normal() / 2)+Sizes.getIconSize()/2, Sizes.getIconSize()/2);
          }
       	
    }
   
    
    
   
    private static void DrawBearing(Cache cache, Canvas canvas,Rect drawingRec)
    {
    	
    	if (GlobalCore.LastValidPosition.Valid || GlobalCore.Marker.Valid)
        {
    		    Coordinate position = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
	            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
	            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude(), cache.Longitude());
	            double cacheBearing = bearing - heading;
	            String cacheDistance=UnitFormatter.DistanceString(cache.Distance(false));
	            DrawBearing(cache,canvas,drawingRec,cacheDistance,cacheBearing);   
			
       }
    }
    
    
    
	public void DrawBearing(Cache cache,Canvas canvas,Rect drawingRec, Waypoint waypoint)
    {
    	if (GlobalCore.LastValidPosition.Valid || GlobalCore.Marker.Valid)
        {
    		    Coordinate position = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
	            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
	            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, waypoint.Latitude(), waypoint.Longitude());
	            double waypointBearing = bearing - heading;
	            float distance = 0;
	            if (waypoint == null)
	            	distance = cache.Distance(false);
	            else
	            	distance = waypoint.Distance();
	            String waypointDistance=UnitFormatter.DistanceString(distance);
	            DrawBearing(cache,canvas,drawingRec,waypointDistance,waypointBearing);
			   
    		
    		
       }
    }
    
    
    private static void DrawBearing (Cache cache,Canvas canvas,Rect drawingRec, String Distance, double Bearing)
    {
    	
    	double scale =(double) Sizes.getScaledFontSize_normal() / 30;
    	
    	ActivityUtils.PutImageScale(canvas, Global.Arrows[1],Bearing,drawingRec.left,drawingRec.top,scale);
	    canvas.drawText(Distance,drawingRec.left,drawingRec.bottom, DTPaint);
	       
    }

    
    private static String getLastFoundLogDate(Cache cache)
    {
    	String FoundDate="";
    	ArrayList<LogEntry> logs = new ArrayList<LogEntry>();
        logs = Database.Logs(cache);// cache.Logs();
        for (LogEntry l : logs)
        {
        	if(l.TypeIcon==0)//Found Icon
        	{
        		SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy"); 
        		FoundDate = postFormater.format(l.Timestamp);
        		break;
        	}
        }
        return FoundDate;
    }
	
	  public static void ReloadSpoilerRessources(final Cache cache)
	    {
		  cache.spoilerRessources = new ArrayList<String>();

	        String path = Config.GetString("SpoilerFolder");
	        String directory = path + "/" + cache.GcCode.substring(0, 4);

	        if (!FileIO.DirectoryExists(directory))
	            return;

	        
	        File dir = new File(directory);
	        FilenameFilter filter = new FilenameFilter() {			
				@Override
				public boolean accept(File dir, String filename) {
					
					filename = filename.toLowerCase();
					if (filename.indexOf(cache.GcCode.toLowerCase()) == 0)
					{
			            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif"))
			            	return true;
					}
					return false;
				}
			};
	        String[] files = dir.list(filter);

	        for (String image : files)
	        {
	        	cache.spoilerRessources.add(directory + "/" + image);
	        }

	        // Add own taken photo
	        directory = Config.GetString("UserImageFolder");

	        if (!FileIO.DirectoryExists(directory))
	            return;

	        dir = new File(directory);
	        filter = new FilenameFilter() {			
				@Override
				public boolean accept(File dir, String filename) {
					
					filename = filename.toLowerCase();
					if (filename.indexOf(cache.GcCode.toLowerCase()) >= 0)
						return true;
					return false;
				}
			};
	        files = dir.list(filter);
	        if (!(files == null))
	        {
		        if (files.length>0)
		        {
			        for (String file : files)
				        {
			        		String ext = FileIO.GetFileExtension(file);
			        		if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif"))
			        		{
			        			cache.spoilerRessources.add(directory + "/" + file);
			        		}
				        }
		        }
	        }
	    }

	

    
}
