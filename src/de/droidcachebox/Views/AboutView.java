package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.Events.CachListChangedEventList;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Views.Forms.NumerickInputBox;
import de.droidcachebox.Views.Forms.numerik_inputbox_dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent, PositionEvent {

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
	
	public static AboutView Me;
	
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
		
		
		
		
		CachesFoundLabel.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View arg0) 
			{
				NumerickInputBox.Show(Global.Translations.Get("AdjustFinds"),Global.Translations.Get("TelMeFounds"),Config.GetInt("FoundOffset"), DialogListner);
			}
		});
		
		
		 
		 
		
		WP.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View arg0) 
			{
				 if (Global.SelectedCache()== null)
		                return;

		            if (!Config.GetBool("AllowInternetAccess"))
		            {
		            	Toast.makeText(main.mainActivity, Global.Translations.Get("allowInetConn"), Toast.LENGTH_SHORT).show();
		                return;
		            }
		            
		            try
		            {
		            	Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(Global.SelectedCache().Url.trim()));
		            	main.mainActivity.startActivity(browserIntent);
		            }
		            catch (Exception exc)
		            {
		            	Toast.makeText(main.mainActivity, Global.Translations.Get("Cann_not_open_cache_browser") + " (" + Global.SelectedCache().Url.trim() + ")", Toast.LENGTH_SHORT).show();
                    }
				
			}
			
			
			
			
		});
		
	}
	
	
	protected static final  DialogInterface.OnClickListener  DialogListner = new  DialogInterface.OnClickListener() 
	   { 
		
		@Override public void onClick(DialogInterface dialog, int button) 
			{
				String text =((numerik_inputbox_dialog) dialog).editText.getText().toString();
				// Behandle das ergebniss
				switch (button)
				{
					case -1: // ok Clicket
						int newFounds = Integer.parseInt(text);

		                Config.Set("FoundOffset", newFounds );
		                Config.AcceptChanges();

						break;
					case -2: // cancel clicket
						Toast.makeText(main.mainActivity, "Click Button 2", Toast.LENGTH_SHORT).show();
						break;
					case -3:
						Toast.makeText(main.mainActivity, "Click Button 3", Toast.LENGTH_SHORT).show();
						break;
				}
				
				dialog.dismiss();
				AboutView.Me.refrechText();
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
		
		
		
		//set LinkLable Color
		CachesFoundLabel.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		WP.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		
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
		
		
		
		refrechText();
	}
	
	public void refrechText()
	{
		CachesFoundLabel.setText(Global.Translations.Get("caches_found") + " " + String.valueOf(Config.GetInt("FoundOffset")));
		if (Global.SelectedCache() != null)
            if (Global.SelectedWaypoint() != null)
            {
            	WP.setText(Global.SelectedWaypoint().GcCode);
            	Cord.setText(Global.FormatLatitudeDM(Global.SelectedWaypoint().Latitude()) + " " + Global.FormatLongitudeDM(Global.SelectedWaypoint().Longitude()));
            }
            else
            {
            	WP.setText(Global.SelectedCache().GcCode);
            	Cord.setText(Global.FormatLatitudeDM(Global.SelectedCache().Latitude()) + " " + Global.FormatLongitudeDM(Global.SelectedCache().Longitude()));
            }
		
		this.invalidate();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) 
	{
		refrechText();
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
		 if ((Global.Locator.getLocation() != null) && (Global.Locator.getLocation().hasAccuracy()))
	        {
	        	float radius = Global.Locator.getLocation().getAccuracy();
	        	Accuracy.setText("+/- " + String.valueOf(radius) + "m");
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
         }

		 
		 //TODO add this if the Locator is correct implemented
		 /*if (Global.Locator.IsGpsResponding())
         {
             if (Global.Locator.Position.Valid)
             {
                 //lblGps.setText("OK";
                 lblGPS.setText(Global.Translations.Get("sats_nr") + " " + Global.Locator.NumSatellites + " " + Global.Translations.Get("Hdop") + " " + Global.Locator.HDOP.ToString("F1", CultureInfo.InvariantCulture) + " " + Global.Translations.Get("alt") + " " + Global.Locator.Position.Elevation.ToString("0"));
                 labelCoordinatesCurrent.Text = Global.FormatLatitudeDM(Global.Locator.Position.Latitude) + " " + Global.FormatLongitudeDM(Global.Locator.Position.Longitude);

             }
             else
                 lblGPS.setText(Global.Translations.Get("waiting_for_fix"));
         }
         else
             lblGPS.setText(Global.Translations.Get("not_responding"));*/
				 
	}

	@Override
	public void OrientationChanged(float heading) {
		// TODO Auto-generated method stub
		
	}

	private void LoadImages()
	{
		Resources res = getResources();
		
		bitmap = BitmapFactory.decodeResource(res, R.drawable.splash_back);
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
	
}
