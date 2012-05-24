package de.cachebox_test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.FileList;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.DB.Database.DatabaseType;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
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
import CB_Core.Types.Categories;
import CB_Core.Types.Coordinate;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.CB_PlugIn.IPlugIn;
import de.cachebox_test.Components.copyAssetFolder;
import de.cachebox_test.DB.AndroidDB;
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

		// check if Layout forced from User
		workPath = Environment.getExternalStorageDirectory() + "/cachebox";

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
			// Tab Modus nur Landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		}

		// else if (Config.settings == null || !Config.settings.AllowLandscape.getValue())
		// {
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		//
		// }
		// else
		// {
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		//
		// }

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
		String database = Config.settings.DatabasePath.getValue();
		Database.Data.StartUp(database);

		// prevent mediascanner to parse all the images in the cachebox folder
		File nomedia = new File(workPath, ".nomedia");
		if (!nomedia.exists())
		{
			try
			{
				nomedia.createNewFile();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		// if (true)
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

		// UiSize Structur für die Berechnung der Größen zusammen stellen!
		Resources res = this.getResources();

		WindowManager w = this.getWindowManager();
		Display d = w.getDefaultDisplay();

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

		setProgressState(40, GlobalCore.Translations.Get("LoadMapPack"));

		de.cachebox_test.Map.Manager manager = new de.cachebox_test.Map.Manager();

		File dir = new File(Config.settings.MapPackFolder.getValue());
		String[] files = dir.list();
		if (!(files == null))
		{
			if (files.length > 0)
			{
				for (String file : files)
				{
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("pack")) manager.LoadMapPack(Config.settings.MapPackFolder
							.getValue() + "/" + file);
					if (FileIO.GetFileExtension(file).equalsIgnoreCase("map"))
					{
						Layer layer = new Layer(file, file, "");
						layer.isMapsForge = true;
						manager.Layers.add(layer);
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

		// Initial PlugIn
		setProgressState(60, GlobalCore.Translations.Get("IniPlugIn"));
		fillPluginList();
		bindPluginServices();

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

			if (!mSelectDbIsStartet)
			{
				mSelectDbIsStartet = true;
				mainActivity.startActivityForResult(selectDBIntent, 546132);
			}

		}
		else
		{
			Initial2();
		}
	}

	private boolean mSelectDbIsStartet = false;

	private void Initial2()
	{

		// initialize Database
		Database.Data = new AndroidDB(DatabaseType.CacheBox, this);
		String database = Config.settings.DatabasePath.getValue();
		Database.Data.StartUp(database);

		Config.settings.ReadFromDB();
		setProgressState(62, GlobalCore.Translations.Get("LoadCaches") + FileIO.GetFileName(Config.settings.DatabasePath.getValue()));
		String FilterString = Config.settings.Filter.getValue();
		GlobalCore.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0]) : new FilterProperties(
				FilterString);
		String sqlWhere = GlobalCore.LastFilter.getSqlWhere();

		GlobalCore.Categories = new Categories();
		Database.Data.GPXFilenameUpdateCacheCount();

		CacheListDAO cacheListDAO = new CacheListDAO();
		cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);

		Database.FieldNotes = new AndroidDB(DatabaseType.FieldNotes, this);
		if (!FileIO.DirectoryExists(Config.WorkPath + "/User")) return;
		Database.FieldNotes.StartUp(Config.WorkPath + "/User/FieldNotes.db3");

		Descriptor.Init();

		// chek Joker PlugIn

		if (Global.iPlugin != null && Global.iPlugin[0] != null)
		{
			Config.settings.hasCallPermission.setValue(true);
		}
		else
		{
			Config.settings.hasCallPermission.setValue(false);
		}

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

	// PlugIn Methodes
	private static final String LOG_TAG = "CB_PlugIn";
	private static final String ACTION_PICK_PLUGIN = "de.cachebox.action.PICK_PLUGIN";
	private static final String KEY_PKG = "pkg";
	private static final String KEY_SERVICENAME = "servicename";
	private ArrayList<HashMap<String, String>> services;
	private LayoutInflater inflater;
	private Handler uiHandler;
	private PluginServiceConnection pluginServiceConnection[] = new PluginServiceConnection[4];

	private void fillPluginList()
	{
		services = new ArrayList<HashMap<String, String>>();
		PackageManager packageManager = getPackageManager();
		Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER);
		// Log.d(LOG_TAG, "fillPluginList: " + list);

		Config.settings.hasFTF_PlugIn.setValue(false);
		Config.settings.hasFTF_PlugIn.setValue(false);
		int i;
		for (i = 0; i < list.size(); ++i)
		{
			ResolveInfo info = list.get(i);
			ServiceInfo sinfo = info.serviceInfo;
			// Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " + sinfo);
			if (sinfo != null)
			{

				if (sinfo.packageName.contains("de.CB_FTF_PlugIn")) // Don't bind, is an Widget
				{
					Config.settings.hasFTF_PlugIn.setValue(true);
				}
				else if (sinfo.packageName.contains("de.CB_PQ_PlugIn"))// Don't bind, is an Widget
				{
					Config.settings.hasPQ_PlugIn.setValue(true);
				}
				else
				// PlugIn for Bind
				{
					HashMap<String, String> item = new HashMap<String, String>();
					item.put(KEY_PKG, sinfo.packageName);
					item.put(KEY_SERVICENAME, sinfo.name);
					services.add(item);
					if (i <= 4)
					{
						inflateToView(i, packageManager, sinfo.packageName);

					}
				}
			}
		}
		Config.AcceptChanges();
	}

	private void inflateToView(int rowCtr, PackageManager packageManager, String packageName)
	{
		try
		{
			ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
			Resources res = packageManager.getResourcesForApplication(info);

			XmlResourceParser xres = res.getLayout(0x7f030000);
			// int parentId = selectRow(rowCtr);
			// ViewGroup parentView = (ViewGroup) findViewById(parentId);
			// parentView.removeAllViews();
			// View view = inflater.inflate(xres, parentView);
			// adjustSubViewIds(parentView, idxToIdOffset(rowCtr));
		}
		catch (NameNotFoundException ex)
		{
			Log.e(LOG_TAG, "NameNotFoundException", ex);
		}
	}

	private void bindPluginServices()
	{

		for (int i = 0; i < services.size(); ++i)
		{
			pluginServiceConnection[i] = new PluginServiceConnection();
			Intent intent = new Intent();
			HashMap<String, String> data = services.get(i);
			intent.setClassName(data.get(KEY_PKG), data.get(KEY_SERVICENAME));
			// Log.d(LOG_TAG, "bindPluginServices: " + intent);
			bindService(intent, pluginServiceConnection[i], Context.BIND_AUTO_CREATE);

		}

	}

	class PluginServiceConnection implements ServiceConnection
	{
		public void onServiceConnected(ComponentName className, IBinder boundService)
		{
			int idx = getServiceConnectionIndex();
			// Log.d(LOG_TAG, "onServiceConnected: ComponentName: " + className + "; idx: " + idx);
			if (idx >= 0)
			{
				Global.iPlugin[idx] = IPlugIn.Stub.asInterface((IBinder) boundService);
			}
		}

		public void onServiceDisconnected(ComponentName className)
		{
			int idx = getServiceConnectionIndex();
			// Log.d(LOG_TAG, "onServiceDisconnected: ComponentName: " + className + "; idx: " + idx);
			if (idx >= 0) Global.iPlugin[idx] = null;
		}

		private int getServiceConnectionIndex()
		{
			for (int i = 0; i < pluginServiceConnection.length; ++i)
				if (this == pluginServiceConnection[i]) return i;
			return -1;
		}
	};

}
