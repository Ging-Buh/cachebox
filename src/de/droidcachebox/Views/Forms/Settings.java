package de.droidcachebox.Views.Forms;

import java.io.IOException;
import java.util.ArrayList;


import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.splash;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.Animations;

import de.droidcachebox.Events.SelectedLangChangedEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.TranslationEngine.LangStrings.Langs;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;



import de.droidcachebox.Events.SelectedLangChangedEvent;

public class Settings extends Activity implements ViewOptionsMenu,SelectedLangChangedEvent {
	Context context;
	

	public void onCreate(Bundle savedInstanceState) {
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		context = this.getBaseContext();
		
		
		SelectedLangChangedEventList.Add(this);
      
		findViewsById();
        
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
		
		CancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Config.readConfigFile();
            	setMainActivity();
            }
          });

		LangCombo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
		
		
		
		
		ToggleLogInView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(LogInTableRow,context);
            }
          });
		ToggleGPSView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(GPSTableRow,context);
            }
          });
		
		checkBoxUseCelltower.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Boolean newState = !Config.GetBool("UseCelltower");
				Config.Set("UseCelltower", newState);
				
			}
		});
		
		checkBoxHTCCompass.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Boolean newState = !Config.GetBool("HtcCompass");
				Config.Set("HtcCompass", newState);
				chkCompassLevelViewState();
			}
		});
		
		EditCompassLevel.addTextChangedListener(new TextWatcher()
        {
  
		@Override
		public void afterTextChanged(Editable arg0) {
			try
			{
			int newState = Integer.parseInt(EditCompassLevel.getText().toString());
			Config.Set("HtcLevel", newState);
			}
			catch(Exception e)
			{}

		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}
}); 
		EditTextGCName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				Config.Set("GcLogin", EditTextGCName.getText().toString());
				
			}
		});
		EditTextGCPW.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				Config.Set("GcPass", EditTextGCName.getText().toString());
				
			}
		});
		EditTextGCVotePW.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				Config.Set("GcVotePassword", EditTextGCName.getText().toString());
				
			}
		});
		
		FillSettings();
		setLang();
		
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
	private Button ToggleGPSView;
	private TableRow GPSTableRow;
	private CheckBox checkBoxUseCelltower;
	private CheckBox checkBoxHTCCompass;
	private TextView DescCompassLevel;
	private EditText EditCompassLevel;
	
	
	private void setMainActivity()
	{
		finish();
	}
	
	private void findViewsById()
	{
		SettingsLayout = (LinearLayout) this.findViewById(R.id.settings_LinearLayout);
        LangCombo = (Spinner)this.findViewById(R.id.settings_LangCombo);
		CancelButton = (Button)this.findViewById(R.id.settings_cancel);
		SaveButton = (Button)this.findViewById(R.id.settings_save);
		LangCombo = (Spinner)this.findViewById(R.id.settings_LangCombo);
		LogInTableRow =(TableRow)this.findViewById(R.id.settings_tableRowLogIn);
		ToggleLogInView = (Button)this.findViewById(R.id.toggle_button_login);
		LabelGcName =(TextView)this.findViewById(R.id.settings_textView1);
		LabelGcPW =(TextView)this.findViewById(R.id.settings_textView2);
		LabelGcVoPw =(TextView)this.findViewById(R.id.settings_textView3);
		EditTextGCName = (EditText)this.findViewById(R.id.settings_editText1);
		EditTextGCPW = (EditText)this.findViewById(R.id.settings_editText2);
		EditTextGCVotePW = (EditText)this.findViewById(R.id.settings_editText3);
		GPSTableRow =(TableRow)this.findViewById(R.id.settings_tableRowgps);
		ToggleGPSView = (Button)this.findViewById(R.id.toggle_button_gps);
		checkBoxUseCelltower = (CheckBox)this.findViewById(R.id.settings_use_cell_id);
		checkBoxHTCCompass = (CheckBox)this.findViewById(R.id.settings_use_intern_compass);
		EditCompassLevel = (EditText)this.findViewById(R.id.settings_compass_level_edit);
		DescCompassLevel = (TextView)this.findViewById(R.id.settings_compass_info_text);
	}
	
	private void setLang()
	{
		LangCombo.setPrompt(Global.Translations.Get("SelectLanguage"));
		SaveButton.setText(Global.Translations.Get("save"));
		CancelButton.setText(Global.Translations.Get("cancel"));
		LabelGcName.setText(Global.Translations.Get("LogIn"));
		LabelGcPW.setText(Global.Translations.Get("GCPW"));
		LabelGcVoPw.setText(Global.Translations.Get("GCVotePW"));
		 checkBoxUseCelltower.setText(Global.Translations.Get("UseCellId"));
         checkBoxHTCCompass.setText(Global.Translations.Get("UseHtcCompass"));
         DescCompassLevel.setText(Global.Translations.Get("DescHtcLevel"));
	}
	
		
	private void FillSettings()
	{
		EditTextGCName.setText(Config.GetString("GcLogin"));
		EditTextGCPW.setText(Config.GetString("GcPass"));
		EditTextGCVotePW.setText(Config.GetString("GcVotePassword"));
		fillLangCombo();
		checkBoxUseCelltower.setChecked(Config.GetBool("UseCelltower"));
		checkBoxHTCCompass.setChecked(Config.GetBool("HtcCompass"));
		EditCompassLevel.setText(String.valueOf(Config.GetInt("HtcLevel")));
		chkCompassLevelViewState();
		
	}
	
	
	private void chkCompassLevelViewState()
	{
		if (Config.GetBool("HtcCompass"))
		{			
			DescCompassLevel.setEnabled(true);
			EditCompassLevel.setEnabled(true);
		}
		else
		{
			DescCompassLevel.setEnabled(false);
			EditCompassLevel.setEnabled(false);
		}
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

	
}
