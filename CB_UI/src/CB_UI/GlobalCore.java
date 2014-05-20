/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.FilterProperties;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DB.Database;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverCacheInterface;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.Map.RouteOverlay;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.devicesSizes;
import CB_Utils.Log.Logger;
import CB_Utils.Log.Logger.iCreateDebugWithHeader;

/**
 * @author ging-buh
 * @author arbor95
 * @author longri
 */
public class GlobalCore extends CB_UI_Base.Global implements SolverCacheInterface
{
	public static final int CurrentRevision = 2139;

	public static final String CurrentVersion = "0.7.";
	public static final String VersionPrefix = "test";

	// public static final String ps = System.getProperty("path.separator");
	public static final String AboutMsg = "Team Cachebox (2011-2014)" + br + "www.team-cachebox.de" + br + "Cache Icons Copyright 2009,"
			+ br + "Groundspeak Inc. Used with permission";
	public static final String splashMsg = AboutMsg + br + br + "POWERED BY:";

	public static boolean restartAfterKill = false;
	public static String restartCache;
	public static String restartWaypoint;
	public static boolean filterLogsOfFriends = false;

	// ###########create instance#############
	public final static GlobalCore INSTANCE = new GlobalCore();

	/**
	 * nur True beim ersten schreiben! Dann müssen erst die Missing Lang Strings eingelesen werden!
	 */
	private static boolean firstWrite = true;

	private GlobalCore()
	{
		super();
		Solver.solverCacheInterface = this;
		Logger.setCreateDebugWithHeader(new iCreateDebugWithHeader()
		{
			@Override
			public void CreateDebugWithHeader(File DebugFile)
			{
				if (Config.newInstall.getValue())
				{
					DebugFile.delete();
					Config.newInstall.setValue(false);
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
					sb.append("MapViewDPIFaktor = " + Config.MapViewDPIFaktor.getValue() + br);
					sb.append("MapViewFontFaktor = " + Config.MapViewFontFaktor.getValue() + br);
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

	private static Cache selectedCache = null;
	private static boolean autoResort;

	public static FilterProperties LastFilter = null;

	public static void setSelectedCache(Cache Cache)
	{
		setSelectedWaypoint(Cache, null);
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

	public static void setSelectedWaypoint(Cache Cache, Waypoint waypoint)
	{
		setSelectedWaypoint(Cache, waypoint, true);
	}

	/**
	 * if changeAutoResort == false -> do not change state of autoResort Flag
	 * 
	 * @param Cache
	 * @param waypoint
	 * @param changeAutoResort
	 */
	public static void setSelectedWaypoint(Cache Cache, Waypoint waypoint, boolean changeAutoResort)
	{

		if (Cache == null)
		{
			selectedCache = null;
			selectedWaypoint = null;
			return;
		}

		// rewrite Changed Values ( like Favroite state)
		if (selectedCache != null)
		{
			if (!Cache.getGcCode().equals("CBPark"))
			{
				Cache lastCache = Database.Data.Query.GetCacheById(selectedCache.Id);

			}
		}

		// remove Detail Info from old selectedCache
		if ((selectedCache != Cache) && (selectedCache != null) && (selectedCache.detail != null))
		{
			selectedCache.deleteDetail(Config.ShowAllWaypoints.getValue());
		}
		selectedCache = Cache;
		selectedWaypoint = waypoint;

		// load Detail Info if not available
		if (selectedCache.detail == null)
		{
			selectedCache.loadDetail();
		}

		SelectedCacheEventList.Call(selectedCache, selectedWaypoint);

		if (changeAutoResort)
		{
			// switch off auto select
			GlobalCore.setAutoResort(false);
		}

		GL.that.renderOnce();
	}

	public static void setNearestCache(Cache Cache)
	{
		nearestCache = Cache;
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

	static boolean JaokerPwChk = false;
	static boolean JokerPwExist = false;

	/**
	 * JokerisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein Passwort für gcJoker.de vorhanden ist.
	 */
	public static boolean JokerisOnline()
	{
		if (!JaokerPwChk)
		{
			JokerPwExist = Config.GcJoker.getValue().length() == 0;
			JaokerPwChk = true;
		}

		if (JokerPwExist)
		{
			// Logger.General("GlobalCore.JokerisOnline() - no Joker Password");
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
			Logger.DEBUG("Change SelectedCache from " + GlobalCore.getSelectedCache().getGcCode() + "to" + List.get(0).getGcCode());
			GlobalCore.setSelectedCache(List.get(0));
		}
		// Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
		if ((GlobalCore.getSelectedCache() == null) && (List.size() > 0))
		{
			GlobalCore.setSelectedCache(List.get(0));
			Logger.DEBUG("Set SelectedCache to " + List.get(0).getGcCode() + " first in List.");
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

	private static CancelWaitDialog wd;

	public static boolean RunFromSplash = false;

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

	static CancelWaitDialog dia;

	public static void chkAPiLogInWithWaitDialog(final IChkRedyHandler handler)
	{
		if (!GroundspeakAPI.API_isCheked())
		{
			dia = CancelWaitDialog.ShowWait("chk API Key", DownloadAnimation.GetINSTANCE(), new IcancelListner()
			{

				@Override
				public void isCanceld()
				{
					dia.close();
				}
			}, new Runnable()
			{

				@Override
				public void run()
				{
					final int ret = GroundspeakAPI.chkMemperShip(false);
					dia.close();

					Timer ti = new Timer();
					TimerTask task = new TimerTask()
					{

						@Override
						public void run()
						{
							handler.chekReady(ret);
						}
					};
					ti.schedule(task, 300);

				}
			});
		}
		else
		{
			handler.chekReady(GroundspeakAPI.chkMemperShip(true));
		}

	}

	@Override
	protected String getVersionPrefix()
	{
		return VersionPrefix;
	}

	// Interface für den Solver zum Zugriff auf den SelectedCache.
	// Direkter Zugriff geht nicht da der Solver im Core definiert ist
	@Override
	public Cache sciGetSelectedCache()
	{
		return getSelectedCache();
	}

	@Override
	public void sciSetSelectedCache(Cache cache)
	{
		setSelectedCache(cache);
	}

	@Override
	public void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint)
	{
		setSelectedWaypoint(cache, waypoint);
	}

	@Override
	public Waypoint sciGetSelectedWaypoint()
	{
		return getSelectedWaypoint();
	}

}
