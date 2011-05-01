package de.droidcachebox.Views.Forms;

import java.io.IOException;
import java.util.ArrayList;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.splash;
import de.droidcachebox.Events.ColorChangedEvent;
import de.droidcachebox.Events.ColorChangedEventList;
import de.droidcachebox.Events.SelectedLangChangedEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.TranslationEngine.LangStrings.Langs;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;



import de.droidcachebox.Events.SelectedLangChangedEvent;

public class Settings extends Activity implements ViewOptionsMenu,SelectedLangChangedEvent,ColorChangedEvent {
	Context context;
	

	public void onCreate(Bundle savedInstanceState) {
		if (Config.GetBool("nightMode"))
			setTheme(R.style.ThemeNight);
		else
			setTheme(R.style.ThemeDay);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
				
		SelectedLangChangedEventList.Add(this);
        ColorChangedEventList.Add(this);
		
        SettingsLayout = (LinearLayout) this.findViewById(R.id.settings_LinearLayout);
        
		SaveButton = (Button)this.findViewById(R.id.settings_save);
		SaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
	            Config.Set("GcLogin",EditTextGCName.getEditableText().toString());
	        	Config.Set("GcPass",EditTextGCPW.getEditableText().toString());
	        	Config.Set("GcVotePassword",EditTextGCVotePW.getEditableText().toString());
            	Config.AcceptChanges();
            	
            	setMainActivity();
            }
          });
		
		CancelButton = (Button)this.findViewById(R.id.settings_cancel);
		CancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Config.readConfigFile();
            	setMainActivity();
            }
          });

		LangCombo = (Spinner)this.findViewById(R.id.settings_LangCombo);
		LangCombo.setOnItemSelectedListener(
			    new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
					String selected =(String)LangCombo.getSelectedItem();
					for (Langs tmp : Sprachen)
					{
						if (selected.equals(tmp.Name))
						{
							Config.Set("Sel_LanguagePath", tmp.Path);
							try {
								Global.Translations.ReadTranslationsFile(tmp.Path);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
							
					}	
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
						
					}
			       
			    }
			);
		
		LogInTableRow =(TableRow)this.findViewById(R.id.settings_tableRowLogIn);
		ToggleLogInView = (Button)this.findViewById(R.id.toggle_button_login);
		ToggleLogInView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	LogInTableRow.setVisibility((LogInTableRow.getVisibility() == View.VISIBLE )? View.GONE : View.VISIBLE);
            	
            }
          });
		
		LabelGcName =(TextView)this.findViewById(R.id.settings_textView1);
		LabelGcPW =(TextView)this.findViewById(R.id.settings_textView2);
		LabelGcVoPw =(TextView)this.findViewById(R.id.settings_textView3);
		EditTextGCName = (EditText)this.findViewById(R.id.settings_editText1);
		EditTextGCPW = (EditText)this.findViewById(R.id.settings_editText2);
		EditTextGCVotePW = (EditText)this.findViewById(R.id.settings_editText3);
		
		FillSettings();
		setLang();
		setColor();
}

	private LinearLayout SettingsLayout;
	private Spinner LangCombo;
	private Button SaveButton;
	private Button CancelButton;
	private Button ToggleLogInView;
	private TableRow LogInTableRow;
	private TextView LabelGcName;
	private TextView LabelGcPW;
	private TextView LabelGcVoPw;
	private EditText EditTextGCName;
	private EditText EditTextGCPW;
	private EditText EditTextGCVotePW;
	
	
	
	private void setMainActivity()
	{
		finish();
	}
	
	private void setLang()
	{
		LangCombo.setPrompt(Global.Translations.Get("SelectLanguage"));
		SaveButton.setText(Global.Translations.Get("save"));
		CancelButton.setText(Global.Translations.Get("cancel"));
		LabelGcName.setText(Global.Translations.Get("LogIn"));
		LabelGcPW.setText(Global.Translations.Get("GCPW"));
		LabelGcVoPw.setText(Global.Translations.Get("GCVotePW"));
	}
	
	private void setColor()
	{
		/*
		boolean day = !Config.GetBool("nightMode");
		SettingsLayout.setBackgroundColor(day? Global.Colors.Day.ListBackground : Global.Colors.Night.ListBackground);
		SaveButton.setTextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		SaveButton.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		CancelButton.setTextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		CancelButton.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		EditTextGCName.setTextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		EditTextGCName.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		EditTextGCPW.setTextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		EditTextGCPW.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		EditTextGCVotePW.setTextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		EditTextGCVotePW.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		ToggleLogInView.setTextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		ToggleLogInView.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		
		//LangCombo.TextColor(day? Global.Colors.Day.Foreground : Global.Colors.Night.Foreground);
		LangCombo.getBackground().setColorFilter(day? Global.Colors.Day.ControlColorFilter : Global.Colors.Night.ControlColorFilter, PorterDuff.Mode.MULTIPLY);
		
		
		*/
	}
	
	private void FillSettings()
	{
		EditTextGCName.setText(Config.GetString("GcLogin"));
		EditTextGCPW.setText(Config.GetString("GcPass"));
		EditTextGCVotePW.setText(Config.GetString("GcVotePassword"));
		fillLangCombo();
		
	}
	
	ArrayList<Langs> Sprachen;
	private void fillLangCombo()
	{
		Sprachen = Global.Translations.GetLangs(Config.GetString("LanguagePath"));
		String[] items = new String[Sprachen.size()];
		int index =0;
		int selection=-1;
		for (Langs tmp : Sprachen)
		{
			if (Config.GetString("Sel_LanguagePath").equals(tmp.Path))
				selection = index;
			items[index++]=tmp.Name;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, items); 
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		LangCombo.setAdapter(adapter);
		LangCombo.setSelection(selection);
		
	}
	
	


	@Override
	public void OnShow() 
	{
		FillSettings();
	}

	@Override
	public void OnHide() {
			
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void SelectedLangChangedEvent() 
	{
		setLang();
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ColorChangedEvent() 
	{
		setColor();
	}

}
