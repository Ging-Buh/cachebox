package de.CB.TestBase;

import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.mapsforge.map.android.graphics.ext_AndroidGraphicFactory;
import org.mapsforge.map.model.DisplayModel;
import org.slf4j.LoggerFactory;

import CB_Locator.GpsStrength;
import CB_Locator.Location.ProviderType;
import CB_Locator.Locator;
import CB_Locator.Events.GpsStateChangeEventList;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.Events.PlatformConnector.IQuit;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.DevicesSizes;
import CB_Utils.Plattform;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.IChanged;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import de.CB.TestBase.Map.AndroidManager;
import de.CB.TestBase.Views.MainView;
import de.CB.TestBase.Views.splash;
import de.CB_VisualTest.android.R;


public class MainActivity extends AndroidApplication implements LocationListener, GpsStatus.NmeaListener, GpsStatus.Listener
{
	final static org.slf4j.Logger log = LoggerFactory.getLogger(MainActivity.class);
	
	String workPath;
	public static LocationManager locationManager;

	public static MainActivity that;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		that = this;

	

		GL.resetIsInitial();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.numSamples = 16;

		Resources res = this.getResources();
		DevicesSizes ui = new DevicesSizes();

		DisplayMetrics displaymetrics = res.getDisplayMetrics();

		int h = displaymetrics.heightPixels;
		int w = displaymetrics.widthPixels;

		setContentView(R.layout.main);

		// only Portrait
		if (h < w)
		{
			int flip = h;
			h = w;
			w = flip;
		}

		ui.Window = new Size(w, h);
		ui.Density = res.getDisplayMetrics().density;
		ui.isLandscape = false;

		Global.displayDensity = ui.Density;

		Plattform.used = Plattform.Android;
		ext_AndroidGraphicFactory.createInstance(this.getApplication());
		new AndroidManager(new DisplayModel());

		// Initial Config

		File cacheDir = new File(this.getCacheDir(), "/cachebox");

		workPath = cacheDir.getAbsolutePath();
		// getExternalSdPath("/freizeitkarte");
		new Config(workPath);
		// Config.Initialize(workPath, workPath + "/cachebox.config");

		chkDirectoryExistAndCreate();

		new UiSizes();
		UiSizes.that.initial(ui);

		// create new splash
		splash sp = new splash(0, 0, w, h, "Splash");

		// create new mainView
		ext_AndroidGraphicFactory.aplication = this.getApplication();
		MainView ma = new MainView(0, 0, w, h, "mainView");

		initialLocationManager();
		initialLocatorBase();

		final Ex Game = new Ex(ui.Window.width, ui.Window.height, sp, ma);

		initialize(Game, cfg);
		GL.that.onStart();
		initialPlatformConector();
	}

	private void initialLocationManager()
	{

		try
		{
			if (locationManager != null)
			{
				// ist schon initialisiert
				return;
			}

			// GPS
			// Get the location manager
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			// Define the criteria how to select the locatioin provider -> use
			// default
			Criteria criteria = new Criteria(); // noch n�tig ???
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setCostAllowed(true);
			criteria.setPowerRequirement(Criteria.POWER_LOW);

			/*
			 * Longri: Ich habe die Zeiten und Distanzen der Location Updates angepasst. Der Network Provider hat eine schlechte
			 * genauigkeit, darher reicht es wenn er alle 10sec einen wert liefert, wen der alte um 500m abweicht. Beim GPS Provider habe
			 * ich die aktualiesierungs Zeit verk�rzt, damit bei deaktiviertem Hardware Kompass aber die Werte trotzdem noch in einem
			 * gesunden Verh�ltnis zwichen Performance und Stromverbrauch, geliefert werden. Andere apps haben hier 0.
			 */

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 500, this);

			locationManager.addNmeaListener(this);
			locationManager.addGpsStatusListener(this);
		}
		catch (Exception e)
		{
			log.error("main.initialLocationManager()", e);
			e.printStackTrace();
		}

	}

	/**
	 * Initial all Locator functions
	 */
	private void initialLocatorBase()
	{
		// ##########################################################
		// initial Locator with saved Location
		// ##########################################################
		double latitude = -1000;
		double longitude = -1000;

		if (Config.settings != null)
		{
			try
			{
				latitude = Config.MapInitLatitude.getValue();
				longitude = Config.MapInitLongitude.getValue();
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			// reload config
			// TODO
		}

		ProviderType provider = (latitude == -1000) ? ProviderType.NULL : ProviderType.Saved;

		CB_Locator.Location initialLocation;

		if (provider == ProviderType.Saved)
		{
			initialLocation = new CB_Locator.Location(latitude, longitude, 0, false, 0, false, 0, 0, provider);
		}
		else
		{
			initialLocation = CB_Locator.Location.NULL_LOCATION;
		}

		new CB_Locator.Locator(initialLocation);

		// ##########################################################
		// initial settings changed handling
		// ##########################################################

		// Use Imperial units?
		CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
		Config.ImperialUnits.addChangedEventListener(new IChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseImperialUnits(Config.ImperialUnits.getValue());
			}
		});

		// GPS update time?
		CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
		Config.gpsUpdateTime.addChangedEventListener(new IChanged()
		{

			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setMinUpdateTime((long) Config.gpsUpdateTime.getValue());
			}
		});

		// Use magnetic Compass?
		CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
		Config.HardwareCompass.addChangedEventListener(new IChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setUseHardwareCompass(Config.HardwareCompass.getValue());
			}
		});

		// Magnetic compass level
		CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
		Config.HardwareCompassLevel.addChangedEventListener(new IChanged()
		{
			@Override
			public void isChanged()
			{
				CB_Locator.Locator.setHardwareCompassLevel(Config.HardwareCompassLevel.getValue());
			}
		});
	}

	@Override
	public void onDestroy()
	{

		if (isFinishing())
		{

			// Config.settings.WriteToDB();

			// GPS Verbindung beenden
			locationManager.removeUpdates(this);

			// Config.AcceptChanges();
			super.onDestroy();
			System.exit(0);
		}
		else
		{
			super.onDestroy();
		}
	}

	@Override
	public void onGpsStatusChanged(int event)
	{
		if (locationManager == null) return;

		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
		{
			GpsStatus status = locationManager.getGpsStatus(null);
			Iterator<GpsSatellite> statusIterator = status.getSatellites().iterator();

			int satellites = 0;
			int fixed = 0;
			CB_List<GpsStrength> SatList = new CB_List<GpsStrength>();
			CB_List<CB_Locator.GpsStrength> coreSatList = new CB_List<CB_Locator.GpsStrength>();
			while (statusIterator.hasNext())
			{
				GpsSatellite sat = statusIterator.next();
				satellites++;

				// satellite signal strength

				if (sat.usedInFix())
				{
					fixed++;
					// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr() + " FIX");
					SatList.add(new GpsStrength(true, sat.getSnr()));
					coreSatList.add(new GpsStrength(true, sat.getSnr()));
				}
				else
				{
					// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr());
					SatList.add(new GpsStrength(false, sat.getSnr()));
					coreSatList.add(new GpsStrength(false, sat.getSnr()));
				}

			}

			SatList.sort();
			coreSatList.sort();

			CB_Locator.GPS.setSatFixes(fixed);
			CB_Locator.GPS.setSatVisible(satellites);
			CB_Locator.GPS.setSatList(coreSatList);
			GpsStateChangeEventList.Call();
			if (fixed < 3 && (Locator.isFixed()))
			{

				if (!losseChek)
				{
					Timer timer = new Timer();
					TimerTask task = new TimerTask()
					{
						@Override
						public void run()
						{
							if (CB_Locator.GPS.getFixedSats() < 3) Locator.FallBack2Network();
							losseChek = false;
						}
					};
					timer.schedule(task, 1000);
				}

			}
		}

	}

	private boolean losseChek = false;

	@Override
	public void onNmeaReceived(long timestamp, String nmea)
	{
		try
		{
			if (nmea.length() >= 6 && nmea.substring(0, 6).equalsIgnoreCase("$GPGGA"))
			{
				String[] s = nmea.split(",");
				try
				{
					if (s[11].equals("")) return;
					if (!s[6].equals("1") & !s[6].equals("2")) return; // Fix ung�ltig
					double altCorrection = Double.valueOf(s[11]);
					if (altCorrection == 0) return;
					log.info("AltCorrection: " + String.valueOf(altCorrection));
					Locator.setAltCorrection(altCorrection);
					Log.d("NMEA.AltCorrection", Double.toString(altCorrection));
					// H�henkorrektur �ndert sich normalerweise nicht, einmal
					// auslesen reicht...
					locationManager.removeNmeaListener(this);
				}
				catch (Exception exc)
				{
					// keine H�henkorrektur vorhanden
				}
			}
		}
		catch (Exception e)
		{
			log.error("main.onNmeaReceived()", e);
			e.printStackTrace();
		}
	}

	@Override
	public void onLocationChanged(Location location)
	{
		ProviderType provider = ProviderType.NULL;

		if (location.getProvider().toLowerCase(new Locale("en")).contains("gps")) provider = ProviderType.GPS;
		if (location.getProvider().toLowerCase(new Locale("en")).contains("network")) provider = ProviderType.Network;

		CB_Locator.Locator.setNewLocation(new CB_Locator.Location(location.getLatitude(), location.getLongitude(), location.getAccuracy(),
				location.hasSpeed(), location.getSpeed(), location.hasBearing(), location.getBearing(), location.getAltitude(), provider));
	}

	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub

	}

	private void chkDirectoryExistAndCreate()
	{

		// FileIO.createDirectory(Config.WorkPath);
		//
		// if (!FileIO.createDirectory(Config.WorkPath + "/maps")) return;

		String[] exclude = new String[]
			{ "webkit", "sound", "sounds", "images", "skins", "lang", "kioskmode", "string-files", "" };
		copyAssetFolder myCopie = new copyAssetFolder();

		myCopie.copyAll(getAssets(), Config.WorkPath, exclude);

	}

	private String getExternalSdPath(String Folder)
	{
		// check if Layout forced from User
		workPath = Environment.getExternalStorageDirectory() + Folder;

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
			externalSd = prev + "/extSdCard" + Folder;
		}
		else if (testExtSdPath(prev + "/MicroSD"))
		{
			externalSd = prev + "/MicroSD" + Folder;
		}
		else if (testExtSdPath(prev + "/sdcard/ext_sd"))
		{
			externalSd = prev + "/sdcard/ext_sd" + Folder;
		}
		else if (testExtSdPath(prev + "/ext_card"))
		{
			// Sony Xperia sola
			externalSd = prev + "/ext_card" + Folder;
		}
		else if (testExtSdPath(prev + "/external"))
		{
			externalSd = prev + "/external" + Folder;
		}
		else if (testExtSdPath(prev + "/sdcard2"))
		{
			externalSd = prev + "/sdcard2" + Folder;
		}
		else if (testExtSdPath(prev + "/sdcard1"))
		{
			externalSd = prev + "/sdcard1" + Folder;
		}
		else if (testExtSdPath(prev + "/sdcard/_ExternalSD"))
		{
			externalSd = prev + "/sdcard/_ExternalSD";
		}
		else if (testExtSdPath(prev + "/sdcard-ext"))
		{
			externalSd = prev + "/sdcard-ext" + Folder;
		}
		else if (testExtSdPath(prev + "/external1"))
		{
			externalSd = prev + "/external1" + Folder;
		}
		else if (testExtSdPath(prev + "/sdcard/external_sd"))
		{
			externalSd = prev + "/sdcard/external_sd" + Folder;
		}
		else if (testExtSdPath(prev + "/emmc"))
		{
			// for CM9
			externalSd = prev + "/emmc" + Folder;
		}
		else if (testExtSdPath("/Removable/MicroSD"))
		{
			// Asus Transformer
			externalSd = prev + "/Removable/MicroSD" + Folder;
		}
		else if (testExtSdPath("/mnt/ext_sd"))
		{
			// ODYS Motion
			externalSd = prev + "/ext_sd" + Folder;
		}
		else if (testExtSdPath("/sdcard/tflash"))
		{
			// Car Radio
			externalSd = prev + "/sdcard/tflash" + Folder;
		}
		else if (testExtSdPath(prev + "/sdcard"))
		{
			// on some devices it is possible that the SD-Card reported by getExternalStorageDirectory() is the extSd and the real
			// external SD is /mnt/sdcard (Faktor2 Tablet!!!)
			externalSd = prev + "/sdcard" + Folder;
		}
		return externalSd;
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

	private void initialPlatformConector()
	{
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

		AndroidClipboard acb = new AndroidClipboard(cm);

		if (cm != null) Global.setDefaultClipboard(acb);

		CB_Android_FileExplorer fileExplorer = new CB_Android_FileExplorer(this);
		PlatformConnector.setGetFileListener(fileExplorer);
		PlatformConnector.setGetFolderListener(fileExplorer);

		PlatformConnector.setQuitListener(new IQuit()
		{
			@Override
			public void Quit()
			{
				finish();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{

		if (requestCode == Global.REQUEST_CODE_PICK_FILE_OR_DIRECTORY_FROM_PLATFORM_CONECTOR)
		{
			CB_Android_FileExplorer.onActivityResult(requestCode, resultCode, data);
			return;
		}
	}

}