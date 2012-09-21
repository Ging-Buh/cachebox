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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import de.cachebox_test.Components.copyAssetFolder;
import de.cachebox_test.DB.AndroidDB;

public class splash extends Activity
{
	public static Activity mainActivity;

	Handler handler;
	Bitmap bitmap;

	String GcCode = null;
	String guid = null;
	String name = null;

	String workPath;

	private boolean mOriantationRestart = false;
	private static devicesSizes ui;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		GlobalCore.displayDensity = this.getResources().getDisplayMetrics().density;
		int h = this.getResources().getDisplayMetrics().heightPixels;
		int w = this.getResources().getDisplayMetrics().widthPixels;

		int sw = h > w ? w : h;
		sw /= GlobalCore.displayDensity;

		// check if tablet
		GlobalCore.isTab = sw > 400 ? true : false;

		// chek if use small skin
		GlobalCore.useSmallSkin = sw < 200 ? true : false;

		// check if Layout forced from User
		workPath = Environment.getExternalStorageDirectory() + "/cachebox";

		// chk gibt es eine Pfad Umleitung

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

		setContentView(GlobalCore.isTab ? R.layout.tab_splash : R.layout.splash);

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

		mainActivity = this;

		LoadImages();

		if (savedInstanceState != null)
		{
			mSelectDbIsStartet = savedInstanceState.getBoolean("SelectDbIsStartet");
			mOriantationRestart = savedInstanceState.getBoolean("OriantationRestart");
		}

		if (mOriantationRestart) return; // wait for result

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
	}

	private void Initial()
	{
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
		Resources res = getResources();

		bitmap = BitmapFactory.decodeResource(res, R.drawable.splash_back);

		((ImageView) findViewById(R.id.splash_BackImage)).setImageBitmap(bitmap);
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
