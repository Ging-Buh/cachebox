/*
 * Copyright (C) 2014-2017 team-cachebox.de
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

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.Import.Importer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverCacheInterface;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Map.Track;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Log.Log;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author ging-buh
 * @author arbor95
 * @author longri
 */
public class GlobalCore extends CB_UI_Base.Global implements SolverCacheInterface {
    public static final int CurrentRevision = 20180608;
    public static final String CurrentVersion = "2.0.";
    public static final String VersionPrefix = "3142";
    public static final String aboutMsg1 = "Team Cachebox (2011-2018)" + br;
    public static final String teamLink = "www.team-cachebox.de";
    public static final String aboutMsg2 = br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String aboutMsg = aboutMsg1 + teamLink + aboutMsg2;
    public static final String splashMsg = aboutMsg + br + br + "POWERED BY:";
    // ###########create instance#############
    public final static GlobalCore INSTANCE = new GlobalCore();
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GlobalCore.class);
    public static boolean restartAfterKill = false;
    public static String restartCache;
    public static String restartWaypoint;
    public static boolean filterLogsOfFriends = false;
    public static Track AktuelleRoute = null;

    // #######################################
    public static int aktuelleRouteCount = 0;
    public static boolean switchToCompassCompleted = false;
    // public static long TrackDistance;
    public static GlobalLocationReceiver receiver;
    public static boolean RunFromSplash = false;
    static boolean JaokerPwChk = false;
    static boolean JokerPwExist = false;
    static CancelWaitDialog dia;
    private static Cache selectedCache = null;
    private static boolean autoResort;
    private static Cache nearestCache = null;
    private static Waypoint selectedWaypoint = null;
    private static CancelWaitDialog wd;

    private GlobalCore() {
        super();
        Solver.solverCacheInterface = this;
    }

    public static Cache getSelectedCache() {
        return selectedCache;
    }

    public static void setSelectedCache(Cache cache) {
        setSelectedWaypoint(cache, null);
    }

    public static boolean selectedCachehasSpoiler() {
        if (selectedCache != null) {
            return selectedCache.hasSpoiler();
        } else
            return false;
    }

    public static Cache NearestCache() {
        return nearestCache;
    }

    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint) {
        if (cache == null)
            return;

        setSelectedWaypoint(cache, waypoint, true);
        if (waypoint == null) {
            CoreSettingsForward.cacheHistory = cache.getGcCode() + "," + CoreSettingsForward.cacheHistory;
            if (CoreSettingsForward.cacheHistory.length() > 120) {
                CoreSettingsForward.cacheHistory = CoreSettingsForward.cacheHistory.substring(0, CoreSettingsForward.cacheHistory.lastIndexOf(","));
            }
        }
    }

    /**
     * if changeAutoResort == false -> do not change state of autoResort Flag
     *
     * @param cache
     * @param waypoint
     * @param changeAutoResort
     */
    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort) {

        if (cache == null) {
            Log.info(log, "[GlobalCore]setSelectedWaypoint: cache=null");
            selectedCache = null;
            selectedWaypoint = null;
            return;
        }

        // // rewrite Changed Values ( like Favroite state)
        // if (selectedCache != null)
        // {
        // if (!Cache.getGcCode().equals("CBPark"))
        // {
        // Cache lastCache = Database.Data.Query.GetCacheById(selectedCache.Id);
        //
        // }
        // }

        // remove Detail Info from old selectedCache
        if ((selectedCache != cache) && (selectedCache != null) && (selectedCache.detail != null)) {
            selectedCache.deleteDetail(Config.ShowAllWaypoints.getValue());
        }
        selectedCache = cache;
        Log.info(log, "[GlobalCore]setSelectedWaypoint: cache=" + cache.getGcCode());
        selectedWaypoint = waypoint;

        // load Detail Info if not available
        if (selectedCache.detail == null) {
            selectedCache.loadDetail();
        }

        SelectedCacheEventList.Call(selectedCache, selectedWaypoint);

        if (changeAutoResort) {
            // switch off auto select
            GlobalCore.setAutoResort(false);
        }

        GL.that.renderOnce();
    }

    public static void setNearestCache(Cache Cache) {
        nearestCache = Cache;
    }

    public static Waypoint getSelectedWaypoint() {
        return selectedWaypoint;
    }

    /**
     * APIisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein API Access Token vorhanden ist.
     */
    public static boolean APIisOnline() {
        if (Config.GetAccessToken().length() == 0) {
            Log.info(log, "GlobalCore.APIisOnline() - no GC - API AccessToken");
            return false;
        }
        if (PlatformConnector.isOnline()) {
            return true;
        }
        return false;
    }

    /**
     * JokerisOnline Liefert TRUE wenn die Möglichkeit besteht auf das Internet zuzugreifen und ein Passwort für gcJoker.de vorhanden
     * ist.
     */
    public static boolean JokerisOnline() {
        if (!JaokerPwChk) {
            JokerPwExist = Config.GcJoker.getValue().length() == 0;
            JaokerPwChk = true;
        }

        if (JokerPwExist) {
            // Log.info(log, "GlobalCore.JokerisOnline() - no Joker Password");
            return false;
        }
        if (PlatformConnector.isOnline()) {
            return true;
        }
        return false;
    }

    public static String getVersionString() {
        final String ret = "Version: " + CurrentVersion + String.valueOf(CurrentRevision) + "  " + (VersionPrefix.equals("") ? "" : "(" + VersionPrefix + ")");
        return ret;
    }

    public static Coordinate getSelectedCoord() {
        Coordinate ret = null;

        if (selectedWaypoint != null) {
            ret = selectedWaypoint.Pos;
        } else if (selectedCache != null) {
            ret = selectedCache.Pos;
        }

        return ret;
    }

    public static void checkSelectedCacheValid() {

        CacheList List = Database.Data.Query;

        // Prüfen, ob der SelectedCache noch in der cacheList drin ist.
        if ((List.size() > 0) && (GlobalCore.isSetSelectedCache()) && (List.GetCacheById(GlobalCore.getSelectedCache().Id) == null)) {
            // der SelectedCache ist nicht mehr in der cacheList drin -> einen beliebigen aus der CacheList auswählen
            Log.debug(log, "Change SelectedCache from " + GlobalCore.getSelectedCache().getGcCode() + "to" + List.get(0).getGcCode());
            GlobalCore.setSelectedCache(List.get(0));
        }
        // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
        if ((GlobalCore.getSelectedCache() == null) && (List.size() > 0)) {
            GlobalCore.setSelectedCache(List.get(0));
            Log.debug(log, "Set SelectedCache to " + List.get(0).getGcCode() + " first in List.");
        }
    }

    public static boolean getAutoResort() {
        return autoResort;
    }

    public static void setAutoResort(boolean value) {
        GlobalCore.autoResort = value;
    }

    public static CancelWaitDialog ImportSpoiler() {
        wd = CancelWaitDialog.ShowWait(Translation.Get("downloadSpoiler"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceld() {
                // TODO Handle Cancel

            }
        }, new cancelRunnable() {

            @Override
            public void run() {
                Importer importer = new Importer();
                ImporterProgress ip = new ImporterProgress();
                int result = importer.importSpoilerForCacheNew(ip, GlobalCore.getSelectedCache());
                wd.close();
                if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
                    GL.that.Toast(ConnectionError.INSTANCE);
                    return;
                }

                if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
                    GL.that.Toast(ApiUnavailable.INSTANCE);
                    return;
                }
            }

            @Override
            public boolean cancel() {
                // TODO Handle Cancel
                return false;
            }
        });
        return wd;
    }

    public static void MsgDownloadLimit() {
        GL.that.RunOnGLWithThreadCheck(new IRunOnGL() {

            @Override
            public void run() {
                GL_MsgBox.Show(Translation.Get("Limit_msg"), Translation.Get("Limit_title"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null);
            }
        });

    }

    public static void chkAPiLogInWithWaitDialog(final IChkRedyHandler handler) {

        if (GroundspeakAPI.ApiLimit()) {
            MsgDownloadLimit();
            return;
        }

        if (!GroundspeakAPI.mAPI_isChecked()) {
            dia = CancelWaitDialog.ShowWait("chk API Key", DownloadAnimation.GetINSTANCE(), new IcancelListener() {

                @Override
                public void isCanceld() {
                    dia.close();
                }
            }, new cancelRunnable() {

                @Override
                public void run() {
                    final int ret = GroundspeakAPI.chkMembership(false);
                    dia.close();

                    Timer ti = new Timer();
                    TimerTask task = new TimerTask() {

                        @Override
                        public void run() {
                            handler.checkReady(ret);
                        }
                    };
                    ti.schedule(task, 300);

                }

                @Override
                public boolean cancel() {
                    // TODO Handle Cancel
                    return false;
                }
            });
        } else {
            handler.checkReady(GroundspeakAPI.chkMembership(true));
        }

    }

    /**
     * Returns true, if a Cache selected and this Cache object is valid.
     *
     * @return
     */
    public static boolean isSetSelectedCache() {
        if (selectedCache == null)
            return false;

        if (selectedCache.getGcCode().length() == 0)
            return false;

        return true;
    }

    @Override
    protected String getVersionPrefix() {
        return VersionPrefix;
    }

    // Interface für den Solver zum Zugriff auf den SelectedCache.
    // Direkter Zugriff geht nicht da der Solver im Core definiert ist
    @Override
    public Cache sciGetSelectedCache() {
        return getSelectedCache();
    }

    @Override
    public void sciSetSelectedCache(Cache cache) {
        setSelectedCache(cache);
        CacheListChangedEventList.Call();
    }

    @Override
    public void sciSetSelectedWaypoint(Cache cache, Waypoint waypoint) {
        setSelectedWaypoint(cache, waypoint);
        CacheListChangedEventList.Call();
    }

    @Override
    public Waypoint sciGetSelectedWaypoint() {
        return getSelectedWaypoint();
    }

    public interface IChkRedyHandler {
        public void checkReady(int MemberTypeId);
    }

}
