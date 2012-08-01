package de.cachebox_test.Views;

import CB_Core.Config;
import CB_Core.Energy;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Events.GpsStateChangeEvent;
import CB_Core.Events.GpsStateChangeEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox;
import CB_Core.GL_UI.Controls.Dialogs.NumerikInputBox.returnValueListner;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.app.Dialog;
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
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Events.PositionEvent;
import de.cachebox_test.Events.PositionEventList;
import de.cachebox_test.Events.ViewOptionsMenu;
import de.cachebox_test.Views.Forms.MessageBox;
import de.cachebox_test.Views.Forms.PleaseWaitMessageBox;

public class AboutView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent, PositionEvent, GpsStateChangeEvent
{

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
	private static Dialog pd;

	public AboutView(Context context, final LayoutInflater inflater)
	{
		super(context);

		Me = this;

		SelectedCacheEventList.Add(this);

		FrameLayout notesLayout = (FrameLayout) inflater.inflate(R.layout.about, null, false);
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
				MessageBox.Show(GlobalCore.Translations.Get("LoadFounds"), GlobalCore.Translations.Get("AdjustFinds"),
						MessageBoxButtons.YesNo, MessageBoxIcon.GC_Live, new DialogInterface.OnClickListener()
						{

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

									pd = PleaseWaitMessageBox.Show(GlobalCore.Translations.Get("LoadFounds"), "GC-Live",
											MessageBoxButtons.Cancel, MessageBoxIcon.GC_Live, null);

									thread.start();

									break;
								case -2:
									NumerikInputBox.Show(GlobalCore.Translations.Get("AdjustFinds"),
											GlobalCore.Translations.Get("TelMeFounds"), CB_Core.Config.settings.FoundOffset.getValue(),
											DialogListner);
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
				if (GlobalCore.SelectedCache() == null) return;

				try
				{
					Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse(GlobalCore.SelectedCache().Url.trim()));
					main.mainActivity.startActivity(browserIntent);
				}
				catch (Exception exc)
				{
					Toast.makeText(
							main.mainActivity,
							GlobalCore.Translations.Get("Cann_not_open_cache_browser") + " (" + GlobalCore.SelectedCache().Url.trim() + ")",
							Toast.LENGTH_SHORT).show();
				}

			}

		});

	}

	protected static final returnValueListner DialogListner = new returnValueListner()
	{
		@Override
		public void returnValue(int value)
		{
			Config.settings.FoundOffset.setValue(value);
			Config.AcceptChanges();
			AboutView.Me.refreshText();
		}

		@Override
		public void cancelClicked()
		{

		}

	};

	private int transFounds = -1;
	private Handler onlineFoundsReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			pd.dismiss();

			if (transFounds > -1)
			{
				String Text = GlobalCore.Translations.Get("FoundsSetTo", String.valueOf(transFounds));
				MessageBox.Show(Text, GlobalCore.Translations.Get("LoadFinds!"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);

				Config.settings.FoundOffset.setValue(transFounds);
				Config.AcceptChanges();
				AboutView.Me.refreshText();
			}
			else
			{
				MessageBox.Show(GlobalCore.Translations.Get("LogInErrorLoadFinds"), "", MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
			}

		}
	};

	private void findViewById()
	{
		CachesFoundLabel = (TextView) findViewById(R.id.about_CachesFoundLabel);
		descTextView = (TextView) findViewById(R.id.splash_textViewDesc);
		versionTextView = (TextView) findViewById(R.id.splash_textViewVersion);
		myTextView = (TextView) findViewById(R.id.splash_TextView);
		lblGPS = (TextView) findViewById(R.id.about_lblGPS);
		GPS = (TextView) findViewById(R.id.about_GPS);
		lblAccuracy = (TextView) findViewById(R.id.about_lblAccuracy);
		Accuracy = (TextView) findViewById(R.id.about_Accuracy);
		lblWP = (TextView) findViewById(R.id.about_lblWP);
		WP = (TextView) findViewById(R.id.about_WP);
		lblCord = (TextView) findViewById(R.id.about_lblCord);
		Cord = (TextView) findViewById(R.id.about_Cord);
		lblCurrent = (TextView) findViewById(R.id.about_lblCurrent);
		Current = (TextView) findViewById(R.id.about_Current);

		strengthLayout = (LinearLayout) findViewById(R.id.strength_control);

		// set LinkLable Color
		CachesFoundLabel.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		WP.setTextColor(Global.getColor(R.attr.LinkLabelColor));

		// set Text Color
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

		// Set Progressbar from Splash unvisible and release the Obj.
		myProgressBar = (ProgressBar) findViewById(R.id.splash_progressbar);
		myProgressBar.setVisibility(View.GONE);
		myProgressBar = null;
	}

	private void setText()
	{
		versionTextView.setText(GlobalCore.getVersionString());
		descTextView.setText(GlobalCore.AboutMsg);

		lblGPS.setText(GlobalCore.Translations.Get("gps"));

		lblWP.setText(GlobalCore.Translations.Get("waypoint"));
		lblCord.setText(GlobalCore.Translations.Get("coordinate"));
		lblCurrent.setText(GlobalCore.Translations.Get("current"));
		lblAccuracy.setText(GlobalCore.Translations.Get("accuracy"));

		GPS.setText(GlobalCore.Translations.Get("not_active"));

		refreshText();
	}

	public void refreshText()
	{
		CachesFoundLabel
				.setText(GlobalCore.Translations.Get("caches_found") + " " + String.valueOf(Config.settings.FoundOffset.getValue()));
		if (GlobalCore.SelectedCache() != null) if (GlobalCore.SelectedWaypoint() != null)
		{
			WP.setText(GlobalCore.SelectedWaypoint().GcCode);
			Cord.setText(GlobalCore.FormatLatitudeDM(GlobalCore.SelectedWaypoint().Latitude()) + " "
					+ GlobalCore.FormatLongitudeDM(GlobalCore.SelectedWaypoint().Longitude()));
		}
		else
		{
			WP.setText(GlobalCore.SelectedCache().GcCode);
			Cord.setText(GlobalCore.FormatLatitudeDM(GlobalCore.SelectedCache().Latitude()) + " "
					+ GlobalCore.FormatLongitudeDM(GlobalCore.SelectedCache().Longitude()));
		}

		this.invalidate();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		main.mainActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				refreshText();
			}
		});

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
		if (!Energy.AboutIsShown()) Energy.setAboutIsShown();
		setSatStrength();
	}

	@Override
	public void OnHide()
	{
		if (Energy.AboutIsShown()) Energy.resetAboutIsShown();
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
		/*
		 * if ((Global.Locator.getLocation() != null) && (Global.Locator.getLocation().hasAccuracy())) { int radius = (int)
		 * Global.Locator.getLocation().getAccuracy(); Accuracy.setText("+/- " + String.valueOf(radius) + "m (" +
		 * Global.Locator.ProviderString()+")"); } else { Accuracy.setText(""); } if (Global.Locator.getLocation() != null) {
		 * Current.setText(Global.FormatLatitudeDM (Global.Locator.getLocation().getLatitude()) + " " + Global.FormatLongitudeDM
		 * (Global.Locator.getLocation().getLongitude())); GPS.setText(GlobalCore.Translations.Get("alt") + " " +
		 * Global.Locator.getAltString()); } if (Global.Locator == null) { GPS.setText(GlobalCore.Translations.Get("not_detected")); return;
		 * }
		 */

	}

	@Override
	public void OrientationChanged(float heading)
	{
		// TODO Auto-generated method stub

	}

	private void LoadImages()
	{
		// set LinkLable Color
		CachesFoundLabel.setTextColor(Global.getColor(R.attr.LinkLabelColor));
		WP.setTextColor(Global.getColor(R.attr.LinkLabelColor));

		// set Text Color

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

		boolean N = Config.settings.nightMode.getValue();

		int BackGroundResourceId = 0;

		BackGroundResourceId = N ? R.drawable.night_splash_back : R.drawable.splash_back;

		bitmap = BitmapFactory.decodeResource(res, BackGroundResourceId);
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
		if ((GlobalCore.Locator != null) && (GlobalCore.Locator.getLocation() != null) && (GlobalCore.Locator.getLocation().hasAccuracy()))
		{
			int radius = (int) GlobalCore.Locator.getLocation().getAccuracy();
			Accuracy.setText("+/- " + String.valueOf(radius) + "m (" + GlobalCore.Locator.ProviderString() + ")");
		}
		else
		{
			Accuracy.setText("");
		}
		if ((GlobalCore.Locator != null) && GlobalCore.Locator.getLocation() != null)
		{
			Current.setText(GlobalCore.FormatLatitudeDM(GlobalCore.Locator.getLocation().getLatitude()) + " "
					+ GlobalCore.FormatLongitudeDM(GlobalCore.Locator.getLocation().getLongitude()));
			GPS.setText(de.cachebox_test.Locator.GPS.getSatAndFix() + "   " + GlobalCore.Translations.Get("alt") + " "
					+ GlobalCore.Locator.getAltString());
		}

		if (GlobalCore.Locator == null)
		{
			GPS.setText(GlobalCore.Translations.Get("not_detected"));
			return;
		}

		setSatStrength();

	}

	private static View[] balken = null;

	public static void setSatStrength()
	{

		de.cachebox_test.Locator.GPS.setSatStrength(strengthLayout, balken);

	}

	@Override
	public String getReceiverName()
	{
		return "AboutView";
	}

}
