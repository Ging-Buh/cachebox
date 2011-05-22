package de.droidcachebox.Views.Forms;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Custom_Controls.MultiToggleButton;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.ToggleButton;

public class EditFilterSettings extends Activity {

	private MultiToggleButton btPre;
	private MultiToggleButton btSet;
	private TableRow trPre;
	private TableRow trSet;
	
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
		
        Button bOK = (Button) findViewById(R.id.edco_ok);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            	finish();	            	
            }
          });
        Button bCancel = (Button) findViewById(R.id.edco_cancel);
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
		
	}
	
	private void initialSettings()
	{
		
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
		
}
