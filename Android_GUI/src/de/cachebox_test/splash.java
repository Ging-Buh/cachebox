package de.cachebox_test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Log.Logger;
import CB_Core.Math.Size;
import CB_Core.Math.UiSizes;
import CB_Core.Math.devicesSizes;
import CB_Core.Settings.SettingBase;
import CB_Core.Settings.SettingBool;
import CB_Core.Settings.SettingDouble;
import CB_Core.Settings.SettingEncryptedString;
import CB_Core.Settings.SettingEnum;
import CB_Core.Settings.SettingFile;
import CB_Core.Settings.SettingFolder;
import CB_Core.Settings.SettingInt;
import CB_Core.Settings.SettingIntArray;
import CB_Core.Settings.SettingString;
import CB_Core.Settings.SettingTime;
import CB_Core.Types.Coordinate;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.cachebox_test.Components.copyAssetFolder;
import de.cachebox_test.DB.AndroidDB;

public class splash extends Activity
{
	public static final String PREFS_NAME = "DroidCacheboxPrefsFile";

	public static Activity mainActivity;
	final Context context = this;
	Handler handler;
	Bitmap bitmap;
	Dialog pWaitD;
	String GcCode = null;
	String guid = null;
	String name = null;
	String GpxPath = null;

	String workPath;

	private boolean mOriantationRestart = false;
	private static devicesSizes ui;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.splash);

		GlobalCore.displayDensity = this.getResources().getDisplayMetrics().density;
		int h = this.getResources().getDisplayMetrics().heightPixels;
		int w = this.getResources().getDisplayMetrics().widthPixels;

		int sw = h > w ? w : h;
		sw /= GlobalCore.displayDensity;

		// check if tablet
		GlobalCore.isTab = sw > 400 ? true : false;

		// chek if use small skin
		GlobalCore.useSmallSkin = sw < 200 ? true : false;

		// get parameters
		final Bundle extras = getIntent().getExtras();
		final Uri uri = getIntent().getData();

		// try to get data from extras
		if (extras != null)
		{
			GcCode = extras.getString("geocode");
			name = extras.getString("name");
			guid = extras.getString("guid");
		}

		// try to get data from URI
		if (GcCode == null && guid == null && uri != null)
		{
			String uriHost = uri.getHost().toLowerCase();
			String uriPath = uri.getPath().toLowerCase();
			// String uriQuery = uri.getQuery();

			if (uriHost.contains("geocaching.com") == true)
			{
				GcCode = uri.getQueryParameter("wp");
				guid = uri.getQueryParameter("guid");

				if (GcCode != null && GcCode.length() > 0)
				{
					GcCode = GcCode.toUpperCase();
					guid = null;
				}
				else if (guid != null && guid.length() > 0)
				{
					GcCode = null;
					guid = guid.toLowerCase();
				}
				else
				{
					// warning.showToast(res.getString(R.string.err_detail_open));
					finish();
					return;
				}
			}
			else if (uriHost.contains("coord.info") == true)
			{
				if (uriPath != null && uriPath.startsWith("/gc") == true)
				{
					GcCode = uriPath.substring(1).toUpperCase();
				}
				else
				{
					// warning.showToast(res.getString(R.string.err_detail_open));
					finish();
					return;
				}
			}
		}

		if (uri != null)
		{
			if (uri.getEncodedPath().endsWith(".gpx"))
			{
				GpxPath = uri.getEncodedPath();
			}
		}

		mainActivity = this;

		LoadImages();

		if (savedInstanceState != null)
		{
			mSelectDbIsStartet = savedInstanceState.getBoolean("SelectDbIsStartet");
			mOriantationRestart = savedInstanceState.getBoolean("OriantationRestart");
		}

		if (mOriantationRestart) return; // wait for result

		// Thread thread = new Thread()
		// {
		// @Override
		// public void run()
		// {
		// Initial();
		// }
		// };
		//
		// thread.start();
		//
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		// first, try to find stored preferences of workPath
		SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
		workPath = settings.getString("WorkPath", "");
		boolean askAgain = settings.getBoolean("AskAgain", true);

		if ((workPath.length() == 0) || (askAgain))
		{
			// no saved workPath found -> search sd-cards and if more than 1 is found give the user the possibility to select one

			// check if Layout forced from User
			workPath = Environment.getExternalStorageDirectory() + "/cachebox";

			// extract first part of path ("/mnt/" or "/storage/" ...)
			int pos = workPath.indexOf("/", 2); // search for the second /
			String prev = "/mnt";
			if (pos > 0)
			{
				prev = workPath.substring(0, pos);
			}
			// search for an external SD-Card
			String externalSd = "";

			// search for an external sd card on different devices
			if (testExtSdPath(prev + "/extSdCard"))
			{
				externalSd = prev + "/extSdCard/CacheBox";
			}
			else if (testExtSdPath(prev + "/MicroSD"))
			{
				externalSd = prev + "/MicroSD/CacheBox";
			}
			else if (testExtSdPath(prev + "/sdcard/ext_sd"))
			{
				externalSd = prev + "/sdcard/ext_sd/CacheBox";
			}
			else if (testExtSdPath(prev + "/external"))
			{
				externalSd = prev + "/external/CacheBox";
			}
			else if (testExtSdPath(prev + "/sdcard2"))
			{
				externalSd = prev + "/sdcard2/CacheBox";
			}
			else if (testExtSdPath(prev + "/sdcard1"))
			{
				externalSd = prev + "/sdcard1/CacheBox";
			}
			else if (testExtSdPath(prev + "/sdcard/_ExternalSD"))
			{
				externalSd = prev + "/sdcard/_ExternalSD";
			}
			else if (testExtSdPath(prev + "/sdcard-ext"))
			{
				externalSd = prev + "/sdcard-ext/CacheBox";
			}
			else if (testExtSdPath(prev + "/external1"))
			{
				externalSd = prev + "/external1/CacheBox";
			}
			else if (testExtSdPath(prev + "/sdcard/external_sd"))
			{
				externalSd = prev + "/sdcard/external_sd/CacheBox";
			}
			else if (testExtSdPath(prev + "/emmc"))
			{
				// for CM9
				externalSd = prev + "/emmc/CacheBox";
			}
			else if (testExtSdPath("/Removable/MicroSD"))
			{
				// Asus Transformer
				externalSd = "/Removable/MicroSD/CacheBox";
			}
			else if (testExtSdPath(prev + "/sdcard"))
			{
				// on some devices it is possible that the SD-Card reported by getExternalStorageDirectory() is the extSd and the real
				// external SD is /mnt/sdcard (Faktor2 Tablet!!!)
				externalSd = prev + "/sdcard/CacheBox";
			}
			final String externalSd2 = externalSd;

			if ((externalSd.length() > 0) && (!externalSd.equalsIgnoreCase(workPath)))
			{
				// externe SD wurde gefunden != internal
				// -> Auswahldialog anzeigen
				try
				{
					final Dialog dialog = new Dialog(context);
					dialog.setContentView(R.layout.sdselectdialog);
					TextView title = (TextView) dialog.findViewById(R.id.select_sd_title);
					title.setText(title.getText() + "\n ");
					final CheckBox cbAskAgain = (CheckBox) dialog.findViewById(R.id.select_sd_askagain);
					cbAskAgain.setChecked(askAgain);
					Button buttonI = (Button) dialog.findViewById(R.id.button1);
					buttonI.setText("Internal SD\n\n" + workPath);
					buttonI.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							// close select dialog
							dialog.dismiss();

							// show please wait dialog
							showPleaseWaitDialog();

							// use internal SD -> nothing to change
							Thread thread = new Thread()
							{
								@Override
								public void run()
								{
									boolean askAgain = cbAskAgain.isChecked();
									saveWorkPath(askAgain);
									dialog.dismiss();
									startInitial();
								}
							};
							thread.run();
						}
					});
					Button buttonE = (Button) dialog.findViewById(R.id.button2);
					buttonE.setText("External SD\n\n" + externalSd);
					buttonE.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							// close select dialog
							dialog.dismiss();

							// show please wait dialog
							showPleaseWaitDialog();

							// use external SD -> change workPath
							Thread thread = new Thread()
							{
								@Override
								public void run()
								{
									workPath = externalSd2;
									boolean askAgain = cbAskAgain.isChecked();
									saveWorkPath(askAgain);
									startInitial();
								}
							};
							thread.run();

						}
					});

					dialog.show();

				}
				catch (Exception ex)
				{
					String x = ex.getMessage();
				}
			}
			else
			{
				startInitial();
			}
		}
		else
		{
			// restore the saved workPath
			// test whether workPath is available by checking the free size on the SD
			String workPathToTest = workPath.substring(0, workPath.lastIndexOf("/"));
			long bytesAvailable = 0;
			try
			{
				StatFs stat = new StatFs(workPathToTest);
				bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
			}
			catch (Exception ex)
			{
				bytesAvailable = 0;
			}
			if (bytesAvailable == 0)
			{
				// there is a workPath stored but this workPath is not available at the moment (maybe SD is removed)
				Toast.makeText(mainActivity, "WorkPath " + workPath + " is not available!\nMaybe SD-Card is removed?", Toast.LENGTH_LONG)
						.show();
				finish();
				return;
			}

			startInitial();
		}

	}

	private void showPleaseWaitDialog()
	{
		pWaitD = ProgressDialog.show(splash.this, "In progress", "Copy resources");

		pWaitD.show();
		TextView tv1 = (TextView) pWaitD.findViewById(android.R.id.message);
		tv1.setTextColor(Color.WHITE);
	}

	// this will test whether the extPath is an existing path to an external sd card
	private boolean testExtSdPath(String extPath)
	{
		if (extPath.equalsIgnoreCase(workPath)) return false; // if this extPath is the same than the actual workPath -> this is the
																// internal SD, not
		// the external!!!
		if (FileIO.FileExists(extPath))
		{
			StatFs stat = new StatFs(extPath);
			long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getBlockCount();
			if (bytesAvailable == 0) return false; // ext SD-Card is not plugged in -> do not use it
			else
				return true; // ext SD-Card is plugged in
		}
		return false;
	}

	private void saveWorkPath(boolean askAgain)
	{
		SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("WorkPath", workPath);
		editor.putBoolean("AskAgain", askAgain);
		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onDestroy()
	{
		if (isFinishing())
		{
			ReleaseImages();
			// versionTextView = null;
			// myTextView = null;
			// descTextView = null;
			mainActivity = null;

		}
		super.onDestroy();
		if (pWaitD != null && pWaitD.isShowing())
		{
			pWaitD.dismiss();
			pWaitD = null;
		}

		ui = null;

	}

	private void startInitial()
	{

		// show wait dialog if not running
		if (pWaitD == null) showPleaseWaitDialog();

		// saved workPath found -> use this
		Thread thread = new Thread()
		{
			@Override
			public void run()
			{
				Initial();
			}
		};

		thread.start();

	}

	private void Initial()
	{
		// Jetzt ist der workPath erstmal festgelegt.
		// Zur Kompatibilität mit älteren Installationen wird hier noch die redirection.txt abgefragt
		if (FileIO.FileExists(workPath + "/redirection.txt"))
		{
			BufferedReader Filereader;

			try
			{
				Filereader = new BufferedReader(new FileReader(workPath + "/redirection.txt"));
				String line;

				while ((line = Filereader.readLine()) != null)
				{
					// chk ob der umleitungs Ordner exestiert
					if (FileIO.FileExists(line))
					{
						workPath = line;
					}
				}

				Filereader.close();
			}
			catch (IOException e)
			{
				Logger.Error("read redirection", "", e);
				e.printStackTrace();
			}

		}

		if (FileIO.FileExists(workPath + "/.forcePhone"))
		{
			GlobalCore.isTab = false;
		}
		else if (FileIO.FileExists(workPath + "/.forceTablet"))
		{
			GlobalCore.isTab = true;
		}

		if (GlobalCore.isTab)
		{
			// Tab Modus only Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		}
		else
		{
			// Phone Modus only Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		}

		Logger.setDebug(Global.Debug);

		// Read Config
		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		Database.Settings = new AndroidDB(DatabaseType.Settings, this);
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");

		// wenn die Settings DB neu Erstellt wurde, müssen die Default werte
		// geschrieben werden.
		if (Database.Settings.isDbNew())
		{
			Config.settings.LoadAllDefaultValues();
			Config.settings.WriteToDB();
		}
		else
		{
			Config.settings.ReadFromDB();
		}

		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);

		// wenn eine cachebox.config existiert, werden die Werte in die DB
		// übertragen
		if (FileIO.FileExists(workPath + "/cachebox.config"))
		{
			Config.readConfigFile(/* getAssets() */);

			for (Iterator<SettingBase> it = Config.settings.values().iterator(); it.hasNext();)
			{
				SettingBase setting = it.next();

				if (setting instanceof SettingBool)
				{
					((SettingBool) setting).setValue(Config.GetBool(setting.getName()));
				}
				else if (setting instanceof SettingIntArray)
				{
					((SettingIntArray) setting).setValue(Config.GetInt(setting.getName()));
				}
				else if (setting instanceof SettingTime)
				{
					((SettingTime) setting).setValue(((Config.GetInt("LockM") * 60) + Config.GetInt("LockSec")) * 1000);
				}
				else if (setting instanceof SettingInt)
				{
					((SettingInt) setting).setValue(Config.GetInt(setting.getName()));
				}
				else if (setting instanceof SettingDouble)
				{
					((SettingDouble) setting).setValue(Config.GetDouble(setting.getName()));
				}
				else if (setting instanceof SettingFolder)
				{
					((SettingFolder) setting).setValue(Config.GetString(setting.getName()));
				}
				else if (setting instanceof SettingFile)
				{
					((SettingFile) setting).setValue(Config.GetString(setting.getName()));
				}
				else if (setting instanceof SettingEnum)
				{
					((SettingEnum<?>) setting).setValue(Config.GetString(setting.getName()));
				}
				else if (setting instanceof SettingEncryptedString)
				{
					((SettingEncryptedString) setting).setEncryptedValue(Config.GetString(setting.getName() + "Enc"));
				}
				else if (setting instanceof SettingString)
				{
					((SettingString) setting).setValue(Config.GetString(setting.getName()));
				}

			}
			// Schreibe settings in die DB
			Config.AcceptChanges();

			// cachebox.config umbenennen.
			File f = new File(workPath + "/cachebox.config");
			f.renameTo(new File(workPath + "/ALT_cachebox.config"));

		}

		// copy AssetFolder only if Rev-Number changed, like at new installation
		if (Config.settings.installRev.getValue() < GlobalCore.CurrentRevision)
		// if (true)
		{
			// String[] exclude = new String[]{"webkit","sounds","images"};
			copyAssetFolder myCopie = new copyAssetFolder();
			myCopie.copyAll(getAssets(), Config.WorkPath);
			Config.settings.installRev.setValue(GlobalCore.CurrentRevision);
			Config.settings.newInstall.setValue(true);
			Config.AcceptChanges();
		}
		else
		{
			Config.settings.newInstall.setValue(false);
			Config.AcceptChanges();
		}

		// UiSize Structur für die Berechnung der Größen zusammen stellen!
		Resources res = this.getResources();

		// WindowManager w = this.getWindowManager();
		// Display d = w.getDefaultDisplay();

		FrameLayout frame = (FrameLayout) findViewById(R.id.frameLayout1);
		int width = frame.getMeasuredWidth();
		int height = frame.getMeasuredHeight();

		if (ui == null)
		{
			ui = new devicesSizes();

			ui.Window = new Size(width, height);
			ui.Density = res.getDisplayMetrics().density;
			ui.ButtonSize = new Size(res.getDimensionPixelSize(R.dimen.BtnSize),
					(int) ((res.getDimensionPixelSize(R.dimen.BtnSize) - 5.3333f * ui.Density)));
			ui.RefSize = res.getDimensionPixelSize(R.dimen.RefSize);
			ui.TextSize_Normal = res.getDimensionPixelSize(R.dimen.TextSize_normal);
			ui.ButtonTextSize = res.getDimensionPixelSize(R.dimen.BtnTextSize);
			ui.IconSize = res.getDimensionPixelSize(R.dimen.IconSize);
			ui.Margin = res.getDimensionPixelSize(R.dimen.Margin);
			ui.ArrowSizeList = res.getDimensionPixelSize(R.dimen.ArrowSize_List);
			ui.ArrowSizeMap = res.getDimensionPixelSize(R.dimen.ArrowSize_Map);
			ui.TB_IconSize = res.getDimensionPixelSize(R.dimen.TB_icon_Size);
			ui.isLandscape = false;
		}
		UiSizes.initial(ui);
		Global.Paints.init(this);
		Global.InitIcons(this);

		new de.cachebox_test.Map.AndroidManager();

		double lat = Config.settings.MapInitLatitude.getValue();
		double lon = Config.settings.MapInitLongitude.getValue();
		if ((lat != -1000) && (lon != -1000))
		{
			GlobalCore.LastValidPosition = new Coordinate(lat, lon);
		}

		Initial2();
	}

	private boolean mSelectDbIsStartet = false;

	private void Initial2()
	{

		// initialize Database
		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
		Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);

		Config.AcceptChanges();

		// Initial Ready Show main
		finish();
		Intent mainIntent = new Intent().setClass(splash.this, main.class);
		Bundle b = new Bundle();
		if (GcCode != null)
		{

			b.putSerializable("GcCode", GcCode);
			b.putSerializable("name", name);
			b.putSerializable("guid", guid);

		}

		if (GpxPath != null)
		{
			b.putSerializable("GpxPath", GpxPath);
		}

		if (pWaitD != null)
		{
			pWaitD.dismiss();
			pWaitD = null;
		}

		b.putSerializable("UI", ui);
		mainIntent.putExtras(b);
		startActivity(mainIntent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean("SelectDbIsStartet", mSelectDbIsStartet);
		outState.putBoolean("OriantationRestart", true);
	}

	private void LoadImages()
	{

		((TextView) findViewById(R.id.splash_textViewDesc)).setVisibility(View.INVISIBLE);
		((TextView) findViewById(R.id.splash_textViewVersion)).setVisibility(View.INVISIBLE);
		((TextView) findViewById(R.id.splash_TextView)).setVisibility(View.INVISIBLE);

	}

	private void ReleaseImages()
	{
		((ImageView) findViewById(R.id.splash_BackImage)).setImageResource(0);

		if (bitmap != null)
		{
			bitmap.recycle();
			bitmap = null;
		}

	}

	// private LayoutInflater inflater;
	// private Handler uiHandler;

}
