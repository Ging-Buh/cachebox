package de.droidcachebox;


import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Categories;
import CB_Core.Types.Coordinate;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.droidcachebox.DAO.CacheListDAO;
import de.droidcachebox.Database.DatabaseType;
import de.droidcachebox.Components.copyAssetFolder;

import de.droidcachebox.Map.Descriptor;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.MapView;
import de.droidcachebox.Views.FilterSettings.PresetListView;
import de.droidcachebox.Views.Forms.SelectDB;


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
	Bitmap gc_power_logo;
	
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
		 Logger.setDebug(Global.Debug);
		
		 // Read Config
		 String workPath = Environment.getExternalStorageDirectory() + "/cachebox";
		 Config.Initialize(workPath, workPath + "/cachebox.config");
		 
		 Config.readConfigFile(/*getAssets()*/);
		 
		 // copy AssetFolder only if Rev-Number changed, like at new installation
		 if(Config.GetInt("installRev")<Global.CurrentRevision+1)
		 {
//			 String[] exclude = new String[]{"webkit","sounds","images"};
			 copyAssetFolder myCopie = new copyAssetFolder();
			 myCopie.copyAll(getAssets(), Config.WorkPath);
			 Config.Set("installRev", Global.CurrentRevision);
			 Config.Set("newInstall", true);
			 Config.AcceptChanges();
		 }
		 else
		 {
			 Config.Set("newInstall", false);
			 Config.AcceptChanges();
		 }
		
		
		 
		 
		 try {
			Global.Translations.ReadTranslationsFile(Config.GetString("Sel_LanguagePath"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		setProgressState(20, Global.Translations.Get("IniUI"));
		 	Sizes.initial(false, this);
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
			        		if (FileIO.GetFileExtension(file).equalsIgnoreCase("pack"))
			        			MapView.Manager.LoadMapPack(Config.GetString("MapPackFolder") + "/" + file);
			        		if (FileIO.GetFileExtension(file).equalsIgnoreCase("map"))
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
	        {
	        	GlobalCore.LastValidPosition = new Coordinate(lat, lon);
	        }
	        
	        // search number of DB3 files
	        FileList fileList = null; 
	        try
	        {
	        	fileList = new FileList(Config.WorkPath, "DB3");
	        } catch (Exception ex)
	        {
	        	Logger.Error("slpash.Initial()", "search number of DB3 files", ex);
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
		    setProgressState(62, Global.Translations.Get("LoadCaches")+ FileIO.GetFileName(Config.GetString("DatabasePath")));
		 	String FilterString = Config.GetString("Filter");
	        Global.LastFilter = (FilterString.length() == 0) ? new FilterProperties(PresetListView.presets[0]) : new FilterProperties(FilterString);
	        String sqlWhere =Global.LastFilter.getSqlWhere();
	        
	        
	        // initialize Database
	        Database.Data = new Database(DatabaseType.CacheBox, this);
	        String database = Config.GetString("DatabasePath");
	        Database.Data.StartUp(database);

            GlobalCore.Categories = new Categories();
            Database.Data.GPXFilenameUpdateCacheCount();

	        CacheListDAO cacheListDAO = new CacheListDAO();
	        cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

	        
	        Database.FieldNotes = new Database(DatabaseType.FieldNotes, this); 
	        if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
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
		gc_power_logo= BitmapFactory.decodeResource(res, R.drawable.power_gc_live);
		((ImageView) findViewById(R.id.splash_BackImage)).setImageBitmap(bitmap);
		((ImageView) findViewById(R.id.splash_Logo)).setImageBitmap(logo);
		((ImageView) findViewById(R.id.splash_GcPowerLogo)).setImageBitmap(gc_power_logo);
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
