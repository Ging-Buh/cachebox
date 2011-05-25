package de.droidcachebox.Views.FilterSettings;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EditFilterSettings extends Activity {

	private MultiToggleButton btPre;
	private MultiToggleButton btSet;
	private TableRow trPre;
	private TableRow trSet;
	private PresetListView lvPre;
	private FilterSetListView lvSet;

	
	public void onCreate(Bundle savedInstanceState) 
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_filter);
		
		btPre = (MultiToggleButton) findViewById(R.id.edfi_pre);
		btSet = (MultiToggleButton) findViewById(R.id.edfi_set);
		// Translate 
		/*btPre.setText(Global.Translations.Get("filterPreset"));
		btSet.setText(Global.Translations.Get("filterSet"));*/
		btPre.setText("Preset");
		btSet.setText("Setting");
		initialMultiToggleStates(btPre);
		initialMultiToggleStates(btSet);
		
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
            	ApplyFilter(Global.LastFilter);
            	Toast.makeText(main.mainActivity, "Applay filter. Found " + String.valueOf(Database.Data.Query.size()) + " Caches!", Toast.LENGTH_LONG).show();
            	finish();	            	
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
	
	private void initialMultiToggleStates(MultiToggleButton bt)
	{
		String ButtonTxt = (String) bt.getText();
		bt.clearStates();
		bt.addState(ButtonTxt, Global.getColor(R.attr.ToggleBtColor_off));
		bt.addState(ButtonTxt, Global.getColor(R.attr.ToggleBtColor_on));
		
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
	
	
	 public static void ApplyFilter(FilterProperties props)
	    {
	       // Cursor.Current = Cursors.WaitCursor;

	    	String sqlWhere =props.getSqlWhere();
	        Global.AddLog("Main.ApplyFilter: " + sqlWhere);
	        
	        Database.Data.Query.clear();
	        Database.Data.Query.LoadCaches(sqlWhere);
	        CachListChangedEventList.Call();
	      //  cacheListButton.Caption = Global.Translations.Get("cacheList") + " (" + Convert.ToString(Global.CacheCount) + ")";

	        //Resort(null);

	     //   Cursor.Current = Cursors.Default;

	       /* if ((Global.LastFilter.ToString() == "") || (Global.LastFilter.ToString() == FilterPresets.presets[0]))
	        {
	            holdButton_DB.BackColor = System.Drawing.Color.FromArgb(231, 239, 206);
	        }
	        else
	        {
	            holdButton_DB.BackColor = Color.Salmon;
	        };

	        holdButton_DB.Refresh();
	        CacheListPanel.CacheListChanged(); // Lade CacheListItems in der dragList
	       */
	    }
	
}
