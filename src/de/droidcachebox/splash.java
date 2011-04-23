package de.droidcachebox;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.Components.CacheNameView;
import de.droidcachebox.Components.ClockView;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.TranslationEngine.LangStrings;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.DescriptionView;
import de.droidcachebox.Views.LogView;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.WaypointView;
import de.droidcachebox.Database;
import de.droidcachebox.Database.DatabaseType;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


public class splash extends Activity 
{
	 public void onCreate(Bundle savedInstanceState) 
	 {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.splash);
	        myProgressBar=(ProgressBar)findViewById(R.id.splash_progressbar);
	        myTextView= (TextView)findViewById(R.id.splash_TextView);
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
	 
	 private void Initial() 
	 {
		 
// Read Config
		 
		 Config.readConfigFile(getAssets());
		 
		 
		setProgressState(20, "Ini UI ...");
		 	Global.Paints.init();
	        Global.InitIcons(this);
	        
        setProgressState(40, "Load Map ...");
	        File dir = new File(Config.GetString("MapPackFolder"));
	        String[] files = dir.list();
	        if (files.length>0)
	        {
	        for (String file : files)
		        {
		        	MapView.Manager.LoadMapPack(Config.GetString("MapPackFolder") + "/" + file);
		        }
	        }
	        
	    setProgressState(60, "Load Caches ...");
	        if (Database.Data != null)
	        	Database.Data = null;
	        // initialize Database
	        Database.Data = new Database(DatabaseType.CacheBox);
	        Database.FieldNotes = new Database(DatabaseType.FieldNotes); 
//		        Database.Data.StartUp("/sdcard/db3 [1].db3");
	        File path = Environment.getExternalStorageDirectory();
	        Database.Data.StartUp(path.getPath() + "/cachebox.db3");
	        Database.Data.Query.LoadCaches("");

	        Descriptor.Init();
	        
	        Config.AcceptChanges();
	        
	        // Initial Ready Show main
	        finish();
	        Intent mainIntent = new Intent().setClass(splash.this,main.class);
    		startActivity(mainIntent);
	 }
	 
	 ProgressBar myProgressBar;
	 TextView myTextView;
	 private void setProgressState(int progress, final String msg)
	 {
		 myProgressBar.setProgress(progress);
		 
		 
		 // TODO Set msg
		 // new Thread(new Runnable() 
		 // {
		 //	    public void run() 
		 //	    {
		 //	    	myTextView.setText(msg);
		 //	    };
		 //}).start();

		 
		 
		 
	 }

}
