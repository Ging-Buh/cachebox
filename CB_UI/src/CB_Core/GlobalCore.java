package CB_Core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DB.Database;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.DisplayType;
import CB_Core.GL_UI.Controls.Animation.DownloadAnimation;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_Core.GL_UI.Controls.PopUps.ConnectionError;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Log.Logger;
import CB_Core.Log.Logger.iCreateDebugWithHeader;
import CB_Core.Map.RouteOverlay;
import CB_Core.Math.UI_Size_Base;
import CB_Core.Math.devicesSizes;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;

import com.badlogic.gdx.utils.Clipboard;

public class GlobalCore
{

	public static final int CurrentRevision = 1787;
	public static final String CurrentVersion = "0.6.";
	public static final String VersionPrefix = "Test";

	public static final String br = System.getProperty("line.separator");
	public static final String fs = System.getProperty("file.separator");
	// public static final String ps = System.getProperty("path.separator");
	public static final String AboutMsg = "Team Cachebox (2011-2013)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009,"
			+ br + "Groundspeak Inc. Used with permission";
	public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";

	public static boolean restartAfterKill = false;
	public static String restartCache;
	public static String restartWaypoint;

	public static double displayDensity = 1;
	public static Plattform platform = Plattform.undef;

	// ######### theme Path ###############
	public static String PathDefault;
	public static String PathCustom;
	public static String PathDefaultNight;
	public static String PathCustomNight;
	// ######################################

	// ###########create instance#############
	public final static GlobalCore INSTANCE = new GlobalCore();

	/**
	 * nur True beim ersten schreiben! Dann müssen erst die Missing Lang Strings eingelesen werden!
	 */
	private static boolean firstWrite = true;

	private GlobalCore()
	{
		Logger.setCreateDebugWithHeader(new iCreateDebugWithHeader()
		{
			@Override
			public void CreateDebugWithHeader(File DebugFile)
			{
				if (Config.settings.newInstall.getValue())
				{
					DebugFile.delete();
					Config.settings.newInstall.setValue(false);
				}

				String Msg = "";

				if (!DebugFile.exists())
				{
					// schreibe UI Sizes als erstes in die dbug.txt
					devicesSizes ui = UI_Size_Base.that.ui;

					if (ui == null) return; // Bin noch nicht soweit!

					StringBuilder sb = new StringBuilder();
					sb.append("###################################" + br);
					sb.append("##  CB Version: " + GlobalCore.getVersionString() + "         ##" + br);
					sb.append("###################################" + br + br + br);
					sb.append("################  Ui Sizes ############" + br);
					sb.append("Window = " + ui.Window.toString() + br);
					sb.append("Density = " + ui.Density + br);
					sb.append("RefSize = " + ui.RefSize + br);
					sb.append("TextSize_Normal = " + ui.TextSize_Normal + br);
					sb.append("ButtonTextSize = " + ui.ButtonTextSize + br);
					sb.append("IconSize = " + ui.IconSize + br);
					sb.append("Margin = " + ui.Margin + br);
					sb.append("ArrowSizeList = " + ui.ArrowSizeList + br);
					sb.append("ArrowSizeMap = " + ui.ArrowSizeMap + br);
					sb.append("TB_IconSize = " + ui.TB_IconSize + br);
					sb.append("isLandscape = " + ui.isLandscape + br);
					sb.append("    " + br);
					sb.append("MapViewDPIFaktor = " + Config.settings.MapViewDPIFaktor.getValue() + br);
					sb.append("MapViewFontFaktor = " + Config.settings.MapViewFontFaktor.getValue() + br);
					sb.append("#######################################" + br + br);

					sb.append("##########  Missing Lang Strings ######" + br);

					sb.append("#######################################" + br + br);

					Msg = sb.toString();

					FileWriter writer;
					try
					{
						writer = new FileWriter(DebugFile, true);
						writer.write(Msg);
						writer.close();
					}
					catch (IOException e)
					{

						e.printStackTrace();
					}

				}
				else
				{
					if (firstWrite)
					{
						try
						{
							Translation.readMissingStringsFile();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						firstWrite = false;
					}
				}

			}
		});
	}

	// #######################################

	public static RouteOverlay.Track AktuelleRoute = null;
	public static int aktuelleRouteCount = 0;
	public static long TrackDistance;

	public static boolean switchToCompassCompleted = false;

	public static GlobalLocationReceiver receiver;

	private static Clipboard defaultClipBoard;

	public static Clipboard getDefaultClipboard()
	{
		if (defaultClipBoard == null)
		{
			return null;
		}
		else
		{
			return defaultClipBoard;
		}
	}

	public static void setDefaultClipboard(Clipboard clipBoard)
	{
		defaultClipBoard = clipBoard;
	}

	/**
	 * Wird im Splash gesetzt und ist True, wenn es sich um ein Tablet handelt!
	 */
	public static boolean isTab = false;

	public static boolean forceTab = false;

	public static boolean forcePhone = false;

	public static boolean useSmallSkin = false;

	public static DisplayType displayType = DisplayType.Normal;

	public static boolean posibleTabletLayout;

	private static Cache selectedCache = null;
	private static boolean autoResort;

	public static FilterProperties LastFilter = null;

	public static void setSelectedCache(Cache cache)
	{
		setSelectedWaypoint(cache, null);
	}

	public static Cache getSelectedCache()
	{
		return selectedCache;
	}

	private static Cache nearestCache = null;

	public static Cache NearestCache()
	{
		return nearestCache;
	}

	private static Waypoint selectedWaypoint = null;

	public static void setSelectedWaypoint(Cache cache, Waypoint waypoint)
	{
		setSelectedWaypoint(cache, waypoint, true);
	}

	/**
	 * if changeAutoResort == false -> do not change state of autoResort Flag
	 * 
	 * @param cache
	 * @param waypoint
	 * @param changeAutoResort
	 */
	public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort)
	{
		selectedCache = cache;
		selectedWaypoint = waypoint;
		SelectedCacheEventList.Call(selectedCache, waypoint);

		if (changeAutoResort)
		{
			// switch off auto select
			GlobalCore.setAutoResort(false);
		}
	}

	public static void NearestCache(Cache nearest)
	{
		nearestCache = nearest;
	}

	public static Waypoint getSelectedWaypoint()
	{
		return selectedWaypoint;
	}

	/**
	 * APIisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein API Access Token vorhanden ist.
	 */
	public static boolean APIisOnline()
	{
		if (Config.GetAccessToken().length() == 0)
		{
			Logger.General("GlobalCore.APIisOnline() - no GC - API AccessToken");
			return false;
		}
		if (platformConector.isOnline())
		{
			return true;
		}
		return false;
	}

	/**
	 * JokerisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein Passwort für gcJoker.de vorhanden ist.
	 */
	public static boolean JokerisOnline()
	{
		if (Config.settings.GcJoker.getValue().length() == 0)
		{
			Logger.General("GlobalCore.JokerisOnline() - no Joker Password");
			return false;
		}
		if (platformConector.isOnline())
		{
			return true;
		}
		return false;
	}

	public static String getVersionString()
	{
		final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  "
				+ (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
		return ret;
	}

	public static Coordinate getSelectedCoord()
	{
		Coordinate ret = null;

		if (selectedWaypoint != null)
		{
			ret = selectedWaypoint.Pos;
		}
		else if (selectedCache != null)
		{
			ret = selectedCache.Pos;
		}

		return ret;
	}

	public static void checkSelectedCacheValid()
	{

		CacheList List = Database.Data.Query;

		// Prüfen, ob der SelectedCache noch in der cacheList drin ist.
		if ((List.size() > 0) && (GlobalCore.getSelectedCache() != null) && (List.GetCacheById(GlobalCore.getSelectedCache().Id) == null))
		{
			// der SelectedCache ist nicht mehr in der cacheList drin -> einen beliebigen aus der CacheList auswählen
			Logger.DEBUG("Change SelectedCache from " + GlobalCore.getSelectedCache().GcCode + "to" + List.get(0).GcCode);
			GlobalCore.setSelectedCache(List.get(0));
		}
		// Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
		if ((GlobalCore.getSelectedCache() == null) && (List.size() > 0))
		{
			GlobalCore.setSelectedCache(List.get(0));
			Logger.DEBUG("Set SelectedCache to " + List.get(0).GcCode + " first in List.");
		}
	}

	public static boolean getAutoResort()
	{
		return autoResort;
	}

	public static void setAutoResort(boolean value)
	{
		GlobalCore.autoResort = value;
	}

	private static boolean isTestVersionCheked = false;
	private static boolean isTestVersion = false;

	public static boolean isTestVersion()
	{
		if (isTestVersionCheked) return isTestVersion;
		isTestVersion = VersionPrefix.contains("Test");
		isTestVersionCheked = true;
		return isTestVersion;
	}

	private static CancelWaitDialog wd;

	public static CancelWaitDialog ImportSpoiler()
	{
		wd = CancelWaitDialog.ShowWait(Translation.Get("chkApiState"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
		{

			@Override
			public void isCanceld()
			{
				// TODO Handle Cancel

			}
		}, new Runnable()
		{

			@Override
			public void run()
			{
				Importer importer = new Importer();
				ImporterProgress ip = new ImporterProgress();
				int result = importer.importSpoilerForCacheNew(ip, GlobalCore.getSelectedCache());
				wd.close();
				if (result == GroundspeakAPI.CONNECTION_TIMEOUT)
				{
					GL.that.Toast(ConnectionError.INSTANCE);
					return;
				}

				if (result == GroundspeakAPI.API_IS_UNAVAILABLE)
				{
					GL.that.Toast(ApiUnavailable.INSTANCE);
					return;
				}
			}
		});
		return wd;
	}

	public interface IChkRedyHandler
	{
		public void chekReady(int MemberTypeId);
	}

	public static void chkAPiLogInWithWaitDialog(final IChkRedyHandler handler)
	{
		if (!GroundspeakAPI.API_isCheked())
		{
			CancelWaitDialog.ShowWait("chk API Key", DownloadAnimation.GetINSTANCE(), new IcancelListner()
			{

				@Override
				public void isCanceld()
				{
					// TODO Auto-generated method stub

				}
			}, new Runnable()
			{

				@Override
				public void run()
				{
					int ret = GroundspeakAPI.chkMemperShip(false);
					handler.chekReady(ret);
				}
			});
		}
		else
		{
			handler.chekReady(GroundspeakAPI.GetMembershipType());
		}

	}

}
