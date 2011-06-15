package de.droidcachebox;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.droidcachebox.Components.CacheNameView;

import de.droidcachebox.Components.copyAssetFolder;
import de.droidcachebox.Events.CachListChangedEventList;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.SelectedLangChangedEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.TranslationEngine.LangStrings;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.LogView;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.WaypointView;
import de.droidcachebox.Views.FilterSettings.EditFilterSettings;
import de.droidcachebox.Views.FilterSettings.PresetListView;
import de.droidcachebox.Views.Forms.SelectDB;
import de.droidcachebox.Database;
import de.droidcachebox.Database.DatabaseType;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class splash extends Activity 
{
	public static Activity mainActivity;

	ProgressBar myProgressBar;
	TextView myTextView;
	TextView versionTextView;
	TextView descTextView;
	Handler handler;
	Bitmap bitmap;
	Bitmap logo;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.splash);
		myProgressBar=(ProgressBar)findViewById(R.id.splash_progressbar);
		myTextView= (TextView)findViewById(R.id.splash_TextView);
		myTextView.setTextColor(Color.BLACK);
		versionTextView=(TextView)findViewById(R.id.splash_textViewVersion);
		versionTextView.setText(Global.getVersionString());
		descTextView=(TextView)findViewById(R.id.splash_textViewDesc);
		descTextView.setText(Global.splashMsg);
		mainActivity = this;
		
		LoadImages();
		
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				Initial();
			}
		};
		       
		Timer timer = new Timer();
		timer.schedule(task, 1000);
		
		
	 }


	@Override public void onDestroy() 
	{
		if (isFinishing())
		{
			ReleaseImages();
			versionTextView = null;
			myTextView = null;
			descTextView = null;
			mainActivity = null;
			
		}
		super.onDestroy();
	} 
	 
	 private void Initial() 
	 {
		 Global.AddLog("------" + Global.CurrentRevision + "-------");

// Read Config
		 
		 // copy AssetFolder
		 String[] exclude = new String[]{"webkit","sounds","images"};
		 copyAssetFolder myCopie = new copyAssetFolder();
		 myCopie.copyAll(getAssets(), Config.WorkPath, exclude);
		 
		 
		 Config.readConfigFile(getAssets());
		 try {
			Global.Translations.ReadTranslationsFile(Config.GetString("Sel_LanguagePath"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		setProgressState(20, Global.Translations.Get("IniUI"));
		 	Global.Paints.init(this);
	        Global.InitIcons(this, false);
	        
        setProgressState(40, Global.Translations.Get("LoadMapPack"));
	        File dir = new File(Config.GetString("MapPackFolder"));
	        String[] files = dir.list();
	        if (!(files == null))
	        {
		        if (files.length>0)
		        {
			        for (String file : files)
				        {
			        		if (Global.GetFileExtension(file).equalsIgnoreCase("pack"))
			        			MapView.Manager.LoadMapPack(Config.GetString("MapPackFolder") + "/" + file);
			        		if (Global.GetFileExtension(file).equalsIgnoreCase("map"))
			        		{
			        			Layer layer = new  Layer(file, file, "");
			        			layer.isMapsForge = true;
			        			MapView.Manager.Layers.add(layer);
			        		}
				        }
		        }
	        }
	    setProgressState(60, Global.Translations.Get("LoadCaches"));
	        if (Database.Data != null)
	        	Database.Data = null;

	        
	        double lat = Config.GetDouble("MapInitLatitude");
	        double lon = Config.GetDouble("MapInitLongitude");
	        if ((lat != -1000) && (lon != -1000))
	        	Global.LastValidPosition = new Coordinate(lat, lon);
	        
	        // search number of DB3 files
	        FileList fileList = null; 
	        try
	        {
	        	fileList = new FileList(Config.WorkPath, "DB3");
	        } catch (Exception ex)
	        {
	        	Global.AddLog(ex.getMessage());
	        }
			if ((fileList.size() > 1) && Config.GetBool("MultiDBAsk"))
			{
				// show Database Selection
	    		Intent selectDBIntent = new Intent().setClass(mainActivity, SelectDB.class);
	    		SelectDB.autoStart = true;
	    		//Bundle b = new Bundle();
			        //b.putSerializable("Waypoint", aktWaypoint);
			        //mainIntent.putExtras(b);
	    		mainActivity.startActivityForResult(selectDBIntent, 546132);
			} else
			{
				Initial2();
			}
	 }
	 
	 private void Initial2()
	 {
	        
		 	String FilterString = Config.GetString("Filter");
	        Global.LastFilter = (FilterString.length() == 0) ? new FilterProperties(PresetListView.presets[0]) : new FilterProperties(FilterString);
	        String sqlWhere =Global.LastFilter.getSqlWhere();
	        
	        
	        // initialize Database
	        Database.Data = new Database(DatabaseType.CacheBox, this);
	        String database = Config.GetString("DatabasePath");
	        Database.Data.StartUp(database);
//	        Database.Data.StartUp(Config.WorkPath + "/CacheBox.db3");
	        Database.Data.Query.LoadCaches(sqlWhere);

	        Database.FieldNotes = new Database(DatabaseType.FieldNotes, this); 
	        if (!Global.DirectoryExists(Config.WorkPath + "/User")) return;
	        Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");
	        
	        Descriptor.Init();
	        
	        
	        
            
	        
	        Config.AcceptChanges();
	        
	        // Initial Ready Show main
	        finish();
	        Intent mainIntent = new Intent().setClass(splash.this,main.class);
    		startActivity(mainIntent);
	 }
	 
	 

	 private void setProgressState(int progress, final String msg)
	 {
		 myProgressBar.setProgress(progress);
		 
				 
		 Thread t = new Thread() {
			    public void run() {
			        runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			            	myTextView.setText(msg);
			            }
			        });
			    }
			};

			t.start();

	 }

	    
	@Override protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {

	    	// SelectDB
	    	if (requestCode == 546132)
	    	{	    		
	    		if (resultCode == RESULT_CANCELED)
	    		{
	    			finish();
	    		} else
	    		{
		    		TimerTask task = new TimerTask()
		    		{
		    			@Override
		    			public void run()
		    			{
		    				Initial2();
		    			}
		    		};
		    		       
		    		Timer timer = new Timer();
		    		timer.schedule(task, 1000);
	    		}
	    	}
	    }
	 
	
	
	private void LoadImages()
	{
		Resources res = getResources();
		
		bitmap = BitmapFactory.decodeResource(res, R.drawable.splash_back);
		logo = BitmapFactory.decodeResource(res, R.drawable.cachebox_logo);
		((ImageView) findViewById(R.id.splash_BackImage)).setImageBitmap(bitmap);
		((ImageView) findViewById(R.id.splash_Logo)).setImageBitmap(logo);			
	}
	
	
	
	
	
	private void ReleaseImages()
	{
		((ImageView) findViewById(R.id.splash_BackImage)).setImageResource(0);
		((ImageView) findViewById(R.id.splash_Logo)).setImageResource(0);
		if (bitmap != null)
		{
			bitmap.recycle();
			bitmap = null;
		}
		if (logo != null)
		{
			logo.recycle();
			logo = null;
		}
	}
}
