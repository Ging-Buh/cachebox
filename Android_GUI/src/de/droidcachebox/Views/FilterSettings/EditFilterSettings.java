package de.droidcachebox.Views.FilterSettings;

import CB_Core.Log.Logger;
import CB_Core.Config;
import de.droidcachebox.Database;
import CB_Core.FilterProperties;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.DAO.CacheListDAO;
import CB_Core.Events.CachListChangedEventList;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.Toast;
import android.app.ProgressDialog;

public class EditFilterSettings extends Activity {

	private MultiToggleButton btPre;
	private MultiToggleButton btSet;
	private MultiToggleButton btCat;
	private TableRow trPre;
	private TableRow trSet;
	private TableRow trCat;
	private PresetListView lvPre;
	private FilterSetListView lvSet;
	private CategorieListView lvCat;
	public static FilterProperties tmpFilterProps;
	public static Activity filterActivity;
	
	public void onCreate(Bundle savedInstanceState) 
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_filter);
		
		filterActivity=this;
		tmpFilterProps=Global.LastFilter;
		
		btPre = (MultiToggleButton) findViewById(R.id.edfi_pre);
		btSet = (MultiToggleButton) findViewById(R.id.edfi_set);
		btCat = (MultiToggleButton) findViewById(R.id.edfi_cat);
		// Translate 
		/*btPre.setText(Global.Translations.Get("filterPreset"));
		btSet.setText(Global.Translations.Get("filterSet"));
		btCat.setText(Global.Translations.Get("category"));*/
		btPre.setText("Preset");
		btSet.setText("Setting");
		btCat.setText("Category");
		MultiToggleButton.initialOn_Off_ToggleStates(btPre);
		MultiToggleButton.initialOn_Off_ToggleStates(btSet);
		MultiToggleButton.initialOn_Off_ToggleStates(btCat);
		
		trPre = (TableRow) findViewById(R.id.edfi_table_pre);
		trSet = (TableRow) findViewById(R.id.edfi_table_set);
		trCat = (TableRow) findViewById(R.id.edfi_table_cat);
		
		switchVisibility(0);
		
		initialPresets();
		initialSettings();
		initialCategorieView();
		fillListViews();
		
		btPre.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				switchVisibility(0);
			}
		});
		
		btSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				switchVisibility(1);
			}
		});
		
		btCat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) 
			{
				switchVisibility(2);
			}
		});
		
        Button bOK = (Button) findViewById(R.id.edfi_ok);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	lvCat.SetCategory();
            	Global.LastFilter=tmpFilterProps;
            	ApplyFilter(Global.LastFilter);
            	
            	
            	//Save selected filter
            	Config.Set("Filter", Global.LastFilter.ToString());
                Config.AcceptChanges();
            	
            }
          });
        Button bCancel = (Button) findViewById(R.id.edfi_cancel);
        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	finish();	            	
            }
          });
		
		 // Translations
        bOK.setText(Global.Translations.Get("ok"));
        bCancel.setText(Global.Translations.Get("cancel"));
	}
	
	
	
	
	private void initialPresets()
	{
		lvPre = new PresetListView(this, this);
		FrameLayout presetLayout = (FrameLayout) findViewById(R.id.layout_filter_presets);
		presetLayout.removeAllViews();
		presetLayout.addView(lvPre);
	}
	
	private void initialSettings()
	{
		lvSet = new FilterSetListView(this, this);
		FrameLayout filterSetLayout = (FrameLayout) findViewById(R.id.layout_filter_setting);
		filterSetLayout.removeAllViews();
		filterSetLayout.addView(lvSet);
		
	}
	
	private void initialCategorieView()
	{
		lvCat = new CategorieListView(this, this);
		FrameLayout categorieLayout = (FrameLayout) findViewById(R.id.layout_filter_categories);
		categorieLayout.removeAllViews();
		categorieLayout.addView(lvCat);
	}
	
	private void fillListViews()
	{
	}
	
	private void switchVisibility()
	{
		if(btPre.getState()==1)
		{
			trSet.setVisibility(View.GONE);
			trPre.setVisibility(View.VISIBLE);
			trCat.setVisibility(View.GONE);if(lvCat!=null)lvCat.SetCategory();
			
		}
		
		if(btSet.getState()==1)
		{
			trPre.setVisibility(View.GONE);
			trSet.setVisibility(View.VISIBLE);
			trCat.setVisibility(View.GONE);if(lvCat!=null)lvCat.SetCategory();
			lvSet.OnShow();
		}
		if(btCat.getState()==1)
		{
			trPre.setVisibility(View.GONE);
			trSet.setVisibility(View.GONE);
			trCat.setVisibility(View.VISIBLE);
			lvCat.OnShow();
		}
	}
	private void switchVisibility(int state)
	{
		if(state==0)
		{
			btPre.setState(1);
			btSet.setState(0);
			btCat.setState(0);
		}
		if(state==1)
		{
			btPre.setState(0);
			btSet.setState(1);
			btCat.setState(0);
		}
		if(state==2)
		{
			btPre.setState(0);
			btSet.setState(0);
			btCat.setState(1);
		}
		
		switchVisibility();
	}
	
		
	private static ProgressDialog pd;
	private static FilterProperties props;
	public static void ApplyFilter(FilterProperties Props)
	    {
		
		
			
			props = Props;
		  pd = ProgressDialog.show(EditFilterSettings.filterActivity, "", 
                 Global.Translations.Get("LoadCaches"), true);
		
		  Thread thread = new Thread()
		  {
		      @Override
		      public void run() 
		      {
		    	  String sqlWhere =props.getSqlWhere();
			      Logger.General("Main.ApplyFilter: " + sqlWhere);
			      Database.Data.Query.clear();
			      CacheListDAO cacheListDAO = new CacheListDAO();
			      cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
			      messageHandler.sendMessage(messageHandler.obtainMessage(1));
		      }

		  };

		  thread.start();

	
	    }
	
	// Instantiating the Handler associated with the main thread.
	  private static Handler messageHandler = new Handler() {

	      @Override
	      public void handleMessage(Message msg) 
	      {
	    	  switch(msg.what) 
	    	  {
	    	  case 1:
	    	  	{
	    	  		CachListChangedEventList.Call();
	    	  		pd.dismiss();
	    	  		Toast.makeText(main.mainActivity, "Applay filter. Found " + String.valueOf(Database.Data.Query.size()) + " Caches!", Toast.LENGTH_LONG).show();
	    	  		EditFilterSettings.filterActivity.finish();	
	    	  	}
		 	     
	    	  }
	    	  
	      }

	  };
	
}
