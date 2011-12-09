package de.cachebox_test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
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
import CB_Core.Types.Categories;
import CB_Core.Types.Coordinate;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.cachebox_test.Components.copyAssetFolder;
import de.cachebox_test.DB.AndroidDB;
import de.cachebox_test.Map.Layer;
import de.cachebox_test.Ui.Sizes;
import de.cachebox_test.Views.MapView;
import de.cachebox_test.Views.Forms.SelectDB;

public class splash extends Activity
{
	public static Activity mainActivity;

	ProgressBar myProgressBar;
	TextView myTextView;
	TextView versionTextView;
	TextView descTextView;
	Handler handler;
	Bitmap bitmap;
	Bitmap logo;
	Bitmap gc_power_logo;

	String GcCode = null;
	String guid = null;
	String name = null;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.splash);

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
			String uriQuery = uri.getQuery();

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

		myProgressBar = (ProgressBar) findViewById(R.id.splash_progressbar);
		myTextView = (TextView) findViewById(R.id.splash_TextView);
		myTextView.setTextColor(Color.BLACK);
		versionTextView = (TextView) findViewById(R.id.splash_textViewVersion);
		versionTextView.setText(Global.getVersionString());
		descTextView = (TextView) findViewById(R.id.splash_textViewDesc);
		descTextView.setText(Global.splashMsg);
		mainActivity = this;

		LoadImages();

		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				Initial();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 1000);

	}

	@Override
	public void onDestroy()
	{
		if (isFinishing())
		{
			ReleaseImages();
			versionTextView = null;
			myTextView = null;
			descTextView = null;
			mainActivity = null;

		}
		super.onDestroy();
	}

	private void Initial()
	{
		Logger.setDebug(Global.Debug);

		// Read Config
		String workPath = Environment.getExternalStorageDirectory() + "/cachebox";
		Config.Initialize(workPath, workPath + "/cachebox.config");

		// hier muss die Config Db initialisiert werden
		Database.Settings = new AndroidDB(DatabaseType.Settings, this);
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.Settings.StartUp(Config.WorkPath + "/User/Config.db3");

		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
		String database = Config.settings.DatabasePath.getValue();
		Database.Data.StartUp(database);

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
					((SettingEnum) setting).setValue(Config.GetString(setting.getName()));
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

		String PocketQueryFolder = Config.settings.PocketQueryFolder.getValue();
		File directoryPocketQueryFolder = new File(PocketQueryFolder);
		if (!directoryPocketQueryFolder.exists())
		{
			directoryPocketQueryFolder.mkdir();
		}
		String TileCacheFolder = Config.settings.TileCacheFolder.getValue();
		File directoryTileCacheFolder = new File(TileCacheFolder);
		if (!directoryTileCacheFolder.exists())
		{
			directoryTileCacheFolder.mkdir();
		}
		String User = workPath + "/User";
		File directoryUser = new File(User);
		if (!directoryUser.exists())
		{
			directoryUser.mkdir();
		}
		String TrackFolder = Config.settings.TrackFolder.getValue();
		File directoryTrackFolder = new File(TrackFolder);
		if (!directoryTrackFolder.exists())
		{
			directoryTrackFolder.mkdir();
		}
		String UserImageFolder = Config.settings.UserImageFolder.getValue();
		File directoryUserImageFolder = new File(UserImageFolder);
		if (!directoryUserImageFolder.exists())
		{
			directoryUserImageFolder.mkdir();
		}

		String repository = workPath + "/repository";
		File directoryrepository = new File(repository);
		if (!directoryrepository.exists())
		{
			directoryrepository.mkdir();
		}
		String DescriptionImageFolder = Config.settings.DescriptionImageFolder.getValue();
		File directoryDescriptionImageFolder = new File(DescriptionImageFolder);
		if (!directoryDescriptionImageFolder.exists())
		{
			directoryDescriptionImageFolder.mkdir();
		}
		String MapPackFolder = Config.settings.MapPackFolder.getValue();
		File directoryMapPackFolder = new File(MapPackFolder);
		if (!directoryMapPackFolder.exists())
		{
			directoryMapPackFolder.mkdir();
		}
		String SpoilerFolder = Config.settings.SpoilerFolder.getValue();
		File directorySpoilerFolder = new File(SpoilerFolder);
		if (!directorySpoilerFolder.exists())
		{
			directorySpoilerFolder.mkdir();
		}

		// copy AssetFolder only if Rev-Number changed, like at new installation
		if (Config.settings.installRev.getValue() < Global.CurrentRevision)
		{
			// String[] exclude = new String[]{"webkit","sounds","images"};
			copyAssetFolder myCopie = new copyAssetFolder();
			myCopie.copyAll(getAssets(), Config.WorkPath);
			Config.settings.installRev.setValue(Global.CurrentRevision);
			Config.settings.newInstall.setValue(true);
			Config.AcceptChanges();
		}
		else
		{
			Config.settings.newInstall.setValue(false);
			Config.AcceptChanges();
		}

		try
		{
			GlobalCore.Translations.ReadTranslationsFile(Config.settings.Sel_LanguagePath.getValue());
		}
		catch (IOException e)
		{

			e.printStackTrace();
		}

		setProgressState(20, GlobalCore.Translations.Get("IniUI"));
		Sizes.initial(false, this);
		Global.Paints.init(this);
		Global.InitIcons(this);

		setProgressState(40, GlobalCore.Translations.Get("LoadMapPack"));
		File dir = new File(Config.settings.MapPackFolder.getValue());
		String[] files = dir.list();
		if (!(files == null))
		{
			if (files.length > 0)
			{
				for (String file : files)
				{
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("pack")) MapView.Manager.LoadMapPack(Config.settings.MapPackFolder
							.getValue() + "/" + file);
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("map"))
					{
						Layer layer = new Layer(file, file, "");
						layer.isMapsForge = true;
						MapView.Manager.Layers.add(layer);
					}
				}
			}
		}
		setProgressState(60, GlobalCore.Translations.Get("LoadCaches"));
		if (Database.Data != null) Database.Data = null;

		double lat = Config.settings.MapInitLatitude.getValue();
		double lon = Config.settings.MapInitLongitude.getValue();
		if ((lat != -1000) && (lon != -1000))
		{
			GlobalCore.LastValidPosition = new Coordinate(lat, lon);
		}

		// chek permission
		if (this.getPackageManager().checkPermission("android.permission.CALL_PHONE", "de.cachebox_test") == PackageManager.PERMISSION_GRANTED)
		{
			Config.settings.hasCallPermission.setValue(true);
			Config.AcceptChanges();
		}

		// search number of DB3 files
		FileList fileList = null;
		try
		{
			fileList = new FileList(Config.WorkPath, "DB3");
		}
		catch (Exception ex)
		{
			Logger.Error("slpash.Initial()", "search number of DB3 files", ex);
		}
		if ((fileList.size() > 1) && Config.settings.MultiDBAsk.getValue())
		{
			// show Database Selection
			Intent selectDBIntent = new Intent().setClass(mainActivity, SelectDB.class);
			SelectDB.autoStart = true;
			// Bundle b = new Bundle();
			// b.putSerializable("Waypoint", aktWaypoint);
			// mainIntent.putExtras(b);
			mainActivity.startActivityForResult(selectDBIntent, 546132);
		}
		else
		{
			Initial2();
		}
	}

	private void Initial2()
	{

		// initialize Database
		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
		String database = Config.settings.DatabasePath.getValue();
		Database.Data.StartUp(database);

		Config.settings.ReadFromDB();
		setProgressState(62, GlobalCore.Translations.Get("LoadCaches") + FileIO.GetFileName(Config.settings.DatabasePath.getValue()));
		String FilterString = Config.settings.Filter.getValue();
		Global.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0]) : new FilterProperties(
				FilterString);
		String sqlWhere = Global.LastFilter.getSqlWhere();

		GlobalCore.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();

		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

		Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

		Descriptor.Init();

		Config.AcceptChanges();

		// Initial Ready Show main
		finish();
		Intent mainIntent = new Intent().setClass(splash.this, main.class);

		if (GcCode != null)
		{
			Bundle b = new Bundle();
			b.putSerializable("GcCode", GcCode);
			b.putSerializable("name", name);
			b.putSerializable("guid", guid);
			mainIntent.putExtras(b);
		}

		startActivity(mainIntent);
	}

	private void setProgressState(int progress, final String msg)
	{
		myProgressBar.setProgress(progress);

		Thread t = new Thread()
		{
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						myTextView.setText(msg);
					}
				});
			}
		};

		t.start();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		// SelectDB
		if (requestCode == 546132)
		{
			if (resultCode == RESULT_CANCELED)
			{
				finish();
			}
			else
			{
				TimerTask task = new TimerTask()
				{
					@Override
					public void run()
					{
						Initial2();
					}
				};

				Timer timer = new Timer();
				timer.schedule(task, 1000);
			}
		}
	}

	private void LoadImages()
	{
		Resources res = getResources();

		bitmap = BitmapFactory.decodeResource(res, R.drawable.splash_back);
		logo = BitmapFactory.decodeResource(res, R.drawable.cachebox_logo);
		gc_power_logo = BitmapFactory.decodeResource(res, R.drawable.power_gc_live);
		((ImageView) findViewById(R.id.splash_BackImage)).setImageBitmap(bitmap);
		((ImageView) findViewById(R.id.splash_Logo)).setImageBitmap(logo);
		((ImageView) findViewById(R.id.splash_GcPowerLogo)).setImageBitmap(gc_power_logo);
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
