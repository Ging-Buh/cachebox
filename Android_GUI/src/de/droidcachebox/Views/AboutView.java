package de.droidcachebox.Views;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import CB_Core.Api.GroundspeakAPI;
import de.droidcachebox.Events.GpsStateChangeEvent;
import de.droidcachebox.Events.GpsStateChangeEventList;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.NumerikInputBox;
import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AboutView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent, PositionEvent ,GpsStateChangeEvent{

	Context context;
	Cache aktCache;
	
	ProgressBar myProgressBar;
	TextView myTextView;
	TextView versionTextView;
	TextView descTextView;
	TextView CachesFoundLabel;
	TextView lblGPS;
	TextView GPS;
	TextView lblAccuracy;
	TextView Accuracy;
	TextView lblWP;
	TextView WP;
	TextView lblCord;
	TextView Cord;
	TextView lblCurrent;
	TextView Current;
	Bitmap bitmap = null;
	Bitmap logo = null;
	
	public static LinearLayout strengthLayout;
	
	public static AboutView Me;
	private static ProgressDialog pd;
	public AboutView(Context context, final LayoutInflater inflater) 
	{
		super(context);
		
		Me=this;
		
		SelectedCacheEventList.Add(this);

		FrameLayout notesLayout = (FrameLayout)inflater.inflate(R.layout.about, null, false);
		this.addView(notesLayout);
		
		findViewById();
		setText();
		
		
		
		 // add Event Handler
        SelectedCacheEventList.Add(this);
        PositionEventList.Add(this);
		GpsStateChangeEventList.Add(this);
		
		
        
        
		CachesFoundLabel.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View arg0) 
			{
				MessageBox.Show(Global.Translations.Get("LoadFounds"), Global.Translations.Get("AdjustFinds"), MessageBoxButtons.YesNo,MessageBoxIcon.GC_Live, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int button) 
					{
						// Behandle das ergebniss
						switch (button)
						{
							case -1:
								
								Thread thread = new Thread()
								  {
								      @Override
								      public void run() 
								      {
								    	  transFounds = GroundspeakAPI.GetCachesFound(Config.GetAccessToken());
								    	  onlineFoundsReadyHandler.sendMessage(onlineFoundsReadyHandler.obtainMessage(1));
								      }

								  };

								  
								  pd = ProgressDialog.show(main.mainActivity, "", 
							                 "Search Online", true);
								 
								  thread.start();
								
								
								
								break;
							case -2:
								NumerikInputBox.Show(Global.Translations.Get("AdjustFinds"),Global.Translations.Get("TelMeFounds"),CB_Core.Config.GetInt("FoundOffset"), DialogListner);
								break;
							case -3:
								
								break;
						}
						
						dialog.dismiss();
					}
					
			    });

				
				
			}
		});
		
		
		 
		 
		
		WP.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View arg0) 
			{
				 if (GlobalCore.SelectedCache()== null)
		                return;

		            if (!Config.GetBool("AllowInternetAccess"))
		            {
		            	Toast.makeText(main.mainActivity, Global.Translations.Get("allowInetConn"), Toast.LENGTH_SHORT).show();
		                return;
		            }
		            
		            try
		            {
		            	Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(GlobalCore.SelectedCache().Url.trim()));
		            	main.mainActivity.startActivity(browserIntent);
		            }
		            catch (Exception exc)
		            {
		            	Toast.makeText(main.mainActivity, Global.Translations.Get("Cann_not_open_cache_browser") + " (" + GlobalCore.SelectedCache().Url.trim() + ")", Toast.LENGTH_SHORT).show();
                    }
				
			}
			
			
			
			
		});
		
	}
	
	
	protected static final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	   { 
		
		@Override public void onClick(DialogInterface dialog, int button) 
			{
				String text =NumerikInputBox.editText.getText().toString();
				// Behandle das ergebniss
				switch (button)
				{
					case -1: // ok Clicket
						int newFounds = Integer.parseInt(text);

		                Config.Set("FoundOffset", newFounds );
		                Config.AcceptChanges();

						break;
					case -2: // cancel clicket
						
						break;
					case -3:
						
						break;
				}
				
				dialog.dismiss();
				AboutView.Me.refreshText();
			}
			
	    };
	
	    
	private int transFounds=-1;
	private Handler onlineFoundsReadyHandler = new Handler() 
	{
		public void handleMessage(Message msg) 
	    {
			pd.dismiss();
			
			if(transFounds>-1)
			{
				String Text = Global.Translations.Get("FoundsSetTo");
			Text = Text.replace("%s", String.valueOf(transFounds));
			MessageBox.Show(Text, Global.Translations.Get("LoadFinds!"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
			
            Config.Set("FoundOffset", transFounds );
            Config.AcceptChanges();
            AboutView.Me.refreshText();
			}
			else
			{
				MessageBox.Show(Global.Translations.Get("LogInErrorLoadFinds"), "", MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
			}
			
			
	    }
	};
	
	
	private void findViewById()
	{
		CachesFoundLabel=(TextView)findViewById(R.id.about_CachesFoundLabel);
		descTextView=(TextView)findViewById(R.id.splash_textViewDesc);
		versionTextView=(TextView)findViewById(R.id.splash_textViewVersion);
		myTextView= (TextView)findViewById(R.id.splash_TextView);
		lblGPS=(TextView)findViewById(R.id.about_lblGPS);
		GPS=(TextView)findViewById(R.id.about_GPS);
		lblAccuracy=(TextView)findViewById(R.id.about_lblAccuracy);
		Accuracy=(TextView)findViewById(R.id.about_Accuracy);
		lblWP=(TextView)findViewById(R.id.about_lblWP);
		WP=(TextView)findViewById(R.id.about_WP);
		lblCord=(TextView)findViewById(R.id.about_lblCord);
		Cord=(TextView)findViewById(R.id.about_Cord);
		lblCurrent=(TextView)findViewById(R.id.about_lblCurrent);
		Current=(TextView)findViewById(R.id.about_Current);
		
		strengthLayout = (LinearLayout)findViewById(R.id.strength_control);
		
		//set LinkLable Color
		CachesFoundLabel.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		WP.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		
		//set Text Color
		CachesFoundLabel.setTextColor(Global.getColor(R.attr.TextColor));
		descTextView.setTextColor(Global.getColor(R.attr.TextColor));
		versionTextView.setTextColor(Global.getColor(R.attr.TextColor));
		myTextView.setTextColor(Global.getColor(R.attr.TextColor));
		lblGPS.setTextColor(Global.getColor(R.attr.TextColor));
		GPS.setTextColor(Global.getColor(R.attr.TextColor));
		lblAccuracy.setTextColor(Global.getColor(R.attr.TextColor));
		Accuracy.setTextColor(Global.getColor(R.attr.TextColor));
		lblWP.setTextColor(Global.getColor(R.attr.TextColor));
		WP.setTextColor(Global.getColor(R.attr.TextColor));
		lblCord.setTextColor(Global.getColor(R.attr.TextColor));
		Cord.setTextColor(Global.getColor(R.attr.TextColor));
		lblCurrent.setTextColor(Global.getColor(R.attr.TextColor));
		Current.setTextColor(Global.getColor(R.attr.TextColor));
		
		//Set Progressbar from Splash unvisible and release the Obj.
		myProgressBar=(ProgressBar)findViewById(R.id.splash_progressbar);
		myProgressBar.setVisibility(View.GONE);
		myProgressBar=null;
	}
	
	private void setText()
	{
		versionTextView.setText(Global.getVersionString());
		descTextView.setText(Global.splashMsg);
		
		lblGPS.setText(Global.Translations.Get("gps"));
        
         lblWP.setText(Global.Translations.Get("waypoint"));
         lblCord.setText(Global.Translations.Get("coordinate"));
         lblCurrent.setText(Global.Translations.Get("current"));
         lblAccuracy.setText(Global.Translations.Get("accuracy"));
         
         
         GPS.setText(Global.Translations.Get("not_active"));
		
		
		
		refreshText();
	}
	
	public void refreshText()
	{
		CachesFoundLabel.setText(Global.Translations.Get("caches_found") + " " + String.valueOf(Config.GetInt("FoundOffset")));
		if (GlobalCore.SelectedCache() != null)
            if (GlobalCore.SelectedWaypoint() != null)
            {
            	WP.setText(GlobalCore.SelectedWaypoint().GcCode);
            	Cord.setText(Global.FormatLatitudeDM(GlobalCore.SelectedWaypoint().Latitude()) + " " + Global.FormatLongitudeDM(GlobalCore.SelectedWaypoint().Longitude()));
            }
            else
            {
            	WP.setText(GlobalCore.SelectedCache().GcCode);
            	Cord.setText(Global.FormatLatitudeDM(GlobalCore.SelectedCache().Latitude()) + " " + Global.FormatLongitudeDM(GlobalCore.SelectedCache().Longitude()));
            }
		
		this.invalidate();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) 
	{
		refreshText();
	}

	@Override
	public boolean ItemSelected(MenuItem item) 
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) 
	{
	}

	@Override
	public void OnShow() 
	{
		LoadImages();
		
		setSatStrength();
	}

	@Override
	public void OnHide() 
	{
		ReleaseImages();
		
	}

	@Override
	public void OnFree() 
	{
		
	}

	@Override
	public int GetMenuId() 
	{
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) 
	{
	}

	@Override
	public int GetContextMenuId() 
	{
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) 
	{
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) 
	{
		return false;
	}



	@Override
	public void PositionChanged(Location location) 
	{
		/* if ((Global.Locator.getLocation() != null) && (Global.Locator.getLocation().hasAccuracy()))
	        {
	        	int radius = (int) Global.Locator.getLocation().getAccuracy();
	        	Accuracy.setText("+/- " + String.valueOf(radius) + "m (" + Global.Locator.ProviderString()+")");
	        }
		 else
		 {
			 Accuracy.setText("");
		 }
		 if (Global.Locator.getLocation() != null)
		 {
			 Current.setText(Global.FormatLatitudeDM(Global.Locator.getLocation().getLatitude()) + " " + Global.FormatLongitudeDM(Global.Locator.getLocation().getLongitude()));
			 GPS.setText(Global.Translations.Get("alt") + " " + Global.Locator.getAltString());
		 }
		 
		 if (Global.Locator == null)
         {
             GPS.setText(Global.Translations.Get("not_detected"));
             return;
         }*/


				 
	}

	@Override
	public void OrientationChanged(float heading) {
		// TODO Auto-generated method stub
		
	}

	private void LoadImages()
	{
		//set LinkLable Color
		CachesFoundLabel.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		WP.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		
		//set Text Color
		
		descTextView.setTextColor(Global.getColor(R.attr.TextColor));
		versionTextView.setTextColor(Global.getColor(R.attr.TextColor));
		myTextView.setTextColor(Global.getColor(R.attr.TextColor));
		lblGPS.setTextColor(Global.getColor(R.attr.TextColor));
		GPS.setTextColor(Global.getColor(R.attr.TextColor));
		lblAccuracy.setTextColor(Global.getColor(R.attr.TextColor));
		Accuracy.setTextColor(Global.getColor(R.attr.TextColor));
		lblWP.setTextColor(Global.getColor(R.attr.TextColor));
		
		lblCord.setTextColor(Global.getColor(R.attr.TextColor));
		Cord.setTextColor(Global.getColor(R.attr.TextColor));
		lblCurrent.setTextColor(Global.getColor(R.attr.TextColor));
		Current.setTextColor(Global.getColor(R.attr.TextColor));
		
		
		Resources res = getResources();
		
		boolean N = Config.GetBool("nightMode");
		
		bitmap = BitmapFactory.decodeResource(res, N? R.drawable.night_splash_back : R.drawable.splash_back);
		logo = BitmapFactory.decodeResource(res, R.drawable.cachebox_logo);
		((ImageView) findViewById(R.id.splash_BackImage)).setImageBitmap(bitmap);
		((ImageView) findViewById(R.id.splash_Logo)).setImageBitmap(logo);
		
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

	@Override
	public void GpsStateChanged() 
	{
		if ((Global.Locator.getLocation() != null) && (Global.Locator.getLocation().hasAccuracy()))
        {
        	int radius = (int) Global.Locator.getLocation().getAccuracy();
        	Accuracy.setText("+/- " + String.valueOf(radius) + "m (" + Global.Locator.ProviderString()+")");
        }
	 else
	 {
		 Accuracy.setText("");
	 }
	 if (Global.Locator.getLocation() != null)
	 {
		 Current.setText(Global.FormatLatitudeDM(Global.Locator.getLocation().getLatitude()) + " " + Global.FormatLongitudeDM(Global.Locator.getLocation().getLongitude()));
		 GPS.setText(de.droidcachebox.Locator.GPS.getSatAndFix() + "   " + Global.Translations.Get("alt") + " " + Global.Locator.getAltString());
	 }
	 
	 if (Global.Locator == null)
     {
         GPS.setText(Global.Translations.Get("not_detected"));
         return;
     }
		
	 
	 setSatStrength();
	 
	}
	
	
	private static View[] balken = null;
	
	public static void setSatStrength()
	{
		
		de.droidcachebox.Locator.GPS.setSatStrength(strengthLayout, balken);
		
	}
	
}
