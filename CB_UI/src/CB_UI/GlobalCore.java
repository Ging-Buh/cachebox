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

import CB_Core.Api.API_ErrorEventHandlerList;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.CacheListChangedEventList;
import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Import.ImporterProgress;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverCacheInterface;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Map.Track;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static CB_Core.Api.API_ErrorEventHandlerList.handleApiKeyError;
import static CB_Core.Api.GroundspeakAPI.*;

/**
 * @author ging-buh
 * @author arbor95
 * @author longri
 */
public class GlobalCore extends CB_UI_Base.Global implements SolverCacheInterface {
    public static final String aboutMsg1 = "Team Cachebox (2011-2019)" + br;
    public static final String teamLink = "www.team-cachebox.de";
    public static final String aboutMsg2 = br + "Cache Icons Copyright 2009," + br + "Groundspeak Inc. Used with permission";
    public static final String aboutMsg = aboutMsg1 + teamLink + aboutMsg2;
    public static final String splashMsg = aboutMsg + br + br + "POWERED BY:";
    private static final String CurrentVersion = "2.0.";
    private static final String log = "GlobalCore";
    public static boolean restartAfterKill = false;
    public static String restartCache;
    public static String restartWaypoint;
    public static boolean filterLogsOfFriends = false;
    public static Track AktuelleRoute = null;
    public static int aktuelleRouteCount = 0;
    public static boolean switchToCompassCompleted = false;
    public static GlobalLocationReceiver receiver;
    public static boolean RunFromSplash = false;
    private static GlobalCore mINSTANCE;
    private static Cache selectedCache = null;
    private static boolean autoResort;
    private static Cache nearestCache = null;
    private static Waypoint selectedWaypoint = null;
    private static CancelWaitDialog wd;
    private int CurrentRevision;
    private String VersionPrefix;

    private GlobalCore() {
        super();
        Solver.solverCacheInterface = this;
        mINSTANCE = this;
    }

    public static GlobalCore getInstance() {
        if (mINSTANCE == null) {
            mINSTANCE = new GlobalCore();
            mINSTANCE.initVersionInfos();
        }
        return mINSTANCE;
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

    static Cache NearestCache() {
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
     * @param cache            cache
     * @param waypoint         wapoint
     * @param changeAutoResort changeAutoResort
     */
    public static void setSelectedWaypoint(Cache cache, Waypoint waypoint, boolean changeAutoResort) {

        if (cache == null) {
            selectedCache = null;
            selectedWaypoint = null;
            return;
        }

        // // rewrite Changed Values ( like Favroite state)
        // if (selectedCache != null)
        // {
        // if (!Cache.getGcCode().equals("CBPark"))
        // {
        // Cache lastCache = Database.Data.cacheList.GetCacheById(selectedCache.Id);
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
        if (GroundspeakAPI.GetSettingsAccessToken().length() == 0) {
            return false;
        }
        return PlatformConnector.isOnline();
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

        CacheList List = Database.Data.cacheList;

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

    public static CancelWaitDialog ImportSpoiler(boolean withLogImages) {
        wd = CancelWaitDialog.ShowWait(Translation.get("downloadSpoiler"), DownloadAnimation.GetINSTANCE(), () -> {
            // canceled
        }, new ICancelRunnable() {

            @Override
            public void run() {
                // Importer importer = new Importer();
                ImporterProgress ip = new ImporterProgress();
                int result = GroundspeakAPI.ERROR;
                if (GlobalCore.getSelectedCache() != null)
                    result = DescriptionImageGrabber.GrabImagesSelectedByCache(ip, true, false, GlobalCore.getSelectedCache().Id, GlobalCore.getSelectedCache().getGcCode(), "", "", withLogImages);
                wd.close();
                if (result != OK) {
                    GL.that.Toast(LastAPIError);
                }
            }

            @Override
            public boolean doCancel() {
                // TODO Handle Cancel
                return false;
            }
        });
        return wd;
    }

    public static void MsgDownloadLimit() {
        GL.that.RunOnGLWithThreadCheck(() -> MessageBox.show(Translation.get("Limit_msg"), Translation.get("Limit_title"), MessageBoxButtons.OK, MessageBoxIcon.GC_Live, null));

    }

    public static void chkAPiLogInWithWaitDialog(final iChkReadyHandler handler) {

        // Live API
        if (GroundspeakAPI.isDownloadLimitExceeded()) {
            MsgDownloadLimit();
            return;
        }

        if (isAccessTokenInvalid()) {
            CancelWaitDialog.ShowWait("chk API Key", DownloadAnimation.GetINSTANCE(), null, new ICancelRunnable() {
                @Override
                public void run() {
                    handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                    // check after some time, if the AccessToken is now available (is got)
                    Timer ti = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            handler.checkReady(isAccessTokenInvalid());
                        }
                    };
                    ti.schedule(task, 300);
                }

                @Override
                public boolean doCancel() {
                    return false;
                }
            });
        } else {
            handler.checkReady(false);
        }

    }

    /**
     * Returns true, if a Cache selected and this Cache object is valid.
     *
     * @return see above
     */
    public static boolean isSetSelectedCache() {
        if (selectedCache == null)
            return false;

        return selectedCache.getGcCode().length() != 0;
    }

    private void initVersionInfos() {
        String info;
        try {
            // GDX works with Android
            FileHandle fileHandle = Gdx.files.internal("build.info");
            info = fileHandle.readString("utf-8");
        } catch (Exception ex) {
            // but on Desktop class GDX is not loaded yet
            File file = FileFactory.createFile("build.info");
            try {
                BufferedReader br = new BufferedReader(file.getFileReader());
                info = br.readLine();
            } catch (Exception ex1) {
                // if nothing works
                info = "#1000#master#687f624ef#2000-01-01";
                Log.err(log, "initVersionInfos " + ex.getLocalizedMessage());
            }
        }
        try {
            String[] sections = info.split("#");
            VersionPrefix = sections[1];
            String dat = sections[4];
            // count only, if published or stable. So you can revert to a previous revision without saving data
            dat = "2019-05-17";
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(dat);
            CurrentRevision = Integer.decode((new SimpleDateFormat("yyyyMMdd")).format(d));
        } catch (Exception ex) {
            // for parsing of date gives an error
            VersionPrefix = "1000";
            CurrentRevision = 0;
        }
    }

    public String getVersionString() {
        return "Version: " + CurrentVersion + CurrentRevision + "  (" + VersionPrefix + ")";
    }

    @Override
    protected String getVersionPrefix() {
        return VersionPrefix;
    }

    public Integer getCurrentRevision() {
        return CurrentRevision;
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

    public interface iChkReadyHandler {
        void checkReady(boolean invalidAccessToken);
    }

}
