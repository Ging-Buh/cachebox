package de.droidcachebox.Views.Forms;

import java.io.IOException;
import java.util.ArrayList;

import CB_Core.Log.Logger;
import CB_Core.TranslationEngine.SelectedLangChangedEvent;
import CB_Core.TranslationEngine.SelectedLangChangedEventList;
import CB_Core.TranslationEngine.LangStrings.Langs;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.SimpleCrypto;
import de.droidcachebox.main;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Components.Animations;
import de.droidcachebox.Custom_Controls.wheel.OnWheelChangedListener;
import de.droidcachebox.Custom_Controls.wheel.OnWheelScrollListener;
import de.droidcachebox.Custom_Controls.wheel.WheelView;
import de.droidcachebox.Custom_Controls.wheel.adapters.NumericWheelAdapter;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Views.MapView.SmoothScrollingTyp;

public class Settings extends Activity implements ViewOptionsMenu,SelectedLangChangedEvent {
	Context context;
	

	public void onCreate(Bundle savedInstanceState) {
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		Bundle bundle = getIntent().getExtras();
        int PerformButtonClickID  = (Integer)bundle.getSerializable("Show");
        
		
		context = this.getBaseContext();
				
		SelectedLangChangedEventList.Add(this);
      
		findViewsById();
		        
		SaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
	            SaveSettings();
            }
          });
		
		CancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Config.readConfigFile();
            	SaveSettings();
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
						
						
					}
			       
			    }
			);
		
		
		ToggleLogInView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(LogInTableRow,context,SettingsScrollView,ToggleLogInView);
            }
          });
		ToggleGPSView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(GPSTableRow,context,SettingsScrollView,ToggleGPSView);
            }
          });
		ToggleMapView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(MapTableRow,context,SettingsScrollView,ToggleMapView);
            	
            }
          });
		ToggleMiscView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(MiscTableRow,context,SettingsScrollView,ToggleMiscView);
            }
          });
		ToggleDebugView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) 
            {
            	Animations.ToggleViewSlideUp_Down(DebugTableRow,context,SettingsScrollView,ToggleDebugView);
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
		
		cbMoveMapCenterWithSpeed.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Boolean newState = !Config.GetBool("MoveMapCenterWithSpeed");
				Config.Set("MoveMapCenterWithSpeed", newState);
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
				try {
					Config.Set("GcPass",SimpleCrypto.encrypt("DCB",EditTextGCPW.getEditableText().toString()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
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
		    	try {
					Config.Set("GcVotePassword",SimpleCrypto.encrypt("DCB",EditTextGCVotePW.getEditableText().toString()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		EditTextGCJoker.addTextChangedListener(new TextWatcher() {
			
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
				Config.Set("GcJoker", EditTextGCJoker.getText().toString());
				
			}
		});
		
		chkDebugShowPanel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Boolean newState = !Config.GetBool("DebugShowPanel");
				Config.Set("DebugShowPanel", newState);
				((main) main.mainActivity).setDebugVisible();
				
			}
		});
		
		
		 initWheel(ScreenLock_wheel_m,0,10);
	     initWheel(ScreenLock_wheel_sec,0,59);
	     
		
		
		FillSettings();
		setLang();
		
		OnShow();
		
		if (PerformButtonClickID!=-1)
		{
			PerformeButtonClick(PerformButtonClickID);
		}
}

	
	private void PerformeButtonClick(int index)
	{
		switch (index)
		{
			case 1:
				ToggleLogInView.performClick();
				break;
				
			case 2:
				ToggleGPSView.performClick();
				break;
				
			case 3:
				ToggleMapView.performClick();
				break;
				
			case 4:
				ToggleMiscView.performClick();
				break;
		
		}
	}
	
	
	private LinearLayout SettingsLayout;
	private ScrollView SettingsScrollView;
	private Spinner LangCombo;
	private Button SaveButton;
	private Button CancelButton;
	private Button ToggleLogInView;
	private TableRow LogInTableRow;
	private TextView LabelGcName;
	private TextView LabelGcPW;
	private TextView LabelGcVoPw;
	private TextView LabelGcJoker;
	private EditText EditTextGCName;
	private EditText EditTextGCPW;
	private EditText EditTextGCVotePW;
	private EditText EditTextGCJoker;
	private Button ToggleGPSView;
	private TableRow GPSTableRow;
	private CheckBox checkBoxHTCCompass;
	private CheckBox cbMoveMapCenterWithSpeed;
	private TextView DescCompassLevel;
	private EditText EditCompassLevel;
	private Button ToggleMapView;
	private TableRow MapTableRow;
	private Button ToggleMiscView;
	private TableRow MiscTableRow;
	private CheckBox chkMapink;
	private CheckBox chkCycleMap;
	private CheckBox chkOsmarenerer;
	private Spinner OsmMinLevel;
	private Spinner OsmMaxLevel;
	private Spinner ZoomCross;
	private Spinner SmoothScrolling;
	private Spinner TrackDistance;
	private CheckBox chkTrackStart;
	private TextView DescMapLayer;
	private TextView DescOsmMinLevel;
	private TextView DescOsmMaxLevel;
	private TextView DescZoomCrossLevel;
	private TextView DescSmothScroll;
	private TextView DescTrackRec;
	private TextView DescTrackCount;
	private Button ToggleDebugView;
	private TableRow DebugTableRow;
	private CheckBox chkAllowInetAccess;
	private CheckBox chkDebugShowPanel;
	private CheckBox chkDebugMemory;
	private CheckBox chkDebugMsg;
	private CheckBox chkDebugLog;
	private Spinner ApproachSound;
	private CheckBox chkDebugMarker;
    private WheelView ScreenLock_wheel_m ;
    private WheelView ScreenLock_wheel_sec ;
    private CheckBox chkAllowLandscape;
    private TextView DescScreenLock;
	
 	private void findViewsById()
	{
		SettingsLayout = (LinearLayout) this.findViewById(R.id.settings_LinearLayout);
		SettingsScrollView = (ScrollView) this.findViewById(R.id.settings_scrollView);
        LangCombo = (Spinner)this.findViewById(R.id.settings_LangCombo);
		CancelButton = (Button)this.findViewById(R.id.settings_cancel);
		SaveButton = (Button)this.findViewById(R.id.settings_save);
		LangCombo = (Spinner)this.findViewById(R.id.settings_LangCombo);
		LogInTableRow =(TableRow)this.findViewById(R.id.settings_tableRowLogIn);
		ToggleLogInView = (Button)this.findViewById(R.id.toggle_button_login);
		LabelGcName =(TextView)this.findViewById(R.id.settings_textView1);
		LabelGcPW =(TextView)this.findViewById(R.id.settings_textView2);
		LabelGcVoPw =(TextView)this.findViewById(R.id.settings_textView3);
		LabelGcJoker =(TextView)this.findViewById(R.id.settings_textView4);
		EditTextGCName = (EditText)this.findViewById(R.id.settings_editText1);
		EditTextGCPW = (EditText)this.findViewById(R.id.settings_editText2);
		EditTextGCVotePW = (EditText)this.findViewById(R.id.settings_editText3);
		EditTextGCJoker = (EditText)this.findViewById(R.id.settings_editText4);
		GPSTableRow =(TableRow)this.findViewById(R.id.settings_tableRowgps);
		ToggleGPSView = (Button)this.findViewById(R.id.toggle_button_gps);
		checkBoxHTCCompass = (CheckBox)this.findViewById(R.id.settings_use_intern_compass);
		cbMoveMapCenterWithSpeed= (CheckBox)this.findViewById(R.id.settings_moveMapCenterWithSpeed);
		EditCompassLevel = (EditText)this.findViewById(R.id.settings_compass_level_edit);
		DescCompassLevel = (TextView)this.findViewById(R.id.settings_compass_info_text);
		ToggleMapView = (Button)this.findViewById(R.id.toggle_button_map);
		MapTableRow =(TableRow)this.findViewById(R.id.settings_tableRow_map);
		ToggleMiscView = (Button)this.findViewById(R.id.toggle_button_misc);
		MiscTableRow =(TableRow)this.findViewById(R.id.settings_tableRow_misc);
		chkMapink=(CheckBox)this.findViewById(R.id.settings_Mapnik);
		chkCycleMap=(CheckBox)this.findViewById(R.id.settings_Cycle_Map);
		chkOsmarenerer=(CheckBox)this.findViewById(R.id.settings_Osmarenderer);
		OsmMinLevel=(Spinner)this.findViewById(R.id.settings_spinner_OSM_min);
		OsmMaxLevel=(Spinner)this.findViewById(R.id.settings_spinner_OSM_max);
		ZoomCross=(Spinner)this.findViewById(R.id.settings_spinner_Zoom_Cross);
		SmoothScrolling=(Spinner)this.findViewById(R.id.settings_spinner_Smooth_Scrolling);
		TrackDistance=(Spinner)this.findViewById(R.id.settings_spinner_Track_Count);
		chkTrackStart=(CheckBox)this.findViewById(R.id.settings_chk_Start_track);
		DescMapLayer=(TextView)this.findViewById(R.id.settings_desc_map_layer);
		DescOsmMinLevel=(TextView)this.findViewById(R.id.settings_desc_OSM_min);
		DescOsmMaxLevel=(TextView)this.findViewById(R.id.settings_desc_OSM_max);
		DescZoomCrossLevel=(TextView)this.findViewById(R.id.settings_desc_ZoomCross);
		DescSmothScroll=(TextView)this.findViewById(R.id.settings_desc_Smooth_Scrolling);
		DescTrackRec=(TextView)this.findViewById(R.id.settings_desc_Track_Rec);
		DescTrackCount=(TextView)this.findViewById(R.id.settings_desc_Track_count);
		ToggleDebugView = (Button)this.findViewById(R.id.toggle_button_debug);
		DebugTableRow =(TableRow)this.findViewById(R.id.settings_tableRow_debug);
		chkAllowInetAccess=(CheckBox)this.findViewById(R.id.settings_allow_internet_access);
		chkDebugShowPanel = (CheckBox)this.findViewById(R.id.settings_debug_chkShow);
		chkDebugMemory = (CheckBox)this.findViewById(R.id.settings_debug_chkMemory);
		chkDebugMsg = (CheckBox)this.findViewById(R.id.settings_debug_chkMsg);
		chkDebugLog = (CheckBox)this.findViewById(R.id.settings_debug_chkLog);
		ApproachSound = (Spinner)this.findViewById(R.id.settings_spinner_Approach_Sound);
		chkDebugMarker = (CheckBox)this.findViewById(R.id.settings_debug_chkMarker);
		ScreenLock_wheel_m = (WheelView)this.findViewById(R.id.settings_ScreenLock_m);
		ScreenLock_wheel_sec = (WheelView)this.findViewById(R.id.settings_ScreenLock_sec);
		chkAllowLandscape = (CheckBox)this.findViewById(R.id.settings_allow_LandScape);
		DescScreenLock=(TextView)this.findViewById(R.id.settings_desc_ScreenLock);
	}
	
	private void setLang()
	{
		LangCombo.setPrompt(Global.Translations.Get("SelectLanguage"));
		SaveButton.setText(Global.Translations.Get("save"));
		CancelButton.setText(Global.Translations.Get("cancel"));
		LabelGcName.setText(Global.Translations.Get("LogIn"));
		LabelGcPW.setText(Global.Translations.Get("GCPW"));
		LabelGcVoPw.setText(Global.Translations.Get("GCVotePW"));
		LabelGcJoker.setText(Global.Translations.Get("GCJoker"));
        checkBoxHTCCompass.setText(Global.Translations.Get("UseHtcCompass"));
        DescCompassLevel.setText(Global.Translations.Get("DescHtcLevel"));
        chkAllowInetAccess.setText(Global.Translations.Get("AllowInternet"));
        chkAllowLandscape.setText(Global.Translations.Get("AllowLandscape"));
        DescScreenLock.setText("Screen lock [min/sec]" + String.format("%n") + "(<5sec = off)");
	}
	
		
	private void FillSettings()
	{
		try
		{
		EditTextGCName.setText(Config.GetString("GcLogin"));
		EditTextGCPW.setText(SimpleCrypto.decrypt("DCB", Config.GetString("GcPass")));
		EditTextGCVotePW.setText(SimpleCrypto.decrypt("DCB", Config.GetString("GcVotePassword")));
		EditTextGCJoker.setText(Config.GetString("GcJoker"));
		fillLangCombo();
		checkBoxHTCCompass.setChecked(Config.GetBool("HtcCompass"));
		EditCompassLevel.setText(String.valueOf(Config.GetInt("HtcLevel")));
		chkCompassLevelViewState();
		fillLevelSpinner();
		fillSmothSpinner();
		fillTrackDistanceSpinner();
		chkTrackStart.setChecked(Config.GetBool("TrackRecorderStartup"));
		chkMapink.setChecked(Config.GetBool("ImportLayerOsm"));
		chkCycleMap.setChecked(Config.GetBool("ImportLayerOcm"));
		chkOsmarenerer.setChecked(Config.GetBool("ImportLayerOsma"));
		cbMoveMapCenterWithSpeed.setChecked(Config.GetBool("MoveMapCenterWithSpeed"));
		OsmMinLevel.setSelection(Config.GetInt("OsmMinLevel"));
		OsmMaxLevel.setSelection(Config.GetInt("OsmMaxLevel"));
		ZoomCross.setSelection(Integer.parseInt(Config.GetString("ZoomCross"))-14);
		SmoothScrolling.setSelection(smoth.indexOf(SmoothScrollingTyp.valueOf(Config.GetString("SmoothScrolling"))));
		
		TrackDistance.setSelection(distance.indexOf(Config.GetInt("TrackDistance")));
		
		chkAllowInetAccess.setChecked(Config.GetBool("AllowInternetAccess"));


		chkDebugShowPanel.setChecked(Config.GetBool("DebugShowPanel"));
		chkDebugMemory.setChecked(Config.GetBool("DebugMemory"));
		chkDebugMsg.setChecked(Config.GetBool("DebugShowMsg"));
		chkDebugMarker.setChecked(Config.GetBool("DebugShowMarker"));
		chkDebugLog.setChecked(Config.GetBool("DebugShowLog"));
		
		fillApproachSpinner();
		ApproachSound.setSelection(approachValues.indexOf(Config.GetInt("SoundApproachDistance")));
		
		ScreenLock_wheel_m.setCurrentItem(Config.GetInt("LockM"));
		ScreenLock_wheel_sec.setCurrentItem(Config.GetInt("LockSec"));
		
		if(Global.Debug)
			ToggleDebugView.setVisibility(View.VISIBLE);
		
		chkAllowLandscape.setChecked(Config.GetBool("AllowLandscape"));
	
		
		}
		catch(Exception e)
		{
			Logger.Error("Settings.FillSettings()", "", e);
		}
	}
	
	private void SaveSettings()
	{
		Config.Set("GcLogin",EditTextGCName.getEditableText().toString());
		Config.Set("GcJoker",EditTextGCJoker.getEditableText().toString());
    	try 
    	{
			Config.Set("GcPass",SimpleCrypto.encrypt("DCB",EditTextGCPW.getEditableText().toString()));
	    	Config.Set("GcVotePassword",SimpleCrypto.encrypt("DCB",EditTextGCVotePW.getEditableText().toString()));
		} catch (Exception e) 
		{
			Logger.Error("Settings.SaveSettings()", "Save GcPass/ GcVotePass", e);
			e.printStackTrace();
		}
    	Config.Set("TrackRecorderStartup",chkTrackStart.isChecked());
    	Config.Set("ImportLayerOsm",chkMapink.isChecked());
    	Config.Set("ImportLayerOcm",chkCycleMap.isChecked());
    	Config.Set("ImportLayerOsma",chkOsmarenerer.isChecked());
    	Config.Set("OsmMinLevel",(Integer) OsmMinLevel.getSelectedItem());
    	Config.Set("OsmMaxLevel",(Integer) OsmMaxLevel.getSelectedItem());
    	Config.Set("ZoomCross",(Integer) ZoomCross.getSelectedItem());
    	String SmoothScrollingString = ((SmoothScrollingTyp) SmoothScrolling.getSelectedItem()).name();
    	Global.SmoothScrolling = SmoothScrollingTyp.valueOf(SmoothScrollingString);
    	Config.Set("SmoothScrolling",SmoothScrollingString);
    	Config.Set("TrackDistance",(Integer) TrackDistance.getSelectedItem());
    	
    	Config.Set("AllowInternetAccess",chkAllowInetAccess.isChecked());
    	Config.Set("DebugShowPanel",chkDebugShowPanel.isChecked());
    	Config.Set("DebugMemory",chkDebugMemory.isChecked());
    	Config.Set("DebugShowMsg",chkDebugMsg.isChecked());
    	Config.Set("DebugShowMarker",chkDebugMarker.isChecked());
    	Config.Set("DebugShowLog",chkDebugLog.isChecked());
    	
    	Config.Set("SoundApproachDistance",(Integer) ApproachSound.getSelectedItem());
    	
    	int M=ScreenLock_wheel_m.getCurrentItem();
    	int Sec =ScreenLock_wheel_sec.getCurrentItem();
    	Config.Set("LockM",M);
    	Config.Set("LockSec",Sec);
    	((main) main.mainActivity).setCounterNew(((M*60)+Sec)*1000);
    	
    	((main) main.mainActivity).setDebugVisible();
    	((main) main.mainActivity).setDebugMsg("");
    	Config.Set("AllowLandscape",chkAllowLandscape.isChecked());
    	
    	Config.AcceptChanges();
		finish();
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
	
	private void fillLevelSpinner()
	{
		Integer Level[] = new Integer[21];
		Integer CrossLevel[] = new Integer[8];
				
		for (int i =0; i<22; i++)
		{
			if(i<21)
				Level[i]=i;
			
			if(i>13)
				CrossLevel[i-14]=i;
				
		}
		
		ArrayAdapter<Integer> minAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, Level); 
		minAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		ArrayAdapter<Integer> maxAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, Level); 
		maxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		ArrayAdapter<Integer> crossAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, CrossLevel); 
		crossAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		OsmMinLevel.setAdapter(minAdapter);
		OsmMaxLevel.setAdapter(maxAdapter);
		ZoomCross.setAdapter(crossAdapter);
	}
	
	
	
	private ArrayList<SmoothScrollingTyp> smoth; 
	
	
	private void fillSmothSpinner()
	{
		smoth= new ArrayList<SmoothScrollingTyp>();
		SmoothScrollingTyp[] tmp = SmoothScrollingTyp.values();
		for ( SmoothScrollingTyp item : tmp)
		{
			smoth.add(item);
		}
		
		ArrayAdapter<SmoothScrollingTyp> smothAdapter = new ArrayAdapter<SmoothScrollingTyp>(this,android.R.layout.simple_spinner_item, smoth); 
		smothAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SmoothScrolling.setAdapter(smothAdapter);
	}
	
	
	ArrayList<Integer> approachValues= new ArrayList<Integer>();
	Integer[] approach = new Integer[]{0,2,10,25,50,100,200,500,1000};
	private void fillApproachSpinner()
	{
		
		for ( Integer item : approach)
		{
			approachValues.add(item);
		}
		
		ArrayAdapter<Integer> approachAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, approachValues); 
		approachAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ApproachSound.setAdapter(approachAdapter);
	}
	
	Integer[] TrackDistanceArray = new Integer[]{1,3,5,10,20};
	ArrayList<Integer> distance = new ArrayList<Integer>();
	
	private void fillTrackDistanceSpinner()
	{
		distance = new ArrayList<Integer>();
		
		for ( Integer item : TrackDistanceArray)
		{
			distance.add(item);
		}
		
		ArrayAdapter<Integer> TrackCountsAdapter = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, distance); 
		TrackCountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		TrackDistance.setAdapter(TrackCountsAdapter);
	}

 	private void setButtonHeight() 
	{
		// setzt die h�he der Buttons auf die H�he der ComboBox
		int Height = (int) (Global.scaledFontSize_normal*4);
		LangCombo.setMinimumHeight(Height);
		ToggleLogInView.setHeight(Height);
		ToggleGPSView.setHeight(Height);
		ToggleMapView.setHeight(Height);
		ToggleMiscView.setHeight(Height);
		ToggleDebugView.setHeight(Height);
	}

	


	@Override
	public void OnShow() 
	{
		setButtonHeight();
		FillSettings();
	}

	@Override
	public void OnHide() {
			
	}

	@Override
	public void OnFree() {
		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void SelectedLangChangedEventCalled() 
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
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
     * Initializes wheel
     * @param id the wheel widget Id
     */
    private void initWheel(WheelView wheel, int min, int max) {
        wheel.setViewAdapter(new NumericWheelAdapter(this, min, max));
        wheel.setVisibleItems(3);
        
        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(true);
    }
    
    // Wheel scrolled flag
    private boolean wheelScrolled = false;
    
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            wheelScrolled = true;
        }
        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
            updateStatus();
        }
    };
    
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!wheelScrolled) {
                updateStatus();
            }
        }
    };
    
    /**
     * Updates status
     */
    private void updateStatus() 
    {
        
    }

    
    /**
     * Returns wheel by Id
     * @param id the wheel Id
     * @return the wheel with passed Id
     */
    private WheelView getWheel(int id) {
        return (WheelView) findViewById(id);
    }
    
 
}
