package de.droidcachebox.Views.FilterSettings;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.FilterProperties;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import de.droidcachebox.Events.CachListChangedEventList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.ProgressDialog;

public class EditFilterSettings extends Activity {

	private MultiToggleButton btPre;
	private MultiToggleButton btSet;
	private TableRow trPre;
	private TableRow trSet;
	private PresetListView lvPre;
	private FilterSetListView lvSet;
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
		// Translate 
		/*btPre.setText(Global.Translations.Get("filterPreset"));
		btSet.setText(Global.Translations.Get("filterSet"));*/
		btPre.setText("Preset");
		btSet.setText("Setting");
		MultiToggleButton.initialOn_Off_ToggleStates(btPre);
		MultiToggleButton.initialOn_Off_ToggleStates(btSet);
		
		trPre = (TableRow) findViewById(R.id.edfi_table_pre);
		trSet = (TableRow) findViewById(R.id.edfi_table_set);
		
		switchVisibility(0);
		
		initialPresets();
		initialSettings();
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
		
        Button bOK = (Button) findViewById(R.id.edfi_ok);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
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
	
	private void fillListViews()
	{
	}
	
	private void switchVisibility()
	{
		if(btPre.getState()==1)
		{
			trSet.setVisibility(View.GONE);
			trPre.setVisibility(View.VISIBLE);
		}
		
		if(btSet.getState()==1)
		{
			trPre.setVisibility(View.GONE);
			trSet.setVisibility(View.VISIBLE);
			lvSet.OnShow();
		}
	}
	private void switchVisibility(int state)
	{
		if(state==0)
		{
			btPre.setState(1);
			btSet.setState(0);
		}
		if(state==1)
		{
			btPre.setState(0);
			btSet.setState(1);
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
			      Global.AddLog("Main.ApplyFilter: " + sqlWhere);
			      Database.Data.Query.clear();
			      Database.Data.Query.LoadCaches(sqlWhere);
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
