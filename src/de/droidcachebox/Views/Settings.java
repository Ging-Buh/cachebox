package de.droidcachebox.Views;

import java.io.IOException;
import java.util.ArrayList;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.SelectedLangChangedEvent;
import de.droidcachebox.Events.SelectedLangChangedEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.TranslationEngine.LangStrings.Langs;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

public class Settings extends FrameLayout implements ViewOptionsMenu,SelectedLangChangedEvent {
	Context context;
	

	public Settings(Context context, LayoutInflater inflater) {
		super(context);
		this.context = context;
		LinearLayout settingsLayout = (LinearLayout)inflater.inflate(R.layout.settings, null, false);
		this.addView(settingsLayout);
		SelectedLangChangedEventList.Add(this);
       
		SaveButton = (Button)this.findViewById(R.id.settings_save);
		SaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
	            Config.Set("GcLogin",EditTextGCName.getEditableText().toString());
	        	Config.Set("GcPass",EditTextGCPW.getEditableText().toString());
	        	Config.Set("GcVotePassword",EditTextGCVotePW.getEditableText().toString());
            	Config.AcceptChanges();
            }
          });
		
		CancelButton = (Button)this.findViewById(R.id.settings_cancel);
		CancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Config.readConfigFile();
            	FillSettings();
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
            	LogInTableRow.setVisibility((LogInTableRow.getVisibility() == View.VISIBLE )? GONE : VISIBLE);
            }
          });
		
		LabelGcName =(TextView)this.findViewById(R.id.settings_textView1);
		LabelGcPW =(TextView)this.findViewById(R.id.settings_textView2);
		LabelGcVoPw =(TextView)this.findViewById(R.id.settings_textView3);
		EditTextGCName = (EditText)this.findViewById(R.id.settings_editText1);
		EditTextGCPW = (EditText)this.findViewById(R.id.settings_editText2);
		EditTextGCVotePW = (EditText)this.findViewById(R.id.settings_editText3);

		setLang();
}

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
	
	}
	
	private void FillSettings()
	{
		EditTextGCName.setText(Config.GetString("GcLogin"));
		EditTextGCPW.setText(Config.GetString("GcPass"));
		EditTextGCVotePW.setText(Config.GetString("GcVotePassword"));
		
		fillLangCombo();
		this.invalidate();
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
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, items); 
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
		LangCombo.setAdapter(adapter);
		LangCombo.setSelection(selection);
		
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
	public void SelectedLangChangedEvent() {
		setLang();
		
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

}
